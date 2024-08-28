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
package com.ericsson.oss.services.ap.core.rest.model.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the Body payload used in the Eoi create project endpoint
 */
public class EoiProjectRequest extends ProjectRequest {

    private String networkUsecaseType = "";

    private List<EoiNetworkElement> networkElements = new ArrayList<>();

    public void setNetworkUsecaseType(final String networkUsecaseType) {
        this.networkUsecaseType = networkUsecaseType;
    }

    public String getNetworkUsecaseType() {
        return networkUsecaseType;
    }

    public void setNetworkElements(final List<EoiNetworkElement> networkElements) {
        this.networkElements = Collections.unmodifiableList(networkElements);
    }

    public List<EoiNetworkElement> getNetworkElements() {
        return Collections.unmodifiableList(networkElements);
    }

}
