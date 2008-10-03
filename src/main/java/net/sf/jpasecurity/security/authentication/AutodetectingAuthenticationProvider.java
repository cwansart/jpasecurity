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
package net.sf.jpasecurity.security.authentication;

import java.util.Collection;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class tries to detect the authentication provider for an application.
 * The following authentication providers are used in the specified order:
 * <ol>
 *   <li>
 *     If a <tt>org.springframework.security.context.SecurityContextHolder</tt> is present in the classpath,
 *     a {@link SpringAuthenticationProvider} is used.
 *   </li>
 *   <li>
 *     If a <tt>org.acegisecurity.context.SecurityContextHolder</tt> is present in the classpath,
 *     a {@link AcegiAuthenticationProvider} is used.
 *   </li>
 *   <li>
 *     If an <tt>javax.ejb.EJBContext</tt> is accessible via JNDI lookup,
 *     an {@link EjbAuthenticationProvider} is used.
 *   </li>
 *   <li>
 *     If none of the former conditions is true, a {@link DefaultAuthenticationProvider} is used.
 *   </li>
 * </ol>
 * @author Arne Limburg
 */
public class AutodetectingAuthenticationProvider implements AuthenticationProvider {

    private static final String SPRING_CONTEXT_HOLDER_CLASS
        = "org.springframework.security.context.SecurityContextHolder";
    private static final String ACEGI_CONTEXT_HOLDER_CLASS
        = "org.acegisecurity.context.SecurityContextHolder";
    private static final Log LOG = LogFactory.getLog(AutodetectingAuthenticationProvider.class);

    private AuthenticationProvider authenticationProvider;

    public AutodetectingAuthenticationProvider() {
        authenticationProvider = autodetectAuthenticationProvider();
    }

    protected AuthenticationProvider autodetectAuthenticationProvider() {
        try {
            Class.forName(SPRING_CONTEXT_HOLDER_CLASS);
            LOG.info("autodetected presence of Spring Security, using SpringAuthenticationProvider");
            return new SpringAuthenticationProvider();
        } catch (ClassNotFoundException springSecurityNotFoundException) {
            try {
                Class.forName(ACEGI_CONTEXT_HOLDER_CLASS);
                LOG.info("autodetected presence of Acegi Security, using AcegiAuthenticationProvider");
                return new AcegiAuthenticationProvider();
            } catch (ClassNotFoundException acegiSecurityNotFoundException) {
                try {
                    InitialContext context = new InitialContext();
                    context.lookup("java:comp/EJBContext");
                    LOG.info("autodetected presence of EJB, using EJBAuthenticationProvider");
                    return new EjbAuthenticationProvider();
                } catch (NamingException ejbSecurityNotFoundException) {
                    LOG.info("falling back to DefaultAuthenticationPovider");
                    return new DefaultAuthenticationProvider();
                }
            }
        }
    }

    public Object getUser() {
        return authenticationProvider.getUser();
    }

    public Collection<?> getRoles() {
        return authenticationProvider.getRoles();
    }
}
