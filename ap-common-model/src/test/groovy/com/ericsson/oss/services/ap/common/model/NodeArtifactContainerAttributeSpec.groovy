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
package com.ericsson.oss.services.ap.common.model

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Test class for {@link NodeArtifactContainerAttribute}
 */
class NodeArtifactContainerAttributeSpec extends CdiSpecification {

    def NodeArtifactContainerAttribute nodeArtifactContainerAttribute

    def "Verify toString method retrieves node artifact container attribute value"() {
        when:
            def attribute = nodeArtifactContainerAttribute.toString()

        then:
            expectedValue.equals(attribute)

        where:
            nodeArtifactContainerAttribute           | expectedValue
            NodeArtifactContainerAttribute.SUSPEND   | "suspend"
            NodeArtifactContainerAttribute.STRICT    | "strict"
    }
}