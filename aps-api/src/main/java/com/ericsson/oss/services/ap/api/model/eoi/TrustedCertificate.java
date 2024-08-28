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

;
import java.util.Collections;
import java.util.List;

public class TrustedCertificate {
    private String id;
    private String caSubjectName;
    private String caFingerprint;
    private String tdpsUri;
    private String caPem;
    private List<Crl> crls;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaSubjectName() {
        return caSubjectName;
    }

    public void setCaSubjectName(String caSubjectName) {
        this.caSubjectName = caSubjectName;
    }

    public String getCaFingerprint() {
        return caFingerprint;
    }

    public void setCaFingerprint(String caFingerprint) {
        this.caFingerprint = caFingerprint;
    }

    public String getTdpsUri() {
        return tdpsUri;
    }

    public void setTdpsUri(String tdpsUri) {
        this.tdpsUri = tdpsUri;
    }

    public String getCaPem() {
        return caPem;
    }

    public void setCaPem(String caPem) {
        this.caPem = caPem;
    }

    public List<Crl> getCrls() {
        return crls;
    }

    public void setCrls(List<Crl> crls) {
        this.crls = Collections.unmodifiableList(crls);
    }


    @Override
    public String toString() {
        return "TrustedCertificate{" +
            "id='" + id + '\'' +
            ", caSubjectName='" + caSubjectName + '\'' +
            ", caFingerprint='" + caFingerprint + '\'' +
            ", tdpsUri='" + tdpsUri + '\'' +
            ", caPem='" + caPem + '\'' +
            ", crls=" + crls +
            '}';
    }
}
