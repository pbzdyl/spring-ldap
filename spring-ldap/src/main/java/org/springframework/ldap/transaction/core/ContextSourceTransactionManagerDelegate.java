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
package org.springframework.ldap.transaction.core;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.transaction.DirContextHolder;
import org.springframework.ldap.transaction.TempEntryRenamingStrategy;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This delegate performs all the work for the
 * {@link ContextSourceTransactionManager}. The work is delegated in order to
 * be able to perform the exact same work for the LDAP part in
 * {@link ContextSourceAndDataSourceTransactionManager}.
 * 
 * @author Mattias Arthursson
 * @see ContextSourceTransactionManager
 * @see ContextSourceAndDataSourceTransactionManager
 */
public class ContextSourceTransactionManagerDelegate {
    private static Log log = LogFactory
            .getLog(ContextSourceTransactionManager.class);

    private ContextSource contextSource;

    private TempEntryRenamingStrategy renamingStrategy = new DefaultTempEntryRenamingStrategy();

    /**
     * Set the ContextSource to work on. Even though the actual ContextSource
     * sent to the LdapTemplate instance should be a
     * {@link TransactionAwareContextSourceProxy}, the one sent to this method
     * should be the target of that proxy. If it is not, the target will be
     * extracted and used instead.
     * 
     * @param contextSource
     *            the ContextSource to work on.
     */
    public void setContextSource(ContextSource contextSource) {
        if (contextSource instanceof TransactionAwareContextSourceProxy) {
            TransactionAwareContextSourceProxy proxy = (TransactionAwareContextSourceProxy) contextSource;
            this.contextSource = proxy.getTarget();
        } else {
            this.contextSource = contextSource;
        }
    }

    public ContextSource getContextSource() {
        return contextSource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doGetTransaction()
     */
    public Object doGetTransaction() throws TransactionException {
        DirContextHolder contextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(this.contextSource);
        ContextSourceTransactionObject txObject = new ContextSourceTransactionObject(
                contextHolder);
        return txObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doBegin(java.lang.Object,
     *      org.springframework.transaction.TransactionDefinition)
     */
    public void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;

        if (txObject.getContextHolder() == null) {
            DirContext newCtx = getContextSource().getReadOnlyContext();
            DirContextHolder contextHolder = new DirContextHolder(newCtx,
                    renamingStrategy);

            txObject.setContextHolder(contextHolder);
            TransactionSynchronizationManager.bindResource(getContextSource(),
                    contextHolder);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCommit(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    public void doCommit(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) status
                .getTransaction();
        txObject.getContextHolder().getTransactionOperationManager().commit();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doRollback(org.springframework.transaction.support.DefaultTransactionStatus)
     */
    public void doRollback(DefaultTransactionStatus status)
            throws TransactionException {
        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) status
                .getTransaction();
        txObject.getContextHolder().getTransactionOperationManager().rollback();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#doCleanupAfterCompletion(java.lang.Object)
     */
    public void doCleanupAfterCompletion(Object transaction) {
        log.debug("Cleaning stored ContextHolder");
        TransactionSynchronizationManager.unbindResource(contextSource);

        ContextSourceTransactionObject txObject = (ContextSourceTransactionObject) transaction;
        DirContext ctx = txObject.getContextHolder().getCtx();

        try {
            log.debug("Closing target context");
            ctx.close();
        } catch (NamingException e) {
            e.printStackTrace();
        }

        txObject.getContextHolder().clear();
    }

    /**
     * Set the {@link TempEntryRenamingStrategy} to be used when renaming
     * temporary entries in unbind and rebind operations. Default value is a
     * {@link DefaultTempEntryRenamingStrategy}.
     * 
     * @param renamingStrategy
     *            the {@link TempEntryRenamingStrategy} to use.
     */
    public void setRenamingStrategy(TempEntryRenamingStrategy renamingStrategy) {
        this.renamingStrategy = renamingStrategy;
    }

}
