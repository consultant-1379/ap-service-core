/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationContext


class EoiValidateNodeNameUniqueSpec extends CdiSpecification {

    @ObjectUnderTest
    private EoiValidateNodeNameUnique eoiValidateNodeNameUnique;

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_1 = "Node1"
    private static final String NODE_2 = "Node2"

    private ValidationContext validationContext;


    def "Validation passes when node is unique in project "() {
        given: "Project file"
        Map<String, Object> attributes = [

                name     : NODE_1,
                neType   : "ERBS",
                cnfType  : "cnftype",
                ipAddress: "1.2.3.4"

        ]

        final List list = new ArrayList()
        list.add(attributes)
        final Map<String, Object> elements = new HashMap<>()
        elements.put("networkelements", list)

        projectDataContentTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), elements)
        validationContext = new ValidationContext("import", projectDataContentTarget)
        when: "There is unique node in the project "
        boolean isTrue = eoiValidateNodeNameUnique.execute(validationContext)

        then: "Validation passed"
        isTrue == true
    }

    def "Validation fails when two network elements with the same node name in the project "() {
        given: "NodeElements 1, NodeElements 2"
        Map<String, Object> attributes = [

                name     : NODE_1,
                neType   : "ERB",
                cnfType  : "type",
                ipAddress: "1.2.3.4"

        ]
        Map<String, Object> attributes1 = [

                name     : NODE_1,
                neType   : "ERBS",
                cnfType  : "cnftype",
                ipAddress: "1.2.3.5"

        ]

        final List list = new ArrayList()
        list.add(attributes)
        list.add(attributes1)
        final Map<String, Object> elements = new HashMap<>()
        elements.put("networkelements", list)

        projectDataContentTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), elements)

        validationContext = new ValidationContext("import", projectDataContentTarget)

        when: "There is unique node in the project "
        boolean isUniqueNode = eoiValidateNodeNameUnique.execute(validationContext)

        then: "Verification failed"
        isUniqueNode == false
    }

    def "Validation pass when two different node name in the project "() {
        given: "NodeElements 1, NodeElements 2"

        Map<String, Object> attributes = [

                name     : NODE_1,
                neType   : "ERB",
                cnfType  : "type",
                ipAddress: "1.2.3.4"

        ]
        Map<String, Object> attributes1 = [

                name     : NODE_2,
                neType   : "ERBS",
                cnfType  : "cnftype",
                ipAddress: "1.2.3.5"

        ]

        final List list = new ArrayList()
        list.add(attributes)
        list.add(attributes1)
        final Map<String, Object> elements = new HashMap<>()
        elements.put("networkelements", list)

        projectDataContentTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), elements)

        validationContext = new ValidationContext("import", projectDataContentTarget);


        when: "There is unique node in the project "
        boolean isUniqueNode = eoiValidateNodeNameUnique.execute(validationContext)

        then: "Verification failed"
        isUniqueNode == true
    }

    def "Validation fails when the network elements has no node name"() {
        given: "NetworkElement file"

        Map<String, Object> attributes = [

                name     : " ",
                neType   : "ERB",
                cnfType  : "type",
                ipAddress: "1.2.3.4"

        ]

        final List list = new ArrayList()
        list.add(attributes)
        final Map<String, Object> elements = new HashMap<>()
        elements.put("networkelements", list)

        projectDataContentTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), elements)
        validationContext = new ValidationContext("import", projectDataContentTarget)


        when: "There is unique node in the project "
        boolean isValidateName = eoiValidateNodeNameUnique.execute(validationContext)

        then: "Verification failed"
        isValidateName == false
    }

}
