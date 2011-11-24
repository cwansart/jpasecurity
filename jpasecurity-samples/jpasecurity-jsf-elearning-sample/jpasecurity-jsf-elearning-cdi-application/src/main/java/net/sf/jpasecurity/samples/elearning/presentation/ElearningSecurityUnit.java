/*
 * Copyright 2011 Arne Limburg
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
package net.sf.jpasecurity.samples.elearning.presentation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import net.sf.jpasecurity.AccessManager;
import net.sf.jpasecurity.sample.elearning.domain.Name;
import net.sf.jpasecurity.sample.elearning.domain.User;
import net.sf.jpasecurity.sample.elearning.domain.UserRepository;

/**
 * @author Arne Limburg
 */
@ApplicationScoped
public class ElearningSecurityUnit {

    @Inject
    private UserRepository userRepository;

    @Produces
    @RequestScoped
    @Named("accessManager")
    public AccessManager createAccessManager(EntityManager entityManager) {
        return entityManager.unwrap(AccessManager.class);
    }

    @Produces
    public User getCurrentUser() {
        Name userName = new Name(FacesContext.getCurrentInstance().getExternalContext().getRemoteUser());
        return userRepository.findUser(userName);
    }
}