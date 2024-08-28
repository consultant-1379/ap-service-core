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

public class CredentialsConfigurationResponse {

    private List<String> credentials;

    public List<String> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<String> credentials) {
        this.credentials = Collections.unmodifiableList(credentials);
    }

    @Override
    public String toString() {
        return "CredentialsConfigurationResponse{" +
                "credentials=" + credentials +
                '}';
    }
}
