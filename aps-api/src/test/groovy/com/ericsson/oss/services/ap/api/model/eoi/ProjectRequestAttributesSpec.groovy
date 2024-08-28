package com.ericsson.oss.services.ap.api.model.eoi

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class ProjectRequestAttributesSpec extends CdiSpecification{
    def "Verify toString method retrieves project request attribute value"() {
        when:
        def attribute = projectRequestAttributes.toString()

        then:
        expectedValue.equals(attribute)

        where:
        projectRequestAttributes                         |       expectedValue

        ProjectRequestAttributes.PROJECT_NAME | "projectname"
        ProjectRequestAttributes.CREATOR      | "creator"
        ProjectRequestAttributes.DESCRIPTION  | "description"
        ProjectRequestAttributes.USE_CASE_TYPE | "useCaseType"
        ProjectRequestAttributes.NODE_NAME    | "name"
        ProjectRequestAttributes.NODE_TYPE    | "nodeType"
        ProjectRequestAttributes.IPADDRESS    | "ipAddress"
        ProjectRequestAttributes.CNF_TYPE     | "cnfType"
        ProjectRequestAttributes.TIME_ZONE | "timeZone"
        ProjectRequestAttributes.OSS_PREFIX | "ossPrefix"
        ProjectRequestAttributes.NODE_IDENTIFIER | "nodeIdentifier"
        ProjectRequestAttributes.USER_NAME | "secureUserName"
        ProjectRequestAttributes.PASSWORD | "securePassword"
        ProjectRequestAttributes.SUBJECT_ALT_NAME | "subjectAltName"
        ProjectRequestAttributes.WORKFLOW_INSTANCE_ID_LIST | "workflowInstanceIdList"
        ProjectRequestAttributes.EOI_NETWORK_ELEMENTS    | "networkelements"
        ProjectRequestAttributes.JSON_PAYLOAD  | "jsonPayload"
        ProjectRequestAttributes.SUPERVISION_ATTRIBUTES | "SupervisionOptions"
    }
}
