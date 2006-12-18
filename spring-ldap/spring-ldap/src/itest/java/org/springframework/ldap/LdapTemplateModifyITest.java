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

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;


import org.springframework.dao.DataAccessException;
import org.springframework.ldap.LdapTemplate;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.support.DirContextAdapter;
import org.springframework.ldap.support.DistinguishedName;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the modification methods (rebind and modifyAttributes) of LdapTemplate.
 * It also illustrates the use of DirContextAdapter as a means of getting
 * ModificationItems, in order to avoid doing a full rebind and use
 * modifyAttributes() instead. We rely on that the bind, unbind and lookup
 * methods work as they should - that should be ok, since that is verified in a
 * separate test class. NOTE: if any of the tests in this class fails, it may be
 * necessary to run the cleanup script as described in README.txt under
 * /src/iutest/.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class LdapTemplateModifyITest extends
        AbstractDependencyInjectionSpringContextTests {
    private LdapTemplate tested;

    private static String DN = "cn=Some Person4,ou=company1,c=Sweden,dc=jayway,dc=se";

    protected String[] getConfigLocations() {
        return new String[] { "/conf/ldapTemplateTestContext.xml" };
    }

    protected void onSetUp() throws Exception {
        DirContextAdapter adapter = new DirContextAdapter();
        adapter.setAttributeValues("objectclass", new String[] { "top",
                "person" });
        adapter.setAttributeValue("cn", "Some Person4");
        adapter.setAttributeValue("sn", "Person4");
        adapter.setAttributeValue("description", "Some description");

        tested.bind(DN, adapter, null);
    }

    protected void onTearDown() throws Exception {
        tested.unbind(DN);
    }

    public void testRebind_Attributes_Plain() {
        Attributes attributes = setupAttributes();

        tested.rebind(DN, null, attributes);

        verifyBoundCorrectData();
    }

    public void testRebind_Attributes_DistinguishedName() {
        Attributes attributes = setupAttributes();

        tested.rebind(new DistinguishedName(DN), null, attributes);

        verifyBoundCorrectData();
    }

    public void testModifyAttributes_MultiValueReplace() {
        BasicAttribute attr = new BasicAttribute("description",
                "Some other description");
        attr.add("Another description");
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);

        tested.modifyAttributes(DN, mods);

        DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
        String[] attributes = result.getStringAttributes("description");
        assertEquals(2, attributes.length);
        assertEquals("Some other description", attributes[0]);
        assertEquals("Another description", attributes[1]);
    }

    public void testModifyAttributes_MultiValueAdd() {
        BasicAttribute attr = new BasicAttribute("description",
                "Some other description");
        attr.add("Another description");
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

        tested.modifyAttributes(DN, mods);

        DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
        String[] attributes = result.getStringAttributes("description");
        assertEquals(3, attributes.length);
        assertEquals("Some description", attributes[0]);
        assertEquals("Some other description", attributes[1]);
        assertEquals("Another description", attributes[2]);
    }

    public void testModifyAttributes_MultiValueAddDuplicateToUnordered() {
        BasicAttribute attr = new BasicAttribute("description",
                "Some description");
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

        try {
            tested.modifyAttributes(DN, mods);
            fail("UncategorizedLdapException expected");
        } catch (UncategorizedLdapException expected) {
            // expected
        }
    }

    /**
     * Test written originally to verify that duplicates are allowed on ordered
     * attributes, but had to be changed since Apache DS seems to disallow
     * duplicates even for ordered attributes.
     */
    public void testModifyAttributes_MultiValueAddDuplicateToOrdered() {
        BasicAttribute attr = new BasicAttribute("description",
                "Some other description", true); // ordered
        attr.add("Another description");
        // Commented out duplicate to make test work for Apache DS
        //attr.add("Some description");
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);

        tested.modifyAttributes(DN, mods);

        DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
        String[] attributes = result.getStringAttributes("description");
        assertEquals(3, attributes.length);
        assertEquals("Some description", attributes[0]);
        assertEquals("Some other description", attributes[1]);
        assertEquals("Another description", attributes[2]);
    }

    public void testModifyAttributes_Plain() {
        ModificationItem item = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description",
                        "Some other description"));

        tested.modifyAttributes(DN, new ModificationItem[] { item });

        verifyBoundCorrectData();
    }

    public void testModifyAttributes_DistinguishedName() {
        ModificationItem item = new ModificationItem(
                DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("description",
                        "Some other description"));

        tested.modifyAttributes(new DistinguishedName(DN),
                new ModificationItem[] { item });

        verifyBoundCorrectData();
    }

    /**
     * Demonstrates how the DirContextAdapter can be used to automatically keep
     * track of changes of the attributes and deliver ModificationItems to use
     * in moifyAttributes().
     * 
     * @throws NamingException
     * @throws DataAccessException
     */
    public void testModifyAttributes_DirContextAdapter()
            throws DataAccessException, NamingException {
        DirContextAdapter adapter = (DirContextAdapter) tested.lookup(DN);

        adapter.setAttributeValue("description", "Some other description");

        ModificationItem[] modificationItems = adapter.getModificationItems();
        tested.modifyAttributes(DN, modificationItems);

        verifyBoundCorrectData();
    }

    private Attributes setupAttributes() {
        Attributes attributes = new BasicAttributes();
        BasicAttribute ocattr = new BasicAttribute("objectclass");
        ocattr.add("top");
        ocattr.add("person");
        attributes.put(ocattr);
        attributes.put("cn", "Some Person4");
        attributes.put("sn", "Person4");
        attributes.put("description", "Some other description");
        return attributes;
    }

    private void verifyBoundCorrectData() {
        DirContextAdapter result = (DirContextAdapter) tested.lookup(DN);
        assertEquals("Some Person4", result.getStringAttribute("cn"));
        assertEquals("Person4", result.getStringAttribute("sn"));
        assertEquals("Some other description", result
                .getStringAttribute("description"));
    }

    public void setTested(LdapTemplate tested) {
        this.tested = tested;
    }
}
