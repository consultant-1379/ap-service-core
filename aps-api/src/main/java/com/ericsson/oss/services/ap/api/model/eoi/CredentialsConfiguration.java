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

import java.util.Collections;
import java.util.List;

public class CredentialsConfiguration {

    private List<CredentialsData> credentialsList;



    public List<CredentialsData> getCredentialsList() {
        return credentialsList;
    }

    public void setCredentialsList(List<CredentialsData> credentialsList) {
        this.credentialsList = Collections.unmodifiableList(credentialsList);
    }

    @Override
    public String toString() {
        return "CredentialsConfiguration{" +
                " credentialsList=" + credentialsList +
                '}';
    }
}
