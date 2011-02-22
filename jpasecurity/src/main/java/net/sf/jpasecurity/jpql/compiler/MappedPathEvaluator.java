/*
 * Copyright 2008 - 2011 Arne Limburg
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
package net.sf.jpasecurity.jpql.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.sf.jpasecurity.ExceptionFactory;
import net.sf.jpasecurity.mapping.ClassMappingInformation;
import net.sf.jpasecurity.mapping.MappingInformation;
import net.sf.jpasecurity.mapping.PropertyMappingInformation;

/**
 * @author Arne Limburg
 */
public class MappedPathEvaluator implements PathEvaluator {

    private MappingInformation mappingInformation;
    private ExceptionFactory exceptionFactory;

    public MappedPathEvaluator(MappingInformation mappingInformation, ExceptionFactory exceptionFactory) {
        this.mappingInformation = mappingInformation;
        this.exceptionFactory = exceptionFactory;
    }

    public Object evaluate(Object root, String path) {
        if (root == null) {
            return null;
        }
        final Collection<Object> rootCollection =
            root instanceof Collection ? (Collection<Object>)root : Collections.singleton(root);
        Collection<Object> result = evaluateAll(rootCollection, path);
        if (result.size() > 1) {
            throw exceptionFactory.createInvalidPathException(path, "path is not single-valued");
        }
        return result.isEmpty()? null: result.iterator().next();
    }

    public Collection<Object> evaluateAll(Collection<Object> root, String path) {
        String[] pathElements = path.split("\\.");
        Collection rootCollection = new ArrayList<Object>(root);
        Collection<Object> resultCollection = new ArrayList<Object>();
        for (String property: pathElements) {
            resultCollection.clear();
            for (Object rootObject: rootCollection) {
                ClassMappingInformation classMapping = mappingInformation.getClassMapping(rootObject.getClass());
                if (classMapping == null) {
                    throw exceptionFactory.createTypeNotFoundException(rootObject.getClass());
                }
                PropertyMappingInformation propertyMapping = classMapping.getPropertyMapping(property);
                if (propertyMapping != null) {
                    Object result = propertyMapping.getPropertyValue(rootObject);
                    if (result != null) {
                        resultCollection.add(result);
                    }
                } //otherwise it must be a subclass property of another subclass,
                  //so this rootObject is not part of the current result
            }
            rootCollection.clear();
            for (Object resultObject: resultCollection) {
                if (resultObject instanceof Collection) {
                    rootCollection.addAll((Collection)resultObject);
                } else {
                    rootCollection.add(resultObject);
                }
            }
        }
        return resultCollection;
    }
}
