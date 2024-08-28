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
package com.ericsson.oss.services.ap.api.workflow;

/**
 Interface that contains validations to be performed for the configuration files during the order.
 */
public interface ValidationConfigurationService {

    /**
     * Given the data to be validated : Product Number, Revision and Configuration Files (siteBasic.xml, siteEquipment.xml) the service validates
     * if the provided data is correct.
     * @param apNodeFdn the input data to be used to construct validate request
     * @param nodeType the input data to be used to construct validate request
     * @return a string as an output containing the validation warnings / failures.
     */
    String validateConfiguration(final String apNodeFdn, final String nodeType);

    /**
     * Given the data to be validated : Product Number, Revision, Preconfiguration File and Configuration Files the service validates
     * if the provided data is correct.
     * @param apNodeFdn the input data to be used to construct validate request
     * @param nodeType the input data to be used to construct validate request
     * @return a string as an output containing the validation warnings / failures.
     */
    String validateDeltaConfiguration(final String apNodeFdn, final String nodeType);

}
