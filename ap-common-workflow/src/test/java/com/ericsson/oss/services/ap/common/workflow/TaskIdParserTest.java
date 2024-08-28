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
package com.ericsson.oss.services.ap.common.workflow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;

/**
 * Unit tests for {@link TaskIdParser}.
 */
public class TaskIdParserTest {

    private static final String ARTIFACT_TYPE = "RbsSummary";
    private static final String VALID_TASK_ID = "CreateFileArtifact__type_" + ARTIFACT_TYPE;

    @Test
    public void whenGettingArtifactTypeAndTaskIdContainsArtifactNameThenArtifactTypeIsReturned() {
        final String result = TaskIdParser.getArtifactType(VALID_TASK_ID);
        assertEquals(ARTIFACT_TYPE, result);
    }

    @Test(expected = ApApplicationException.class)
    public void whenGettingArtifactTypeAndTaskIdDoesNotHaveArtifactNameThenApApplicationExceptionIsThrown() {
        TaskIdParser.getArtifactType("CreateFileArtifact_" + ARTIFACT_TYPE);
    }
}
