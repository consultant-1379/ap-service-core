/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.rest.builder

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.core.rest.view.ViewNodePropertiesResponseBuilder
import com.ericsson.oss.services.ap.core.rest.view.properties.data.PropertyNameValue
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewPropertiesResponse

class NodePropertiesBuilderSpec extends CdiSpecification {

    public static final String AUTO_INTEGRATION_OPTIONS = "AutoIntegrationOptions"
    public static final String LICENSE_OPTIONS = "License"

    @ObjectUnderTest
    private NodePropertiesBuilder nodePropertiesBuilder;

    @MockedImplementation
    private ViewNodePropertiesResponseBuilder nodePropertiesResponseBuilder;

    def nodeAttributes = [
            "nodeName" : "node 1",
            "ipAddress": "1.1.1.1"
    ]
    def autointegrationAttributes = [
            "upgradePackageName": "RadioNode R2DGD release upgrade package"
    ]

    def licenseAttributes = [
            "installLicense" : true
    ]

    def "Builds NodeProperties response from ViewNodeDetailsResponseBuilder"() {

        given: "a ViewPropertiesResponse object is created"
        List<ViewProperties> viewProperties = new ArrayList()
        viewProperties.add(createViewProperty("node", nodeAttributes))
        viewProperties.add(createViewProperty(AUTO_INTEGRATION_OPTIONS, autointegrationAttributes))
        viewProperties.add(createViewProperty(LICENSE_OPTIONS, licenseAttributes))
        def viewPropertiesResponse = new ViewPropertiesResponse(viewProperties)
        nodePropertiesResponseBuilder.buildViewForNode(new ArrayList<MoData>()) >> viewPropertiesResponse

        when: "nodePropertiesBuilder builds NodeProperties"
        def nodeProperties = nodePropertiesBuilder.buildNodeProperties(new ArrayList<MoData>())

        then: "NodeProperties contains correct node attributes"
        def nodeNameAttribute = nodeProperties.attributes.get(0) as PropertyNameValue
        nodeNameAttribute.name == "nodeName"
        nodeNameAttribute.value == "node 1"
        def ipAddressAttribute = nodeProperties.attributes.get(1) as PropertyNameValue
        ipAddressAttribute.name == "ipAddress"
        ipAddressAttribute.value == "1.1.1.1"

        and: "NodeProperties contains AutoIntegration attributeGroup"
        def autointegrationOptions = nodeProperties.attributeGroups.get(0)
        autointegrationOptions.type == AUTO_INTEGRATION_OPTIONS
        def upgradePackageProperty = autointegrationOptions.properties.get(0) as PropertyNameValue
        upgradePackageProperty.name == "upgradePackageName"
        upgradePackageProperty.value == "RadioNode R2DGD release upgrade package"

        and: "NodeProperties contains License attributeGroup"
        def licenseOptions = nodeProperties.attributeGroups.get(1)
        licenseOptions.type == LICENSE_OPTIONS
        def installLicenseProperty = licenseOptions.properties.get(0) as PropertyNameValue
        installLicenseProperty.name == "installLicense"
        installLicenseProperty.value == true
    }

    def "Throws ApServiceException ViewNodeDetailsResponseBuilder fails to return correct response"() {

        given: "a ViewPropertiesResponse object contains no ViewProperties"
        def viewPropertiesResponse = new ViewPropertiesResponse(Collections.emptyList())
        nodePropertiesResponseBuilder.buildViewForNode(new ArrayList<MoData>()) >> viewPropertiesResponse

        when: "NodePropertiesBuilder builds NodeProperties"
        nodePropertiesBuilder.buildNodeProperties(new ArrayList<MoData>())

        then: "ApServiceException is thrown"
        thrown(ApServiceException)

    }

    private static ViewProperties createViewProperty(type, properties) {
        List<Object> attributes = new ArrayList<>()
        properties.each {
            k, v -> attributes.add(new PropertyNameValue(k as String, v))

        }
        return new ViewProperties(type as String, attributes)
    }
}
