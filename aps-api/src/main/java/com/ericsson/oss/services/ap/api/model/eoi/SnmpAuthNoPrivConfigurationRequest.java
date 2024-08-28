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

public class SnmpAuthNoPrivConfigurationRequest {
    private String authAlgo;
    private String authPriv;
    private String authPassword;

    public String getAuthAlgo() {
        return authAlgo;
    }

    public void setAuthAlgo(String authAlgo) {
        this.authAlgo = authAlgo;
    }

    public String getAuthPriv() {
        return authPriv;
    }

    public void setAuthPriv(String authPriv) {
        this.authPriv = authPriv;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    @Override
    public String toString() {
        return "{" +
            "authAlgo='" + authAlgo + '\'' +
            ", authPriv='" + authPriv + '\'' +
            '}';
    }
}
