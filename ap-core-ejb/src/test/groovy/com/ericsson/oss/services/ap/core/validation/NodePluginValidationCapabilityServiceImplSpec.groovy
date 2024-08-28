/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.validation

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.NodePluginCapabilityType;
import com.ericsson.oss.services.ap.api.NodePluginCapabilityVersion;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.NodePluginRestClient
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginCapability

public class NodePluginValidationCapabilityServiceImplSpec extends CdiSpecification {

    @ObjectUnderTest
    private final NodePluginValidationCapabilityServiceImpl nodePluginValidationCapabilityServiceImpl;

    @MockedImplementation
    private final NodePluginRestClient nodePluginRestClient;

    def "When get capability information from nodeplugin and specified capability is in the list expected value is returned for validateCapability"(){
        given: "valid capability information from nodeplugin"
              nodePluginRestClient.getCapabilities(_) >> buildvalidECTValidationResponse()
        when: "validateCapability is invoked"
              def returnValue = nodePluginValidationCapabilityServiceImpl.validateCapability("RadioNode", version, capability);
        then: "expected value is returned"
              returnValue == expectedReturnValue
        where:
        version                             | capability                                  | expectedReturnValue
        NodePluginCapabilityVersion.V1      | NodePluginCapabilityType.EDIT               | true
        NodePluginCapabilityVersion.V1      | NodePluginCapabilityType.VALIDATEDELTA      | true
        NodePluginCapabilityVersion.V0      | NodePluginCapabilityType.VALIDATE           | true
        NodePluginCapabilityVersion.V0      | NodePluginCapabilityType.VALIDATEDELTA      | false
        NodePluginCapabilityVersion.V0      | NodePluginCapabilityType.GENERATE           | false
    }

    def "When get null capability information from nodeplugin false is returned for validateCapability"(){
        given: "valid capability information from nodeplugin"
              nodePluginRestClient.getCapabilities(_) >> null
        when: "validateCapability is invoked"
              def returnValue = nodePluginValidationCapabilityServiceImpl.validateCapability("RadioNode", NodePluginCapabilityVersion.V1, NodePluginCapabilityType.VALIDATEDELTA);
        then: "false is returned"
              returnValue == false
    }

    List<NodePluginCapability> buildvalidECTValidationResponse(){
        final List<NodePluginCapability> capabilityList = new ArrayList<>();
        NodePluginCapabilityType[] capabilities = NodePluginCapabilityType.values();
        final List<String> capabilityForV1 = new ArrayList<>();
        for (NodePluginCapabilityType capability: capabilities){
            capabilityForV1.add(capability.toString());
        }
        final List<String> capabilityForV0 = [NodePluginCapabilityType.VALIDATE.toString()];
        final NodePluginCapability capability1 = new NodePluginCapability();
        final NodePluginCapability capability2 = new NodePluginCapability();
        capability1.setVersionOfInterface(NodePluginCapabilityVersion.V0.toString());
        capability1.setCapabilities(capabilityForV0);
        capabilityList.add(capability1);
        capability2.setVersionOfInterface(NodePluginCapabilityVersion.V1.toString());
        capability2.setCapabilities(capabilityForV1);
        capability2.setApplicationUri("configuration-generator");
        capabilityList.add(capability2);
        return capabilityList;
    }
}
