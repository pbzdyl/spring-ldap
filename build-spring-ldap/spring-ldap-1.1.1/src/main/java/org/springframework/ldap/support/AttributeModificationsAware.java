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

package org.springframework.ldap.support;

import javax.naming.directory.ModificationItem;

/**
 * Indicates that the implementor is capable of keeping track of any attribute
 * modifications and return them as ModificationItems.
 * 
 * @author Mattias Arthursson
 * 
 */
public interface AttributeModificationsAware {

    /**
     * Creates an array of which attributes have been changed or added or removed.
     * 
     * @return an array of modification items
     */
    public ModificationItem[] getModificationItems();
}
