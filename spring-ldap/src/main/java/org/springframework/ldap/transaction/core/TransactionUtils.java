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

package org.springframework.ldap.transaction.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.Name;
import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.transaction.compensating.CompensatingTransactionOperationManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Utility methods for working with LDAP transactions.
 * 
 * @author Mattias Arthursson
 * @since 1.2
 */
public final class TransactionUtils {

    private static final Log logger = LogFactory.getLog(TransactionUtils.class);

    public static final String REBIND_METHOD_NAME = "rebind";

    public static final String BIND_METHOD_NAME = "bind";

    public static final String RENAME_METHOD_NAME = "rename";

    public static final String UNBIND_METHOD_NAME = "unbind";

    public static final String MODIFY_ATTRIBUTES_METHOD_NAME = "modifyAttributes";

    /**
     * Not to be instantiated.
     */
    private TransactionUtils() {

    }

    /**
     * Close the given JNDI Context and ignore any thrown exception. This is
     * useful for typical <code>finally</code> blocks in JNDI code.
     * 
     * @param context
     *            the JNDI Context to close (may be <code>null</code>)
     */
    public static void closeContext(DirContext context) {
        if (context != null) {
            try {
                context.close();
            } catch (NamingException ex) {
                logger.debug("Could not close JNDI DirContext", ex);
            } catch (Throwable ex) {
                // We don't trust the JNDI provider: It might throw
                // RuntimeException or Error.
                logger.debug("Unexpected exception on closing JNDI DirContext",
                        ex);
            }
        }
    }

    /**
     * Get the first parameter in the argument list as a Name.
     * 
     * @param args
     *            arguments supplied to a ldap operation.
     * @return a Name representation of the first argument, or the Name itself
     *         if it is a name.
     */
    public static Name getFirstArgumentAsName(Object[] args) {
        Assert.notEmpty(args);

        Object firstArg = args[0];
        return getArgumentAsName(firstArg);
    }

    /**
     * Get the argument as a Name.
     * 
     * @param arg
     *            an argument supplied to an Ldap operation.
     * @return a Name representation of the argument, or the Name itself if it
     *         is a Name.
     */
    public static Name getArgumentAsName(Object arg) {
        if (arg instanceof String) {
            return new DistinguishedName((String) arg);
        } else if (arg instanceof Name) {
            return (Name) arg;
        } else {
            throw new IllegalArgumentException(
                    "First argument needs to be a Name or a String representation thereof");
        }
    }

    /**
     * Close the supplied context, but only if it is not associated with the
     * current transaction.
     * 
     * @param context
     *            the DirContext to close.
     * @param contextSource
     *            the ContextSource bound to the transaction.
     * @throws NamingException
     */
    public static void doCloseConnection(DirContext context,
            ContextSource contextSource) throws javax.naming.NamingException {
        DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSource);
        if (transactionContextHolder == null
                || transactionContextHolder.getCtx() != context) {
            logger.debug("Closing context");
            // This is not the transactional context or the transaction is
            // no longer active - we should close it.
            context.close();
        } else {
            logger.debug("Leaving transactional context open");
        }
    }

    /**
     * Check whether the supplied method is a method for which transactions is
     * supported (and which should be recorded for possible rollback later).
     * 
     * @param methodName
     *            name of the method to check.
     * @return <code>true</code> if this is a supported transaction operation,
     *         <code>false</code> otherwise.
     */
    public static boolean isSupportedWriteTransactionOperation(String methodName) {
        return (StringUtils.equals(methodName, BIND_METHOD_NAME)
                || StringUtils.equals(methodName, REBIND_METHOD_NAME)
                || StringUtils.equals(methodName, RENAME_METHOD_NAME)
                || StringUtils
                        .equals(methodName, MODIFY_ATTRIBUTES_METHOD_NAME) || StringUtils
                .equals(methodName, UNBIND_METHOD_NAME));

    }

    /**
     * Perform the specified operation, storing the state prior to the
     * operation, to enable commit/rollback later.
     * 
     * @param contextSource
     *            the ContextSource we are operating on.
     * @param targetContext TODO
     * @param method
     *            name of the method to be invoked.
     * @param args
     *            arguments with which the operation is invoked.
     */
    public static void performOperation(ContextSource contextSource,
            DirContext targetContext, Method method, Object[] args) throws Throwable {
        DirContextHolder transactionContextHolder = (DirContextHolder) TransactionSynchronizationManager
                .getResource(contextSource);
        if (transactionContextHolder != null) {

            CompensatingTransactionOperationManager transactionDataManager = transactionContextHolder
                    .getTransactionOperationManager();
            transactionDataManager.performOperation(method.getName(), args);
        } else {
            // Perform the target operation
            try {
                method.invoke(targetContext, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    /**
     * Get the actual class of the supplied DirContext instance; LdapContext or
     * DirContext.
     * 
     * @param context
     *            the DirContext instance to check.
     * @return LdapContext.class if context is an LdapContext, DirContext.class
     *         otherwise.
     */
    public static Class getActualTargetClass(DirContext context) {
        if (context instanceof LdapContext) {
            return LdapContext.class;
        }

        return DirContext.class;
    }

}
