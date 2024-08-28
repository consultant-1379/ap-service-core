/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.workflow;

import com.ericsson.oss.services.ap.api.model.ApplyAmosFAExecutionResult;
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult;

/**
 * Interface to Flow Automation Service which allow to execute the Apply AMOS Script flow
 * and get the report from it.
 */
public interface ApplyAmosFARestService {

    /**
     * Send request to Flow Automation Service to execute the Apply AMOS Script flow
     *
     * @param userName
     *            the name of user
     * @param nodeName
     *            the name of node where the AMOS script to be applied
     * @param amosScriptName
     *            the name of the AMOS script to be applied
     * @param amosScriptContents
     *            the content of AMOS script to be applied
     * @param ignoreError
     *            if import errors for AMOS script is ignored
     *
     * @return the flow execution result
     *
     */
    ApplyAmosFAExecutionResult execute(final String userName, final String nodeName, final String amosScriptName, final String amosScriptContents, final boolean ignoreError);

    /**
     * Send request to Flow Automation Service to get the report of Apply AMOS Script flow
     *
     * @param userName
     *            the name of user
     * @param flowName
     *            the name of the flow instance
     *
     * @return the flow report summary
     */
    ApplyAmosFAReportResult report(final String userName, final String flowName);
}
