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
package com.ericsson.oss.services.ap.common.workflow.model

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.workflow.messages.GetNodeConfigurationMessage

/**
 * Test class for {@link GetNodeConfigurationMessage}
 */
class GetNodeConfigurationMessageSpec extends CdiSpecification {

    @ObjectUnderTest
    GetNodeConfigurationMessage request

    private static final String PATH = "/test/file/path"

    def "Setters work as expected" () {
        given: "Object created"
        request = new GetNodeConfigurationMessage()

        when: "Setters are called"
        request.setAdditionalInfo(PATH)
        request.setResult(true)

        then: "Getters return expected values"
        request.getAdditionalInfo() == PATH
        request.isSuccessful() == true
        request.convertToWorkflowVariables().getAt(request.RESULT_KEY) == true
        request.convertToWorkflowVariables().getAt(request.ADDITIONAL_INFO_KEY) == PATH
        request.getMessageKey() == request.GET_NODE_CONFIGURATION_COMPLETED_KEY
        request.getResultKey() == request.RESULT_KEY
        request.getAdditionalInfoKey() == request.ADDITIONAL_INFO_KEY
    }
}
