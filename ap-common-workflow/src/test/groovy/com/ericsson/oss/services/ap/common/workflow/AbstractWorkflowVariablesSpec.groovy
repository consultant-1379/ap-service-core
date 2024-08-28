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
package com.ericsson.oss.services.ap.common.workflow.task.rollback

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables


class AbstractWorkflowVariablesSpec extends CdiSpecification {

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String  originalBackupName = "testName"
    protected static final String baseUrl = "baseUrl"
    protected static final String sessionId = "sessionId"

    def "when setOriginalBackupName to originalBackupName then get originalBackupName"() {

        when: "invoking setOriginalBackupName"
            workflowVariables.setOriginalBackupName(originalBackupName);

        then: "Validated succssfully"
            workflowVariables.getOriginalBackupName() == originalBackupName
    }

    def "when setBaseUrl then getBaseUrl"() {

        when: "invoking setBaseUrl"
        workflowVariables.setBaseUrl(baseUrl);

        then: "Validated succssfully"
        workflowVariables.getBaseUrl() == baseUrl
    }

    def "when setSessionId then getSessionId"() {

        when: "invoking setBaseUrl"
        workflowVariables.setSessionId(sessionId);

        then: "Validated succssfully"
        workflowVariables.getSessionId() == sessionId;
    }

    def "Verify configuration strict attribute value is set as expected"() {

        when: "invoking setImportConfigurationInStrictSequence"
            workflowVariables.setImportConfigurationInStrictSequence(value)

        then: "Validated successfully"
            workflowVariables.isImportConfigurationInStrictSequence() == expectedValue

        where:
            value       | expectedValue
            true        | true
            false       | false
    }

    def "Verify configuration strict attribute defalut value is set as expected"() {

        expect: "Configuration strict attribute is set as default value"
            workflowVariables.isImportConfigurationInStrictSequence() == false
    }

    def "Verify EoiRollBack value"(){
        when:
        workflowVariables.setEoiRollbackError(true)
        then:
        workflowVariables.isEoiRollbackError()==true

    }
}
