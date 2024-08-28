/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.email;

/**
 * The Email Utility class for Email parameters
 */
public final class EmailProperties {
    public static final String SMTPHOST = "emailrelay";
    public static final String NOREPLYEMAILADDRESS = "ENM Auto Provisioning <enmautoprovisioning@ericsson.com>";

    private EmailProperties() {
        throw new IllegalStateException("Utility class");
    }
}
