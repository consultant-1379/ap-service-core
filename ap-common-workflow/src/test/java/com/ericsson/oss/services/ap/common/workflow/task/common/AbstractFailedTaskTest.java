/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.common;

import org.mockito.Mock;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.recording.CommandRecorder;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Unit tests for {@link AbstractFailedTaskTest}.
 */
public class AbstractFailedTaskTest {

    @Mock
    protected AbstractWorkflowVariables workflowVariables;

    @Mock
    protected CommandRecorder commandRecorder;

    @Mock
    protected DpsOperations dpsOperations;

    @Mock
    protected ServiceFinderBean serviceFinder;

    @Mock
    protected TaskExecution execution;

    @Mock
    protected StateTransitionManagerLocal stateTransitionManager;

}
