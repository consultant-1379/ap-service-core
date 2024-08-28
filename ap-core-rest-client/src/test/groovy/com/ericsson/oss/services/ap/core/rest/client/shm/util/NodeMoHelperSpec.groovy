/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.shm.util

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec

import spock.lang.Subject

class NodeMoHelperSpec extends CdiSpecification {

    @Subject
    @Inject
    NodeMoHelper nodeMoHelper

    @Inject
    private DpsOperations dpsOperations

    private static final String NE_PRODUCT_VERSION = "neProductVersion";
    private static final String IDENTITY_VALUE="CXP9024418/15";
    private static final String REVISION_VALUE="R48A35"
    private static final String EXPECTED_SWVERSION="CXP9024418/15_R48A35"
    private static final String INVALID_NE_VERION = "invalid version"
    private static final String REVISION = "revision"
    private static final String IDENTITY = "identity"
    private static final String NODE_NAME = "Node1"
    private static final String NODE_TYPE = "RadioNode"

    private Map<String, Object> nodeAttributes = new HashMap<>()
    private final Map<String, Object> neProductVersionMap = new HashMap<>()
    private final List<Object> neProductVersionList = new ArrayList<>()
    private String swVersion = null;

    private RuntimeConfigurableDps dps

    void setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        MoCreatorSpec.setDps(dps)
        nodeAttributes.put("networkElementId", NODE_NAME)
        nodeAttributes.put("neType", NODE_TYPE)
        nodeAttributes.put("ossPrefix", "SubNetwork=Node1")
        nodeAttributes.put("ossModelIdentity", "22.Q1-R48A08")
        nodeAttributes.put("ipAddress", "22.22.22.22")
    }

    def "Valid neProductVersion in node MO then getswWithVersionFromNodeMO returns epected version"() {
        given: "Valid neProductVersion in node MO"
            neProductVersionMap.put(REVISION, REVISION_VALUE);
            neProductVersionMap.put(IDENTITY, IDENTITY_VALUE);
            neProductVersionList.add(neProductVersionMap)
            nodeAttributes.put(NE_PRODUCT_VERSION, neProductVersionList)
            MoCreatorSpec.createNetworkElementMoWithAtrributes(NODE_NAME, null,nodeAttributes)

        when: "getswWithVersionFromNodeMO is invoked"
            swVersion = nodeMoHelper.getSoftwareVersionFromMO(NODE_NAME)

        then: "Value is epected"
            swVersion == EXPECTED_SWVERSION
    }

    def "Invalid neProductVersion in node MO then getswWithVersionFromNodeMO returns null and thrown exception"() {
        given: "Valid neProductVersion in node MO"
            nodeAttributes.put(NE_PRODUCT_VERSION, INVALID_NE_VERION)
            MoCreatorSpec.createNetworkElementMoWithAtrributes(NODE_NAME, null,nodeAttributes)

        when: "getswWithVersionFromNodeMO is invoked"
            swVersion = nodeMoHelper.getSoftwareVersionFromMO(NODE_NAME)

        then: "exception thrown"
            ApServiceException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Attribute Value is not properly defined in MO: %1s , Attribute: %2s ",
                    "NetworkElement=" + NODE_NAME, NE_PRODUCT_VERSION));
    }

    def "Empty map in neProductVersion in node MO then getswWithVersionFromNodeMO returns null and thrown exception"() {
        given: "Valid neProductVersion in node MO"
            neProductVersionList.add(neProductVersionMap)
            nodeAttributes.put(NE_PRODUCT_VERSION, neProductVersionList)
            MoCreatorSpec.createNetworkElementMoWithAtrributes(NODE_NAME, null,nodeAttributes)

        when: "getswWithVersionFromNodeMO is invoked"
            swVersion = nodeMoHelper.getSoftwareVersionFromMO(NODE_NAME)

        then: "exception thrown"
            ApServiceException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Software Upgrade Package is not properly defined in MO: %1s , Attribute: %2s ",
                    "NetworkElement=" + NODE_NAME, NE_PRODUCT_VERSION));
    }

    def "Null in neProductVersion in node MO then getswWithVersionFromNodeMO returns null and thrown exception"() {
        given: "Valid neProductVersion in node MO"
            neProductVersionList.add(null)
            nodeAttributes.put(NE_PRODUCT_VERSION, neProductVersionList)
            MoCreatorSpec.createNetworkElementMoWithAtrributes(NODE_NAME, null,nodeAttributes)

        when: "getswWithVersionFromNodeMO is invoked"
            swVersion = nodeMoHelper.getSoftwareVersionFromMO(NODE_NAME)

        then: "exception thrown"
            ApServiceException exception = thrown()
            exception.getMessage().toString().startsWith(String.format("Software Upgrade Package is not properly defined in MO: %1s , Attribute: %2s ",
                    "NetworkElement=" + NODE_NAME, NE_PRODUCT_VERSION));
    }
}
