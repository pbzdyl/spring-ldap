/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ldap;

import java.util.List;


import org.springframework.ldap.ContextMapper;
import org.springframework.ldap.LdapTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the ContextMapper search method. In its way this method also
 * demonstrates the use of DirContextAdapter and the DirObjectFactory.
 * 
 * @author Mattias Arthursson
 */
public class LdapTemplateContextMapperITest extends
        AbstractDependencyInjectionSpringContextTests {
    private LdapTemplate tested;

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    /**
     * This method depends on a DirObjectFactory ({@link org.springframework.ldap.support.DefaultDirObjectFactory})
     * being set in the ContextSource.
     */
    public void testSearch_ContextMapper() {
        ContextMapper mapper = new PersonContextMapper();
        List result = tested.search("ou=company1,c=Sweden,dc=jayway,dc=se",
                "(&(objectclass=person)(sn=Person2))", mapper);

        assertEquals(1, result.size());
        Person person = (Person) result.get(0);
        assertEquals("Some Person2", person.getFullname());
        assertEquals("Person2", person.getLastname());
        assertEquals("Sweden, Company1, Some Person2", person.getDescription());
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
