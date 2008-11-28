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
package org.springframework.samples.petclinic;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

/**
 * @author Arne Limburg
 */
public class Credential extends BaseEntity implements UserDetails {

    private static final GrantedAuthority[] USER_AUTHORITIES = {new GrantedAuthorityImpl("ROLE_USER")};
    
    private String username;
    private String password;
    private Person user;
    
    public String getUsername() {
        return username;
    }
    
    protected void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    
    protected void setPassword(String password) {
        this.password = password;
    }
    
    public Person getUser() {
        return user;
    }
    
    protected void setUser(Person user) {
        this.user = user;
    }

    public GrantedAuthority[] getAuthorities() {
        return USER_AUTHORITIES;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }
}