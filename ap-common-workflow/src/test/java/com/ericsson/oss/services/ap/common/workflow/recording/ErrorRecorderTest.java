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
package com.ericsson.oss.services.ap.common.workflow.recording;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ErrorRecorderTest {

   @Mock
   SystemRecorder systemRecorder;

   @InjectMocks
   ErrorRecorder errorRecorder;

    @Test
    public void testEnableDisableSupervisionFailed() {
        doNothing().when(systemRecorder).recordError(Mockito.anyString(), Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        errorRecorder.updateSupervisionStatusFailed("Project=Project1,Node=Node1", "CM", new RuntimeException("Status updation Failed"), "DISABLE", "PRE_MIGRATION");
        verify(systemRecorder).recordError(Mockito.anyString(), Mockito.anyObject(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}