/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.test.util.assertions

import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute

/**
 * CommonAssertionsSpec is a utility class that contains commonly used assertions that
 * can be used by test classes, to prevent the duplication of code.
 */
class CommonAssertionsSpec {

    /**
     * Compares the expected vs the actual integration state of a node
     *
     * @param dps                   RuntimeConfigurableDps
     * @param fdn                   Node FDN to check the integration state of
     * @param nodeIntegrationState  The EXPECTED node integration state
     */
    def static assertIntegrationState(final dps, final String fdn, final String nodeIntegrationState ) {
        final ManagedObject nodeStatusMO = dps.build().getLiveBucket().findMoByFdn(fdn + "," + NODE_STATUS.toString() + "=1")
        final String apNodeState = nodeStatusMO.getAttribute(NodeStatusAttribute.STATE.toString())
        nodeIntegrationState == apNodeState
    }

    /**
     * Checks if an MO has been created
     *
     * @param dps                   RuntimeConfigurableDps
     * @param moFDN                 The expected created FDN
     */
    def static assertMoCreated(final dps, final String moFDN) {
        final ManagedObject managedObject = dps.build().getLiveBucket().findMoByFdn(moFDN)
        managedObject.getFdn().equals(moFDN)
    }

    /**
     * Checks if an attribute of an FDN is as expected
     *
     * @param dps                   RuntimeConfigurableDps
     * @param fdn                   Node FDN to check the attribute on
     * @param attribute             The attribute of the FDN to check
     * @param expectedValue         The EXPECTED string value of the attribute to be
     */
    def static assertAttributeForMo(final dps, final String fdn, final String attribute, final String expectedValue) {
        final ManagedObject managedObject = dps.build().getLiveBucket().findMoByFdn(fdn)
        final String actualValue = managedObject.getAttribute(attribute)
        expectedValue.equals(actualValue)
    }
}
