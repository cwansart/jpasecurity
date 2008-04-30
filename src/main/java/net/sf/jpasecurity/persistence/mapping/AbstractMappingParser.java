/*
 * Copyright 2008 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.sf.jpasecurity.persistence.mapping;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import javax.persistence.PersistenceException;

/**
 * Parses classes for persistence annotations and stores mapping information
 * in the provided map.
 * @author Arne Limburg
 */
public abstract class AbstractMappingParser {

    public static final String READ_PROPERTY_PREFIX = "get";

    private Map<Class<?>, ClassMappingInformation> classMappings;

    public AbstractMappingParser(Map<Class<?>, ClassMappingInformation> classMappings) {
        this.classMappings = classMappings;
    }

    public ClassMappingInformation parse(Class<?> mappedClass) {
        ClassMappingInformation classMapping = classMappings.get(mappedClass);
        if (classMapping == null) {
            Class<?> superclass = mappedClass.getSuperclass();
            ClassMappingInformation superclassMapping = null;
            if (superclass != null) {
                superclassMapping = parse(mappedClass.getSuperclass());
            }
            if (isMapped(mappedClass)) {
                boolean usesFieldAccess;
                if (superclassMapping != null) {
                    usesFieldAccess = superclassMapping.usesFieldAccess();
                } else {
                    usesFieldAccess = usesFieldAccess(mappedClass);
                }
                Class<?> idClass = null;
                if (superclassMapping == null || superclassMapping.getIdClass() == null) {
                    idClass = getIdClass(mappedClass, usesFieldAccess);
                }
                //TODO add extracting of the entity name here
                classMapping = new ClassMappingInformation(mappedClass.getSimpleName(),
                                                           mappedClass,
                                                           superclassMapping,
                                                           idClass,
                                                           usesFieldAccess);
                classMappings.put(mappedClass, classMapping);
                if (usesFieldAccess) {
                    for (Field field: mappedClass.getDeclaredFields()) {
                        PropertyMappingInformation propertyMapping = parse(field);
                        classMapping.addPropertyMapping(propertyMapping);
                    }
                } else {
                    for (Method method: mappedClass.getDeclaredMethods()) {
                        if (method.getName().startsWith(READ_PROPERTY_PREFIX)) {
                            PropertyMappingInformation propertyMapping = parse(method);
                            classMapping.addPropertyMapping(propertyMapping);
                        }
                    }
                }
            } else {
                classMapping = superclassMapping;
            }
        }
        return classMapping;
    }

    private PropertyMappingInformation parse(Member property) {
        String name = getName(property);
        Class<?> type = getType(property);
        ClassMappingInformation classMapping = parse(property.getDeclaringClass());
        boolean isIdProperty = isIdProperty(property);
        if (isSingleValuedRelationshipProperty(property)) {
            ClassMappingInformation typeMapping = parse(type);
            return new SingleValuedRelationshipMappingInformation(name, typeMapping, classMapping, isIdProperty);
        } else if (isCollectionValuedRelationshipProperty(property)) {
            ClassMappingInformation targetMapping = parse(getTargetType(property));
            return new CollectionValuedRelationshipMappingInformation(name, type, targetMapping, classMapping, isIdProperty);
        } else if (isSimplePropertyType(type)) {
            return new SimplePropertyMappingInformation(name, type, classMapping, isIdProperty);
        } else {
            throw new PersistenceException("could not determine mapping for property \"" + name + "\" of class " + property.getDeclaringClass().getName());
        }
    }

    protected String getName(Member property) {
        if (property instanceof Method) {
            return Character.toLowerCase(property.getName().charAt(3)) + property.getName().substring(4);
        } else {
            return property.getName();
        }
    }

    private Class<?> getType(Member property) {
        if (property instanceof Method) {
            return ((Method)property).getReturnType();
        } else {
            return ((Field)property).getType();
        }
    }

    protected Class<?> getTargetType(Member property) {
        Type genericType;
        if (property instanceof Method) {
            genericType = ((Method)property).getGenericReturnType();
        } else {
            genericType = ((Field)property).getGenericType();
        }
        if (!(genericType instanceof ParameterizedType)) {
            throw new PersistenceException("no target entity specified for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
        Type[] genericTypeArguments = ((ParameterizedType)genericType).getActualTypeArguments();
        Type genericTypeArgument;
        if (genericTypeArguments.length == 1) {
            genericTypeArgument = genericTypeArguments[0];
        } else if (genericTypeArguments.length == 2) {
            //Must be a map, take the value
            genericTypeArgument = genericTypeArguments[1];
        } else {
            throw new PersistenceException("could not determine target entity for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
        if (genericTypeArgument instanceof Class) {
            return (Class<?>)genericTypeArgument;
        } else {
            Type[] bounds = null;
            if (genericTypeArgument instanceof TypeVariable) {
                bounds = ((TypeVariable)genericTypeArgument).getBounds();
            } else if (genericTypeArgument instanceof WildcardType) {
                bounds = ((WildcardType)genericTypeArgument).getUpperBounds();
            }
            if (bounds != null) {
                for (Type bound: ((TypeVariable)genericTypeArgument).getBounds()) {
                    if (bound instanceof Class) {
                        return (Class<?>)bound;
                    }
                }
            }
            throw new PersistenceException("could not determine target entity for property \"" + property.getName() + "\" of class " + property.getDeclaringClass().getName());
        }
    }

    protected boolean usesFieldAccess(Class<?> mappedClass) {
        Field[] fields = mappedClass.getDeclaredFields();
        for (Field field: fields) {
            if (isMappable(field) && isMapped(field)) {
                return true;
            }
        }
        return false;
    }

    protected abstract boolean isMapped(Class<?> mappedClass);

    protected abstract boolean isMapped(Member member);
    
    protected abstract Class<?> getIdClass(Class<?> entityClass, boolean usesFieldAccess);

    protected boolean isMappable(Member member) {
        return !Modifier.isStatic(member.getModifiers()) && !Modifier.isTransient(member.getModifiers());
    }
    
    protected boolean isSimplePropertyType(Class<?> type) {
        return isEmbeddable(type)
            || type.isPrimitive()
            || type.equals(Boolean.class)
            || type.equals(Byte.class)
            || type.equals(Short.class)
            || type.equals(Integer.class)
            || type.equals(Long.class)
            || type.equals(BigInteger.class)
            || type.equals(Float.class)
            || type.equals(Double.class)
            || type.equals(BigDecimal.class)
            || type.equals(Character.class)
            || type.equals(String.class)
            || type.equals(java.util.Date.class)
            || type.equals(Calendar.class)
            || type.equals(java.sql.Date.class)
            || type.equals(Time.class)
            || type.equals(Timestamp.class)
            || type.equals(byte[].class)
            || type.equals(Byte[].class)
            || type.equals(char[].class)
            || type.equals(Character[].class)
            || Enum.class.isAssignableFrom(type)
            || Serializable.class.isAssignableFrom(type);
    }
    
    protected abstract boolean isEmbeddable(Class<?> type);

    protected abstract boolean isIdProperty(Member property);
    
    protected boolean isRelationshipProperty(Member property) {
        return isSingleValuedRelationshipProperty(property) || isCollectionValuedRelationshipProperty(property);
    }

    protected abstract boolean isSingleValuedRelationshipProperty(Member property);

    protected abstract boolean isCollectionValuedRelationshipProperty(Member property);
}
