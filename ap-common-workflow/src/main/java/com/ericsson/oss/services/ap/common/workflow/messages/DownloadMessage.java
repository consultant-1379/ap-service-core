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
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Correlation message to download an artifact.
 */
public final class DownloadMessage {

    private static final String DOWNLOAD_MESSAGE_KEY = "DOWNLOAD";

    private DownloadMessage() {

    }

    /**
     * Correlation message key to download an artifact.
     *
     * @return the download message key
     */
    public static String getMessageKey() {
        return DOWNLOAD_MESSAGE_KEY;
    }
}
