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

public class TrustCategory{
    private String id;
    private List<String> certificates;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getCertificates() {
        return certificates;
    }

    public void setCertificates(List<String> certificates) {
        this.certificates = Collections.unmodifiableList(certificates);
    }

    @Override
    public String toString() {
        return "TrustCategory{" +
            "id='" + id + '\'' +
            ", certificates=" + certificates +
            '}';
    }
}
