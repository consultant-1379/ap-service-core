/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.steps;

import javax.ejb.EJB;

import com.ericsson.oss.services.ap.api.AutoProvisioningService;

/**
 * Main test-step class, to be extended by all test-step classes for testing AutoProvisioningService EJB.
 */
public class ServiceCoreTestSteps {

    @EJB
    protected AutoProvisioningService service;
}
