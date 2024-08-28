/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.model.profile;

/**
 * POJO model for CIQ object in {@link Profile}.
 */
public class Ciq {
    private String ciqLocation;

    public Ciq() {
    }

    public Ciq(final String ciqLocation) {
        this.ciqLocation = ciqLocation;
    }

    public String getCiqLocation() {
        return ciqLocation;
    }

    public void setCiqLocation(final String ciqLocation) {
        this.ciqLocation = ciqLocation;
    }
}
