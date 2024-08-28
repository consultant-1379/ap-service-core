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
package com.ericsson.oss.services.ap.core.rest.war.resource

import javax.ws.rs.core.Response
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.ArtifactBaseType
import com.ericsson.oss.services.ap.api.AutoProvisioningService

class ConfigurationResourceV2Spec extends CdiSpecification {

	@ObjectUnderTest
	ConfigurationResourceV2 configurationResourceV2

	@MockedImplementation
	AutoProvisioningService autoProvisioningService

	private static final String projectId = "project1"
	private static final String nodeId = "node1"

	private static final String NODE_FDN = "Project=project1,Node=node1"

	def "Verify Response code 200-OK for dayZero Download Configuration Data"(){
		given: "Retrieve Configuration Data"
		String configurationName = "dayZero"

		autoProvisioningService.downloadConfigurationFile(NODE_FDN, nodeId) >> "src/test/resources/node1_Day0.json"

		when: "downloadConfigurationFile rest call is received"
		Response response = configurationResourceV2.downloadConfigurationFile(projectId, nodeId, configurationName, NODE_FDN)
		then: "response should be OK"
		assert(Response.Status.OK.getStatusCode() == response.getStatus());
	}

	def "Verify Response code 500-INTERNAL SERVER ERROR for siteInstall Download Configuration Data"(){
		given: "Retrieve Configuration Data"
		String configurationName = "siteInstall"

		autoProvisioningService.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.GENERATED) >> "nodeData.xml"

		when: "downloadConfigurationFile rest call is received"
		Response response = configurationResourceV2.downloadConfigurationFile(projectId, nodeId, configurationName, NODE_FDN)
		then: "response should be INTERNAL SERVER ERROR"
		assert(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == response.getStatus());
	}

	def "Verify Response code 404-NOT FOUND for Download Configuration Data with file path null"(){
		given: "Retrieve Configuration Data"
		String configurationName = "dayZero"

		autoProvisioningService.downloadConfigurationFile(NODE_FDN,nodeId) >> null

		when: "downloadConfigurationFile rest call is received"
		Response response = configurationResourceV2.downloadConfigurationFile(projectId, nodeId, configurationName, NODE_FDN)
		then: "response should be NOT FOUND"
		assert(Response.Status.NOT_FOUND.getStatusCode() == response.getStatus());
	}

	def "Verify Response code 400-BAD REQUEST for Download Configuration Data"(){
		given: "Retrieve Configuration Data"
		String configurationName = "ap"

		when: "downloadConfigurationFile rest call is received"
		Response response = configurationResourceV2.downloadConfigurationFile(projectId, nodeId, configurationName, NODE_FDN)
		then: "response should be BAD REQUEST"
		assert(Response.Status.BAD_REQUEST.getStatusCode() == response.getStatus());
	}
}
