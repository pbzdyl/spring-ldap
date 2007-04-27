/*
 * Copyright 2005-2007 the original author or authors.
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

/**
 * Common interface for filters.
 * 
 * @author Adam Skogman
 */
public interface Filter {

    /**
     * Encodes the filter to a string using the (@link #encode(StringBuffer)
     * method.
     * 
     * @return The encoded filter
     */
    public String encode();

    /**
     * Prints the query with LDAP encoding to a stringbuffer
     * 
     * @param buff
     *            The stringbuffer
     * @return The very same stringbuffer
     */
    public StringBuffer encode(StringBuffer buff);

    /**
     * All filters must implement equals.
     * 
     * @param o
     * @return <code>true</code> if the objects are equal.
     */
    public boolean equals(Object o);

    /**
     * All filters must implement hashCode()
     * 
     * @return hascode
     */
    public int hashCode();

}