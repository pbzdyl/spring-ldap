/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.samples.person.domain;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PersonValidator implements Validator {

    public boolean supports(Class clazz) {
        return clazz.equals(Person.class);
    }

    public void validate(Object obj, Errors errors) {
        Person person = (Person) obj;
        if (!StringUtils.hasText(person.getFullName())) {
            errors.reject("noName", "Please provide the full name");
        }
        if (!StringUtils.hasText(person.getCompany())) {
            errors.reject("noCompany", "Please provide the company");
        }
        if (!StringUtils.hasText(person.getCountry())) {
            errors.reject("noCountry", "Please provide the country");
        }
        if (!StringUtils.hasText(person.getPhone())) {
            errors.reject("noPhone", "Please provide the telephone number");
        }
    }
}