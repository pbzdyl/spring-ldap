/*
 * Copyright 2005-2008 the original author or authors.
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

package org.springframework.ldap.filter;

import org.springframework.ldap.filter.LessThanOrEqualsFilter;

import junit.framework.TestCase;

/**
 * @author Mattias Hellborg Arthursson
 */
public class LessThanOrEqualsFilterTest extends TestCase {

    /**
     * Constructor for EqualsQueryTest.
     * 
     * @param name
     */
    public LessThanOrEqualsFilterTest(String name) {
        super(name);
    }

    public void testEncode() {

        LessThanOrEqualsFilter eqq = new LessThanOrEqualsFilter("foo",
                "*bar(fie)");

        StringBuffer buff = new StringBuffer();
        eqq.encode(buff);

        assertEquals("(foo<=\\2abar\\28fie\\29)", buff.toString());

    }

    public void testEncodeInt() {

        LessThanOrEqualsFilter eqq = new LessThanOrEqualsFilter("foo", 456);

        StringBuffer buff = new StringBuffer();
        eqq.encode(buff);

        assertEquals("(foo<=456)", buff.toString());

    }

}
