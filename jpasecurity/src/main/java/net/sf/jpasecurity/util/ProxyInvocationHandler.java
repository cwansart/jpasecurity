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
package net.sf.jpasecurity.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This implementation of the {@link java.lang.reflect.InvocationHandler} interface
 * delegates to a target object if the super-class cannot handle an invocation.
 * @see AbstractInvocationHandler for invocation handling of the super-class
 * @author Arne Limburg
 */
public class ProxyInvocationHandler<T> extends AbstractInvocationHandler {

    private T target;

    public ProxyInvocationHandler(T target) {
        this.target = target;
    }

    public T createProxy() {
        Class<?> targetClass = getTarget().getClass();
        return (T)Proxy.newProxyInstance(targetClass.getClassLoader(), getImplementingInterfaces(targetClass), this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result;
        if (canInvoke(method)) {
            result = super.invoke(proxy, method, args);
        } else {
            try {
                result = method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        return getTarget().equals(result)? proxy: result;
    }

    protected T getTarget() {
        return target;
    }

    /**
     * This method returns an empty set.
     * Subclasses may override this method to specify interfaces to implement.
     */
    protected Collection<Class<?>> getImplementingInterfaces() {
        return Collections.EMPTY_SET;
    }

    private Class<?>[] getImplementingInterfaces(Class<?> type) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.addAll(getImplementingInterfaces());
        while (type != null) {
            for (Class<?> iface: type.getInterfaces()) {
                interfaces.add(iface);
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }
}
