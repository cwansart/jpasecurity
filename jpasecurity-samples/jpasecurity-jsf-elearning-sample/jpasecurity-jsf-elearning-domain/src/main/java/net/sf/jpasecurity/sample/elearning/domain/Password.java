/*
 * Copyright 2011 Arne Limburg - open knowledge GmbH
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
package net.sf.jpasecurity.sample.elearning.domain;

import static org.apache.commons.lang.Validate.notNull;

import javax.persistence.Embeddable;

/**
 * This is a value object that represents a password.
 *
 * @author Arne Limburg - open knowledge GmbH (arne.limburg@openknowledge.de)
 */
@Embeddable
public class Password {

    private String text;

    protected Password() {
        // to satisfy @Embeddable-contract
    }

    public Password(String text) {
        notNull(text, "text may not be null");
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return text;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Password)) {
            return false;
        }
        Password name = (Password)object;
        return getText().equals(name.getText());
    }

    public int hashCode() {
        return text.hashCode();
    }
}
