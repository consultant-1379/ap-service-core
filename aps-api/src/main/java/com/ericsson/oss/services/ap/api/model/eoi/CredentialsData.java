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
package com.ericsson.oss.services.ap.api.model.eoi;

public class CredentialsData {

    private String credentialsType;
    private String credUser;
    private String credPass;

    public String getCredentialsType() {
        return credentialsType;
    }

    public void setCredentialsType(String credentialsType) {
        this.credentialsType = credentialsType;
    }

    public String getCredUser() {
        return credUser;
    }

    public void setCredUser(String credUser) {
        this.credUser = credUser;
    }

    public String getCredPass() {
        return credPass;
    }

    public void setCredPass(String credPass) {
        this.credPass = credPass;
    }

    @Override
    public String toString() {
        return "CredentialsData [credentialsType=" + credentialsType + ", credUser=" + credUser + "]";
    }
}
