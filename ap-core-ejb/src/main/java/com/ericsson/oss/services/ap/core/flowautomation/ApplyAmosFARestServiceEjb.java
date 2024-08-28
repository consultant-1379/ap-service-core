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
package com.ericsson.oss.services.ap.core.flowautomation;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAExecutionResult;
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult;
import com.ericsson.oss.services.ap.api.workflow.ApplyAmosFARestService;
import com.ericsson.oss.services.ap.core.rest.client.flowautomation.ApplyAmosFARestClient;
/**
 * Execute the Apply AMOS Script flow and get the report from it.
 */
@EService
@Stateless
public class ApplyAmosFARestServiceEjb implements ApplyAmosFARestService {

    @Inject
    private ApplyAmosFARestClient faClient;

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplyAmosFAExecutionResult execute(final String userName, final String nodeName, final String amosScriptName, final String amosScriptContents, final boolean ignoreError) {
        final String executionName = generateExecutionName(nodeName, amosScriptName);
        return faClient.execute(userName, executionName, nodeName, amosScriptName, amosScriptContents, ignoreError);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplyAmosFAReportResult report(final String userName, final String executionName) {
        return faClient.report(userName, executionName);
    }

    private String generateExecutionName(final String nodeName, final String amosScriptName) {
        final int maxNameLength = 128;
        final String namePrefix = "PBC-";
        final String nameSuffix = "-" + System.currentTimeMillis();
        final String fileNameWithoutExtension = amosScriptName.split("\\.")[0];

        final int maxLengthLeft = maxNameLength - namePrefix.length() - nameSuffix.length();
        String nameBody = String.format("%s-%s", nodeName, fileNameWithoutExtension);
        nameBody = StringUtils.substring(nameBody, 0, maxLengthLeft);

        return String.format("%s%s%s", namePrefix, nameBody, nameSuffix);
    }
}
