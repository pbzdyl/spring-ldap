/*
 * Copyright 2002-2007 the original author or authors.
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
package org.springframework.ldap.support.transaction;

/**
 * A {@link CompensatingTransactionRecordingOperation} performing nothing,
 * returning a {@link NullRollbackOperation} regardless of the input. Instances
 * of this class will be created if the
 * {@link CompensatingTransactionDataManager} cannot determine any appropriate
 * {@link CompensatingTransactionRecordingOperation} for the current operation.
 * 
 * @author Mattias Arthursson
 * 
 */
public class NullRecordingOperation implements
        CompensatingTransactionRecordingOperation {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ldap.support.transaction.CompensatingTransactionRecordingOperation#recordOperation(java.lang.Object[])
     */
    public CompensatingTransactionRollbackOperation recordOperation(
            Object[] args) {
        return new NullRollbackOperation();
    }

}
