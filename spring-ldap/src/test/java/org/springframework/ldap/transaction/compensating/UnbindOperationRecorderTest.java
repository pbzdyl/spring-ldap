package org.springframework.ldap.transaction.compensating;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.transaction.compensating.TempEntryRenamingStrategy;
import org.springframework.ldap.transaction.compensating.UnbindOperationExecutor;
import org.springframework.ldap.transaction.compensating.UnbindOperationRecorder;
import org.springframework.transaction.compensating.CompensatingTransactionOperationExecutor;

public class UnbindOperationRecorderTest extends TestCase {
    private MockControl ldapOperationsControl;

    private LdapOperations ldapOperationsMock;

    private MockControl renamingStrategyControl;

    private TempEntryRenamingStrategy renamingStrategyMock;

    protected void setUp() throws Exception {
        ldapOperationsControl = MockControl.createControl(LdapOperations.class);
        ldapOperationsMock = (LdapOperations) ldapOperationsControl.getMock();

        renamingStrategyControl = MockControl
                .createControl(TempEntryRenamingStrategy.class);
        renamingStrategyMock = (TempEntryRenamingStrategy) renamingStrategyControl
                .getMock();

    }

    protected void tearDown() throws Exception {
        ldapOperationsControl = null;
        ldapOperationsMock = null;

        renamingStrategyControl = null;
        renamingStrategyMock = null;

    }

    protected void replay() {
        ldapOperationsControl.replay();
        renamingStrategyControl.replay();
    }

    protected void verify() {
        ldapOperationsControl.verify();
        renamingStrategyControl.verify();
    }

    public void testRecordOperation() {
        final DistinguishedName expectedTempName = new DistinguishedName(
                "cn=john doe_temp");
        final DistinguishedName expectedDn = new DistinguishedName(
                "cn=john doe");
        UnbindOperationRecorder tested = new UnbindOperationRecorder(
                ldapOperationsMock, renamingStrategyMock);

        renamingStrategyControl.expectAndReturn(renamingStrategyMock
                .getTemporaryName(expectedDn), expectedTempName);

        replay();
        // Perform test
        CompensatingTransactionOperationExecutor operation = tested
                .recordOperation(new Object[] { expectedDn });
        verify();

        // Verify result
        assertTrue(operation instanceof UnbindOperationExecutor);
        UnbindOperationExecutor rollbackOperation = (UnbindOperationExecutor) operation;
        assertSame(ldapOperationsMock, rollbackOperation.getLdapOperations());
        assertSame(expectedDn, rollbackOperation.getOriginalDn());
        assertSame(expectedTempName, rollbackOperation.getTemporaryDn());
    }

}
