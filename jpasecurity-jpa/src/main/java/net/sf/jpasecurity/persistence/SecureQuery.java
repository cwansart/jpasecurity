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
package net.sf.jpasecurity.persistence;

import static net.sf.jpasecurity.util.Types.isSimplePropertyType;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import net.sf.jpasecurity.entity.FetchManager;
import net.sf.jpasecurity.entity.SecureObjectManager;
import net.sf.jpasecurity.jpa.JpaParameter;
import net.sf.jpasecurity.jpa.JpaQuery;


/**
 * This class handles invocations on queries.
 * @author Arne Limburg
 */
public class SecureQuery<T> extends DelegatingQuery<T> {

    private SecureObjectManager objectManager;
    private FetchManager fetchManager;
    private List<String> selectedPaths;
    private FlushModeType flushMode;

    public SecureQuery(SecureObjectManager objectManager,
                       FetchManager fetchManager,
                       Query query,
                       List<String> selectedPaths,
                       FlushModeType flushMode) {
        super(query);
        this.objectManager = objectManager;
        this.fetchManager = fetchManager;
        this.selectedPaths = selectedPaths;
        this.flushMode = flushMode;
    }

    public TypedQuery<T> setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return super.setFlushMode(flushMode);
    }

    public TypedQuery<T> setParameter(int index, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), index, parameter);
        return this;
    }

    public TypedQuery<T> setParameter(String name, Object parameter) {
        objectManager.setParameter(new JpaQuery(getDelegate()), name, parameter);
        return this;
    }

    public <P> TypedQuery<T> setParameter(Parameter<P> parameter, P value) {
        objectManager.setParameter(new JpaQuery(getDelegate()), new JpaParameter<P>(parameter), value);
        return this;
    }

    public T getSingleResult() {
        preFlush();
        T result = getSecureResult(super.getSingleResult());
        postFlush();
        return result;
    }

    public List<T> getResultList() {
        preFlush();
        List<T> targetResult = super.getResultList();
        postFlush();
        List<T> proxyResult = new ArrayList<T>();
        for (T entity: targetResult) {
            proxyResult.add(getSecureResult(entity));
        }
        return proxyResult;
    }

    private void preFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.preFlush();
        }
    }

    private void postFlush() {
        if (flushMode == FlushModeType.AUTO) {
            objectManager.postFlush();
        }
    }

    private <R> R getSecureResult(R result) {
        if (result == null) {
            return null;
        }
        if (isSimplePropertyType(result.getClass())) {
            return result;
        }
        if (!(result instanceof Object[])) {
            result = objectManager.getSecureObject(result);
            fetchManager.fetch(result);
            return result;
        }
        Object[] scalarResult = (Object[])result;
        for (int i = 0; i < scalarResult.length; i++) {
            if (scalarResult[i] != null && !isSimplePropertyType(scalarResult[i].getClass())) {
                scalarResult[i] = objectManager.getSecureObject(scalarResult[i]);
                if (selectedPaths != null) {
                    fetchManager.fetch(scalarResult[i]);
                }
            }
        }
        return (R)scalarResult;
    }
}
