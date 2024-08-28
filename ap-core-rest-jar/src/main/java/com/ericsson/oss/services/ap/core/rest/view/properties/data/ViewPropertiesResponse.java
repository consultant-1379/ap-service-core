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
package com.ericsson.oss.services.ap.core.rest.view.properties.data;

import java.util.List;

/**
 * Response object containing a list of {@link ViewProperties} objects containing node properties information
 */
public class ViewPropertiesResponse {

    private final List<ViewProperties> viewProperties;

    public ViewPropertiesResponse(final List<ViewProperties> viewProperties) {
        this.viewProperties = viewProperties;
    }

    public List<ViewProperties> getViewProperties() {
        return viewProperties;
    }

}
