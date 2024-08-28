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
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing

import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import org.apache.http.HttpEntity
import org.apache.http.util.EntityUtils
import org.mockito.internal.util.reflection.Whitebox
import spock.lang.Subject

import javax.inject.Inject

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec

class InstantaneousLicensingRestDataBuilderSpec extends CdiSpecification {

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_NAME = "Node1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=" + NODE_NAME
    private static final String LICENSE_FDN = NODE_FDN + ",LicenseOptions=1"
    private static final String AUTOINTEGRATION_FDN = NODE_FDN + ",AutoIntegrationOptions=1"
    private static final String ATTRIBUTE_VALUE = NODE_NAME + "_attribute"

    @Subject
    @Inject
    private InstantaneousLicensingRestDataBuilder instantaneousLicensingRestDataBuilder

    @Inject
    private DpsOperations dpsOperations

    private RuntimeConfigurableDps dps

    private ManagedObject projectMo
    private ManagedObject nodeMo

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        instantaneousLicensingRestDataBuilder.dpsOperations = this.dpsOperations
        MoCreatorSpec.setDps(dps)
        projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
        nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        addAutoIntegrationMo(nodeMo)
    }

    def "When REST object is created for Create License request then it has expected content"() {
        when: "Http object is created by the builder"
            addLicenseMo(nodeMo, ATTRIBUTE_VALUE)
            HttpEntity entity = instantaneousLicensingRestDataBuilder.buildCreateLicenseRequest(NODE_FDN)

        then: "The object has expected values"
            String responseData =EntityUtils.toString(entity)
            responseData.contains("\"fingerprint\":\"Node1_attribute\"")
            responseData.contains("\"swltId\":\"Node1_attribute\"")
            responseData.contains("\"softwarePackageName\":\"Node1_attribute\"")
            responseData.contains("\"hardwareType\":\"Node1_attribute\"")
            responseData.contains("\"radioAccessTechnologies\":[]")
            responseData.contains("\"groupId\":\"Node1_attribute\"}")
    }

    def "When REST object is created for Create License request and there's no fingerprint then it has expected content"() {
        when: "Http object is created by the builder"
            addLicenseMo(nodeMo)
            HttpEntity entity = instantaneousLicensingRestDataBuilder.buildCreateLicenseRequest(NODE_FDN)

        then: "The object has expected values"
            String responseData =EntityUtils.toString(entity)
            responseData.contains("\"fingerprint\":\"Node1\"")
            responseData.contains("\"swltId\":\"Node1_attribute\"")
            responseData.contains("\"softwarePackageName\":\"Node1_attribute\"")
            responseData.contains("\"hardwareType\":\"Node1_attribute\"")
            responseData.contains("\"radioAccessTechnologies\":[]")
            responseData.contains("\"groupId\":\"Node1_attribute\"}")
    }

    private ManagedObject addLicenseMo(final ManagedObject parentMo, String fingerprint = null) {
        final Map<String,Object> attributes = new HashMap<>();
        attributes.put("fingerprint", fingerprint)
        attributes.put("softwareLicenseTargetId", ATTRIBUTE_VALUE)
        attributes.put("hardwareType", ATTRIBUTE_VALUE)
        attributes.put("radioAccessTechnologies", new ArrayList<>())
        attributes.put("groupId", ATTRIBUTE_VALUE)
        return dps.addManagedObject()
                .withFdn(LICENSE_FDN)
                .type(LICENSE_OPTIONS.toString())
                .namespace(AP.toString())
                .addAttributes(attributes)
                .parent(parentMo)
                .build()
    }

    private ManagedObject addAutoIntegrationMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>();
        attributes.put("upgradePackageName", ATTRIBUTE_VALUE)
        return dps.addManagedObject()
                .withFdn(AUTOINTEGRATION_FDN)
                .type(AI_OPTIONS.toString())
                .namespace(AP.toString())
                .addAttributes(attributes)
                .parent(parentMo)
                .build()
    }

}
