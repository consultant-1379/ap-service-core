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
package com.ericsson.oss.services.ap.core.test.order;

import static com.ericsson.oss.services.ap.arquillian.util.data.dps.model.DetachedPersistenceObject.Builder.newDetachedPersistenceObject;
import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.data.managedobject.OSSMosGenerator;
import com.ericsson.oss.services.ap.arquillian.util.Files;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.OrderTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;

/**
 * Tests related to order a hardware replace project archive functionality.
 */
@RunWith(Cucumber.class)
public class OrderHardwareReplaceTest extends ServiceCoreTest {

    private static final String FILE_PREFIX_REPLACE = "import/replace/";
    private String fileName;

    @Inject
    private OrderTestSteps orderTestSteps;

    @Inject
    private StubbedServiceSteps stubbedService;

    @Inject
    private OSSMosGenerator ossMosGenerator;

    @Inject
    private Dps dpsHelper;

    @Inject
    private Files fileHelper;

    @Given("^the project (.+) and node (.+) with node type (.+)")
    public void set_project_and_node(final String projectName, final String nodeName, final String nodeType) {
        this.projectName = projectName;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
    }

    @Given("^the user has a replace file named '(.+)'$")
    public void replace(final String fileName) {
        this.fileName = fileName;
    }

    @Given("^node pre-exists in the NRM database$")
    public void add_radio_node_to_nrm_database() {
        final ManagedObject networkElementMo = createNetworkElementMo(nodeName, nodeType);
        final ManagedObject securityFunctionMo = createSecurityFunctionMo(networkElementMo);
        createNetworkElementSecurityMo(securityFunctionMo);
        createCMManagedObject(nodeName);
        ossMosGenerator.generateConnectivityInfo(nodeName, "1.0.0", "COM_MED", "ComConnectivityInformation");
        updateNeTarget(nodeName, nodeType);
    }

    @When("^the user requests a hardwareReplace with backupName (.+) on node (.+) with hardwareSerialNumber '(.+)'$")
    public void hardwareReplaceOfNodeWithBackupNamed(final String backupName, final String nodeFdn, final String hardwareSerialNumber) {
        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_REPLACE + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders replace file$")
    public void order_replace_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_REPLACE + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @Then("^the system will have created only a siteInstallation file in raw directory$")
    public void validateSiteInstallationFileExistsInRawDirectory() {
        final File[] rawArtifacts = fileHelper.getArtifacts(this.projectName + "/" + this.nodeName, "raw");
        assertThat(rawArtifacts).extracting("name").containsOnly("siteInstallation.xml");
    }

    private void updateNeTarget(final String nodeName, final String nodeType) {
        final Map<String, Object> poTargetAttributes = new HashMap<>();
        poTargetAttributes.put("category", "NODE");
        poTargetAttributes.put("type", nodeType);
        poTargetAttributes.put("name", nodeName);

        final PersistenceObject target = newDetachedPersistenceObject()
            .namespace("DPS")
            .type("Target")
            .version("1.0.0")
            .attributes(poTargetAttributes)
            .build();

        final PersistenceObject createdPo = dpsHelper.createPo(target);
        final String networkElementFdn = String.format("NetworkElement=%s", nodeName);
        dpsHelper.updateMoTarget(createdPo.getPoId(), networkElementFdn);
        dpsHelper.addPoAssociation(createdPo.getPoId(), getConnectivityInfoFdn(networkElementFdn), "ciRef");
    }

    private String getConnectivityInfoFdn(final String networkElementFdn) {
        final ManagedObject networkElement = dpsHelper.findMoByFdn(networkElementFdn);
        final Collection<ManagedObject> children = networkElement.getChildren();

        for (final ManagedObject child : children) {
            if (child.getFdn().contains("ConnectivityInformation")) {
                return child.getFdn();
            }
        }
        throw new IllegalStateException(String.format("Could not find connectivity information child MO for %s", networkElementFdn));
    }
}
