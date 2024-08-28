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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;

/**
 * Class used to parse the task ID of a BPMN task.
 */
public final class TaskIdParser {

    private static final String TASK_ID_CONTAINS_TYPE_PATTERN = "(.+)__type_(.+)";

    private TaskIdParser() {

    }

    /**
     * Extracts the artifact type from the supplied BPMN service task ID.
     * <p>
     * Expects the task ID to be in the format <code>{@literal <}serviceTaskName{@literal >}__type_{@literal <}artifactType{@literal >}</code>
     *
     * @param taskId
     *            the task ID to parse
     * @return the artifact type
     */
    public static String getArtifactType(final String taskId) {
        final Pattern pattern = Pattern.compile(TASK_ID_CONTAINS_TYPE_PATTERN);
        final Matcher matcher = pattern.matcher(taskId);

        if (!matcher.matches()) {
            throw new ApApplicationException("Artifact type missing from task ID: " + taskId);
        }
        return matcher.group(2);
    }
}
