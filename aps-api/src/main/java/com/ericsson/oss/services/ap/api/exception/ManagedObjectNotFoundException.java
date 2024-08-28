/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.exception;

/**
 * Thrown to indicate that a required MO for the usecase does not exist.
 */
public class ManagedObjectNotFoundException extends ApApplicationException {

    private static final long serialVersionUID = 3035930036035758577L;

    private final String moType;
    private final String networkElementName;

    /**
     * Exception with MO type and the <code>NetworkElement</code> name
     *
     * @param moType
     *            the type of the MO
     * @param networkElementName
     *            the name of the <code>NetworkElement</code>
     */
    public ManagedObjectNotFoundException(final String moType, final String networkElementName) {
        this.moType = moType;
        this.networkElementName = networkElementName;
    }

    /**
     * Returns the name of <code>NetworkElement</code> which cannot be found.
     *
     * @return the name of the <code>NetworkElement</code>
     */
    public String getNetworkElementName() {
        return networkElementName;
    }

    /**
     * Returns the type of the MO which cannot be found.
     *
     * @return the type of the MO
     */
    public String getMoType() {
        return moType;
    }

}
