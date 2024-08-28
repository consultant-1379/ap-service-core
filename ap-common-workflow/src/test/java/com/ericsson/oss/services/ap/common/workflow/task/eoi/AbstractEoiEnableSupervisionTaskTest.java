/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.eoi;

import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AbstractEoiEnableSupervisionTask}.
 */

@RunWith(MockitoJUnitRunner.class)

public class AbstractEoiEnableSupervisionTaskTest {
    private boolean fmSupervisionIsEnabled;
    private boolean pmSupervisionIsEnabled;
    private boolean inventorySupervisionIsEnabled;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private TaskExecution execution;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @InjectMocks
    private final EoiEnableSupervisionTask eoiEnableSupervisionTask = new EoiEnableSupervisionTask();

    @InjectMocks
    private final FailingFmEnableSupervisionTask failingFmEnableSupervisionTask = new FailingFmEnableSupervisionTask();

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
    }

    @Test
    public void whenExecutingTaskAndNoSupervisionIsSetToBeEnabledThenNoSupervisionIsEnabled() {
        eoiEnableSupervisionTask.executeTask(execution);
        assertFalse("FM supervision was enabled when it should be disabled", fmSupervisionIsEnabled);
        assertFalse("PM supervision was enabled when it should be disabled", pmSupervisionIsEnabled);
    }

    @Test
    public void whenExecutingTaskAndFmSupervisionIsSetToBeEnabledThenFmSupervisionIsEnabled() {
        when(workflowVariables.isEnableSupervision(SupervisionMoType.FM)).thenReturn(true);

        eoiEnableSupervisionTask.executeTask(execution);

        assertTrue("FM supervision was not enabled", fmSupervisionIsEnabled);
        assertFalse("PM supervision was enabled when it should be disabled", pmSupervisionIsEnabled);
        assertFalse("Inventory supervision was enabled when it should be disabled", inventorySupervisionIsEnabled);
    }

    @Test
    public void whenExecutingTaskAndSupervisionFailsToBeEnabledThenIntegrationIsSetToWarningAndNoExceptionIsPropagatedFroMigrationNode() {
        when(workflowVariables.isEnableSupervision(SupervisionMoType.FM)).thenReturn(true);
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(true);
        failingFmEnableSupervisionTask.executeTask(execution);
        verify(workflowVariables).setMigrationTaskWarning(true);
    }

    @Test
    public void whenExecutingTaskAndSupervisionFailsToBeEnabledThenIntegrationIsSetToWarningAndNoExceptionIsPropagated() {
        when(workflowVariables.isEnableSupervision(SupervisionMoType.FM)).thenReturn(true);
        failingFmEnableSupervisionTask.executeTask(execution);
        verify(workflowVariables).setIntegrationTaskWarning(true);
    }

    @Test
    public void whenExecutingTaskAndFmAndPmSupervisionIsSetToBeEnabledAndFmSupervisionFailsThenPmSupervisionIsEnabled() {
        when(workflowVariables.isEnableSupervision(SupervisionMoType.FM)).thenReturn(true);
        when(workflowVariables.isEnableSupervision(SupervisionMoType.PM)).thenReturn(true);

        failingFmEnableSupervisionTask.executeTask(execution);

        verify(workflowVariables).setIntegrationTaskWarning(true);
        assertFalse("FM supervision was enabled when it should be disabled", fmSupervisionIsEnabled);
        assertTrue("PM supervision was not enabled", pmSupervisionIsEnabled);
        assertFalse("Inventory supervision was enabled when it should be disabled", inventorySupervisionIsEnabled);

    }

    private final class EoiEnableSupervisionTask extends AbstractEoiEnableSupervisionTask {

        @Override
        protected void eoiEnableSupervision(String apNodeFdn, SupervisionMoType supervisionToEnable) {
            enableSupervisionsForTest(supervisionToEnable);
        }
    }

    private final class FailingFmEnableSupervisionTask extends AbstractEoiEnableSupervisionTask {


        @Override
        protected void eoiEnableSupervision(String apNodeFdn, SupervisionMoType supervisionToEnable) {
            if (SupervisionMoType.FM == supervisionToEnable) {
                throw new IllegalStateException("Error message");
            }

            enableSupervisionsForTest(supervisionToEnable);

        }
    }
    private void enableSupervisionsForTest(final SupervisionMoType supervisionToEnable) {
        fmSupervisionIsEnabled = (SupervisionMoType.FM == supervisionToEnable);
        pmSupervisionIsEnabled = (SupervisionMoType.PM == supervisionToEnable);
    }

}
