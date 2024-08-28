/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.common;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.wfs.task.impl.TaskExecutionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Unit tests for {@link AbstractAutoBindTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractAutoBindTaskTest {

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private TaskExecution execution;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @InjectMocks
    private final AutoBindTask autoBindTask = new AutoBindTask();

    @Mock
    ManagedObject nodeMO;

    @Mock
    TaskExecutionImpl taskExecutionImpl;

    private boolean isNodeBound = false;

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getNodeType()).thenReturn("RadioNode");
        isNodeBound = false;
    }

    @Test
    public void whenBindNodeAndHardwareSerialNumberIsValidThenBindOccurs() {
        when(workflowVariables.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        autoBindTask.executeTask(execution);
        assertTrue(isNodeBound);
    }

    @Test
    public void whenBindNodeAndEasyLateBindIsTrueThenBindOccurs() {
        System.setProperty("autoProvisioning_bind_nodeName", "true");
        when(workflowVariables.getNodeType()).thenReturn("RadioNode");
        when(workflowVariables.getApNodeFdn()).thenReturn("");
        autoBindTask.executeTask(execution);
        assertTrue(isNodeBound);
    }

    @Test
    public void whenEasyLateBindIsTrueAndHardwareSerialNumberIsBlankThenBindOccurs() {
        when(workflowVariables.getHardwareSerialNumber()).thenReturn("");
        when(workflowVariables.getNodeType()).thenReturn("RadioNode");
        System.setProperty("autoProvisioning_bind_nodeName", "true");
        autoBindTask.executeTask(execution);
        assertTrue(isNodeBound);
    }

    @Test
    public void whenEasyLateBindIsFalseAndHardwareSerialNumberIsBlankThenBindDoesNotOccur() {
        when(workflowVariables.getHardwareSerialNumber()).thenReturn("");
        System.setProperty("autoProvisioning_bind_nodeName", "false");
        autoBindTask.executeTask(execution);
        assertFalse(isNodeBound);
    }

    @Test
    public void whenMigrationBindFailsThenBpmnErrorIsThrown() {
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getHardwareSerialNumber()).thenReturn("ABC123456");
        when(workflowVariables.getApNodeFdn()).thenReturn("fail");
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(true);
        autoBindTask.executeTask(taskExecutionImpl);
        assertTrue(isNodeBound);
    }

    @Test
    public void whenMigrationBindSuccessfulAndHardwareSerialNumberIsNotBlank() {
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getHardwareSerialNumber()).thenReturn("ABC123456");
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(true);
        autoBindTask.executeTask(taskExecutionImpl);
        assertTrue(isNodeBound);
    }

    private final class AutoBindTask extends AbstractAutoBindTask {

        @Override
        protected void bindNode(final String apNodeFdn, final String hardwareSerialNumber, final boolean easyLateBind) {
            isNodeBound = true;

            if (null != apNodeFdn && apNodeFdn.contains("fail")) {
                throw new ApApplicationException("error");
            }
        }
    }
}
