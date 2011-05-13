/*
 * Copyright 2010 Arne Limburg
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import junit.framework.TestCase;
import net.sf.jpasecurity.model.MethodAccessAnnotationTestBean;
import net.sf.jpasecurity.model.acl.PrivilegeType;
import net.sf.jpasecurity.security.authentication.TestAuthenticationProvider;

/**
 * @author Arne Limburg
 */
public class QueryTest extends TestCase {

    public static final String USER1 = "user1";
    public static final String USER2 = "user2";

    public void testEmptyResult() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("xml-based-field-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from FieldAccessXmlTestBean bean");
        assertTrue(query instanceof EmptyResultQuery);
        assertEquals(0, query.getResultList().size());
    }

    public void testEnumParameter() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from Privilege bean WHERE bean.type=:TYPE");
        query.setParameter("TYPE", PrivilegeType.DATA);
        query.getResultList();
    }

    public void testEnumParameterList() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("acl-model");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select bean from Privilege bean WHERE bean.type in (:TYPES)");
        final ArrayList<PrivilegeType> types = new ArrayList<PrivilegeType>();
        types.add(PrivilegeType.DATA);
        types.add(PrivilegeType.METHOD);
        query.setParameter("TYPES", types);
        query.getResultList();
    }

    public void testScalarResult() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-method-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestAuthenticationProvider.authenticate("root", "admin");
        ParentChildTestData testData = new ParentChildTestData(entityManager);
        MethodAccessAnnotationTestBean child1 = testData.createPermutations(USER1, USER2).iterator().next();
        MethodAccessAnnotationTestBean parent1 = child1.getParent();
        entityManager.getTransaction().commit();
        entityManager.close();

        TestAuthenticationProvider.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Object[]> result
            = entityManager.createQuery("select bean.name, bean.parent from MethodAccessAnnotationTestBean bean")
                           .getResultList();
        assertEquals(1, result.size());
        assertEquals(USER1, result.get(0)[0]);
        assertEquals(parent1, result.get(0)[1]);
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
        TestAuthenticationProvider.authenticate(null);
    }

    public void testHibernateWithClause() {
        EntityManagerFactory entityManagerFactory
            = Persistence.createEntityManagerFactory("annotation-based-method-access");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        TestAuthenticationProvider.authenticate("root", "admin");
        ParentChildTestData testData = new ParentChildTestData(entityManager);
        MethodAccessAnnotationTestBean child1 = testData.createPermutations(USER1, USER2).iterator().next();
        entityManager.getTransaction().commit();
        entityManager.close();

        TestAuthenticationProvider.authenticate(USER1);
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<MethodAccessAnnotationTestBean> result
            = entityManager.createQuery("select bean from MethodAccessAnnotationTestBean bean "
                                        + "join bean.parent parent with parent.name = '" + USER1 + "' "
                                        + "where bean.name = :name")
                           .setParameter("name", USER1)
                           .getResultList();
        assertEquals(1, result.size());
        assertEquals(child1, result.iterator().next());
        entityManager.getTransaction().commit();
        entityManager.close();
        entityManagerFactory.close();
        TestAuthenticationProvider.authenticate(null);
    }
}