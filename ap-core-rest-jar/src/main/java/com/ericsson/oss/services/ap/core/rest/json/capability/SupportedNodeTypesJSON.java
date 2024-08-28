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
package com.ericsson.oss.services.ap.core.rest.json.capability;

import java.util.List;

/**
 * POJO model for representing the capability of a ConfigurationProfile - supported node types.
 */
public class SupportedNodeTypesJSON {

    private List<String> supportedNodeTypes;

    public SupportedNodeTypesJSON(final List<String> supportedNodeTypes) {
        this.supportedNodeTypes = supportedNodeTypes;
    }

    public List<String> getSupportedNodeTypes() {
        return supportedNodeTypes;
    }

    public void setSupportedNodeTypes(final List<String> supportedNodeTypes) {
        this.supportedNodeTypes = supportedNodeTypes;
    }
}
