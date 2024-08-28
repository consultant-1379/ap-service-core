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

package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_BSC
import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_RNC
import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateControllingNodesExistSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateControllingNodesExist ValidateControllingNodesExist

    @Inject
    private DpsQueries dpsQueries

    private ValidationContext validationContext

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String BSC_NODE_NAME = "bsc"
    private static final String RNC_NODE_NAME = "rnc"
    private static final String BAD_BSC_NODE_NAME = "nonExistBsc"
    private static final String BAD_RNC_NODE_NAME = "nonExistRnc"

    private static final String NETWORK_ELEMENT_PREFIX = "NetworkElement=%s";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        addNetworkElementMo(BSC_NODE_NAME)
        addNetworkElementMo(RNC_NODE_NAME)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>()
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip")
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive)
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList)
    }

    def "Validate controlling Nodes exist in ENM" () {
        given: "Validation Context and mock nodeInfo object with controllingNodes attributes"
        validationContext = new ValidationContext("import", projectDataContentTarget)
        nodeInfoReader.read(*_) >> getNodeInfo(bscNodeName, rncNodeName)

        when: "Do the validation"
        boolean isExisting = ValidateControllingNodesExist.execute(validationContext)

        then: "The result should be same as the expect result"
        isExisting == result

        where:
        bscNodeName        | rncNodeName        || result
        BSC_NODE_NAME      | RNC_NODE_NAME      || true
        BAD_BSC_NODE_NAME  | BAD_RNC_NODE_NAME  || false
    }

    def getNodeInfo(final String bscFdn, final rncFdn){
        final NodeInfo nodeInfo = new NodeInfo()
        final Map<String, Object> controllingNodesAttributes = new HashMap<>()
        controllingNodesAttributes.put(CONTROLLING_BSC.getAttributeName(), bscFdn)
        controllingNodesAttributes.put(CONTROLLING_RNC.getAttributeName(), rncFdn)
        nodeInfo.setControllingNodesAttributes(controllingNodesAttributes)

        return nodeInfo
    }

    def addNetworkElementMo(final String neName) {
        dps.addManagedObject()
                .withFdn(String.format(NETWORK_ELEMENT_PREFIX, neName))
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .type(NETWORK_ELEMENT.toString())
                .build()
    }
}