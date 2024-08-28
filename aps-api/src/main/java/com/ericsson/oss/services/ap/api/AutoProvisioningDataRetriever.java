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
package com.ericsson.oss.services.ap.api;

import java.util.List;

/**
 * Interface used when retrieving data for Auto Provisioning usecases
 */
public interface AutoProvisioningDataRetriever {

    /**
     * Get supported node types
     * 
     * @return {@link List} of supported node types by Profile Management
     */
    List<String> getSupportedNodeTypes();
}
