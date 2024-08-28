/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.log;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;

/**
 * Unit tests for {@link DdpTimer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DdpTimerTest {

    private static final String LOG_NAME = "logName";
    private static final String TOTAL_NODES_LOG_ENTRY = "TOTAL_NODE(S)=";
    private static final int NUMBER_OF_NODES = 3;

    @Mock
    private SystemRecorder recorder;

    @InjectMocks
    private DdpTimer ddpTimer;

    @Test
    public void whenTimerIsStartedAndEndsAfter50MillisecondsThenRecorderLogsTimeStampWithTwoDigits() throws InterruptedException {
        ddpTimer.start(LOG_NAME);
        Thread.sleep(50);
        ddpTimer.end(NODE_FDN);

        verify(recorder).recordCommand(anyString(), any(CommandPhase.class), anyString(), anyString(),
            argThat(new LogEntryTimeStampHasTwoDigits()));
    }

    @Test
    public void whenTimerIsStartedWithAGivenLogNameAndEndsThenRecorderLogsWithThatName() {
        ddpTimer.start(LOG_NAME);
        ddpTimer.end(NODE_FDN);

        verify(recorder).recordCommand(eq(LOG_NAME), eq(CommandPhase.FINISHED_WITH_SUCCESS), anyString(), anyString(), anyString());
    }

    @Test
    public void whenTimerIsStartedAndEndsWithAGivenFdnThenRecorderLogsFdnAndRdn() {
        ddpTimer.start(LOG_NAME);
        ddpTimer.end(NODE_FDN);

        verify(recorder).recordCommand(anyString(), eq(CommandPhase.FINISHED_WITH_SUCCESS), eq(NODE_NAME), eq(NODE_FDN), anyString());
    }

    @Test
    public void whenTimerIsStartedAndEndsWithNoNumberOfNodesSpecifiedThenRecorderLogsWithOnlyOneNode() {
        ddpTimer.start(LOG_NAME);
        ddpTimer.end(NODE_FDN);

        verify(recorder).recordCommand(anyString(), eq(CommandPhase.FINISHED_WITH_SUCCESS), anyString(), anyString(),
            endsWith(TOTAL_NODES_LOG_ENTRY + 1));
    }

    @Test
    public void whenTimerIsStartedAndEndsWithNumberOfNodesSpecifiedThenRecorderLogsWithThatNumberOfNodes() {
        ddpTimer.start(LOG_NAME);
        ddpTimer.end(NODE_FDN, NUMBER_OF_NODES);

        verify(recorder).recordCommand(anyString(), eq(CommandPhase.FINISHED_WITH_SUCCESS), anyString(), anyString(),
            endsWith(TOTAL_NODES_LOG_ENTRY + NUMBER_OF_NODES));
    }

    @Test
    public void whenTimerIsStartedAndEndsWithErrorThenRecorderLogsAsFinishedWithError() {
        ddpTimer.start(LOG_NAME);
        ddpTimer.endWithError(NODE_FDN, NUMBER_OF_NODES);

        verify(recorder).recordCommand(anyString(), eq(CommandPhase.FINISHED_WITH_ERROR), anyString(), anyString(), anyString());
    }

    private static class LogEntryTimeStampHasTwoDigits extends ArgumentMatcher<String> {

        @Override
        public boolean matches(final Object list) {
            final String logEntry = String.valueOf(list);
            final String timeStamp = logEntry.split("=")[1].split("\\s")[0];
            return timeStamp.length() == 2;
        }
    }
}
