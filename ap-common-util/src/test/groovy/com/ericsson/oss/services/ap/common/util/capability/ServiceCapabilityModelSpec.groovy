/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.capability

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class ServiceCapabilityModelSpec extends CdiSpecification  {

    def "Read Security Resource and Operation from Service Capablity Model"() {
        given: "Service Capablity JSON file is pre-defined"

        when: "Read the required capabilities for an use case and check the model"
                def capabilities = ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(useCase)
                def resourceDefined = false
                def operationDefined = false
                for (SecurityCapability capability : capabilities) {
                    if (resource.equals(capability.getResource())) {
                        resourceDefined = true
                        operationDefined = capability.getOperations().contains(operation)
                        break;
                    }
                }

        then: "Security Resource and Operation are defined"
                resourceDefined == resResult
                operationDefined == opResult

        where:
                useCase               | resource                   | operation   | resResult | opResult
                "APPLY_AMOS_SCRIPT"   | "flowautomation_m2m"       | "read"      |  true     | true
                "APPLY_AMOS_SCRIPT"   | "flowautomation_m2m"       | "execute"   |  true     | true
                "APPLY_AMOS_SCRIPT"   | "amos_em_m2m"              | "read"      |  true     | true
                "APPLY_AMOS_SCRIPT"   | "amos_em_m2m"              | "create"    |  true     | false
                "APPLY_AMOS_SCRIPT"   | "amos_em_m2m"              | "patch"     |  true     | false
                "APPLY_AMOS_SCRIPT"   | "scripting_cli_access_m2m" | "execute"   |  true     | true
    }

    def "Read capabilities as a list from Service Capablity Model"() {
        given: "Service Capablity JSON file is pre-defined"

        when: "Read the required capabilities for an use case as a list"
                def capabilities = ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(useCase)
                def resourceSize = (capabilities == null ? 0 : capabilities.size())

        then: "The number of resources matches"
                resourceSize == size

        where:
                useCase                     | size
                "APPLY_AMOS_SCRIPT"         | 3
                "DUMMY_USE_CASE"            | 0
    }
}
