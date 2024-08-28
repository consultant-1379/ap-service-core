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

public class Crl{
    private String cdpsUri;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public String getCdpsUri() {
        return cdpsUri;
    }

    public void setCdpsUri(String cdpsUri) {
        this.cdpsUri = cdpsUri;
    }

    @Override
    public String toString() {
        return "Crl{" +
            "cdpsUri='" + cdpsUri + '\'' +
            ", id='" + id + '\'' +
            '}';
    }
}
