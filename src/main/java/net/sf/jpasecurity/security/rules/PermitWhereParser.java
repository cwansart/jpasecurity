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
package net.sf.jpasecurity.security.rules;

import java.util.HashMap;
import java.util.Map;

import net.sf.jpasecurity.util.AbstractAnnotationParser;

/**
 * This class parses classes for the {@link PermitWhere} annotation.
 * @author Arne Limburg
 */
public class PermitWhereParser extends AbstractAnnotationParser<PermitWhere> {

    private Map<Class<?>, PermitWhere> permissions = new HashMap<Class<?>, PermitWhere>();

    /**
     * Parses the specified classes for the {@link PermitWhere} annotation.
     * @param classes the classes to parse
     * @return a map containing the {@link PermitWhere} annotations for the specified classes
     */
    public Map<Class<?>, PermitWhere> parsePermissions(Class<?>... classes) {
        parse(classes);
        return permissions;
    }

    protected void process(Class<?> annotatedClass, PermitWhere permitWhere) {
        permissions.put(annotatedClass, permitWhere);
    }
}
