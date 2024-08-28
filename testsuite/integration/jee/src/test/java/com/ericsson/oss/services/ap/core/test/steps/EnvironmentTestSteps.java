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
package com.ericsson.oss.services.ap.core.test.steps;

import static com.ericsson.oss.services.ap.arquillian.util.data.project.ProjectDescriptor.ProjectDescriptorBuilder.usingNodeType;
import static com.ericsson.oss.services.ap.core.test.data.ImportTestData.NODE_DATA;
import static com.ericsson.oss.services.ap.core.test.data.ProjectArtifactData.PROJECT_DATA;
import static com.ericsson.oss.services.ap.core.test.data.ProjectArtifactData.PROJECT_DATA_WITH_FORMAT;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.Config;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.Files;
import com.ericsson.oss.services.ap.arquillian.util.ProjectGenerator;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.arquillian.util.data.dps.model.DetachedManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.data.project.ProjectDescriptor;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.core.test.ErbsProjectOptions;

import cucumber.api.java.en.Then;
import ru.yandex.qatools.allure.annotations.Step;

public class EnvironmentTestSteps {

    private static final String SITE_INSTALL = "siteInstallation";
    private static final String PROFILE_CIQ_DIRECTORY = "/ericsson/autoprovisioning/projects/%s/profiles/%s/ciq";

    private static final Set<String> VNF_NODE_TYPES = new HashSet<>(Arrays.asList("vPP", "vSD"));

    private static final String ERBS_NODE_TYPE = "erbs";

    private static final String COM_TOP_NAMESPACE = "ComTop";
    private static final String COM_TOP_NAMESPACE_VERSION = "10.21.0";

    @Inject
    private Files fileHelper;

    @Inject
    private Dps dpsHelper;

    @Inject
    private ProjectGenerator projectGenerator;

    @Step("Create project of type {0} with {1} node(s)")
    public ManagedObject create_project(final String nodeType, final int nodeCount) {
        final String actualType = ERBS_NODE_TYPE.equalsIgnoreCase(nodeType) ? "ERBS" : nodeType;
        final String internalNodeType = getActualNodeType(nodeType);
        final ProjectDescriptor projectDescriptor = usingNodeType(actualType)
            .withNodeIdentifier((String) NODE_DATA.get(String.format("%sNodeIdentifier", internalNodeType)))
            .withNodeCount(nodeCount)
            .withArtifacts(PROJECT_DATA.get(actualType))
            .build();

        return getFirstNodeMo(projectGenerator.generate(projectDescriptor));
    }

    @Step("Create project of type {1} with {2} node(s)")
    public ManagedObject create_project(final String projectName, final String nodeType, final int nodeCount, final String... nodeNames) {
        final String actualType = ERBS_NODE_TYPE.equalsIgnoreCase(nodeType) ? "ERBS" : nodeType;

        final ProjectDescriptor.ProjectDescriptorBuilder projectDescriptorBuilder = usingNodeType(actualType)
            .withProjectName(projectName)
            .withNodeCount(nodeCount)
            .withNodeNames(nodeNames)
            .withArtifacts(PROJECT_DATA.get(actualType))
            .withAutoIntegrationOption("upgradePackageName", "dummy_upgrade_package");
        //will be removed once ProjectDescriptor class is updated
        final String internalNodeType = getActualNodeType(nodeType);
        final String nodeIdentifier = (String) NODE_DATA.get(String.format("%sNodeIdentifier", internalNodeType));
        if (nodeIdentifier != null) {
            projectDescriptorBuilder.withNodeIdentifier(nodeIdentifier);
        }
        final ProjectDescriptor projectDescriptor = projectDescriptorBuilder
            .build();

        return getFirstNodeMo(projectGenerator.generate(projectDescriptor));
    }

    @Step("Create project of type {1} with {3} format artifacts")
    public ManagedObject createProject(final String projectName, final String nodeType, final String nodeName, final String fileFormat) {
        final String actualType = ERBS_NODE_TYPE.equalsIgnoreCase(nodeType) ? "ERBS" : nodeType;

        final ProjectDescriptor.ProjectDescriptorBuilder projectDescriptorBuilder = usingNodeType(actualType)
            .withProjectName(projectName)
            .withNodeCount(1)
            .withNodeNames(nodeName)
            .withArtifacts(PROJECT_DATA_WITH_FORMAT.get(fileFormat).get(actualType))
            .withAutoIntegrationOption("upgradePackageName", "dummy_upgrade_package");
        //will be removed once ProjectDescriptor class is updated
        final String internalNodeType = getActualNodeType(nodeType);
        final String nodeIdentifier = (String) NODE_DATA.get(String.format("%sNodeIdentifier", internalNodeType));
        if (nodeIdentifier != null) {
            projectDescriptorBuilder.withNodeIdentifier(nodeIdentifier);
        }
        final ProjectDescriptor projectDescriptor = projectDescriptorBuilder
            .build();

        return getFirstNodeMo(projectGenerator.generate(projectDescriptor));
    }

    @Step("Create project of type {0} with {1} node(s)")
    public ManagedObject create_project_without_artifacts(final String nodeType, final int nodeCount) {
        final String actualType = ERBS_NODE_TYPE.equalsIgnoreCase(nodeType) ? "ERBS" : nodeType;
        final ProjectDescriptor projectDescriptor = usingNodeType(actualType)
            .withNodeCount(nodeCount)
            .build();

        return getFirstNodeMo(projectGenerator.generate(projectDescriptor));
    }

    @Step("Ordering node {0}")
    public void order_node_for_project(final ManagedObject nodeMo) {
        if (VNF_NODE_TYPES.contains(nodeMo.getAttribute("nodeType"))) {
            return;
        }
        final String artifactResourcePath = ErbsProjectOptions.getDefaultArtifact(SITE_INSTALL).getArtifactLocation();
        final Path artifactFilePath = create_generated_artifact_file(nodeMo, artifactResourcePath);

        final String artifactFdn = getArtifactFdn(
            nodeMo.getFdn(),
            SITE_INSTALL);

        update_node_mo_with_generated_file_path(artifactFdn, artifactFilePath.toString());
    }

    @Step("Create generated artifact file for {0} ({1})")
    public Path create_generated_artifact_file(final ManagedObject nodeMo, final String artifactResourcePath) {
        final String artifactFileName = new File(artifactResourcePath).getName();
        final byte[] artifactFileContents = Resources.getResourceAsBytes(artifactResourcePath);
        final String subDir = nodeMo.getParent().getName() + File.separator + nodeMo.getName();
        return fileHelper.writeArtifact(subDir, artifactFileName, artifactFileContents, "generated");
    }

    @Step("Update {0} with generated file path ({1})")
    public void update_node_mo_with_generated_file_path(final String nodeArtifactFdn, final String artifactFilePath) {
        final Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("generatedLocation", artifactFilePath);
        updateParams.put("exportable", true);
        dpsHelper.updateMo(nodeArtifactFdn, updateParams);
    }

    @Step("Get artifact of type {1} for node {0}")
    public String getArtifactFdn(final String nodeFdn, final String artifactType) {
        final ManagedObject artifactMo = dpsHelper.findMoByFdn(nodeFdn + ",NodeArtifactContainer=1");
        for (final ManagedObject child : artifactMo.getChildren()) {
            final String currentArtifactType = child.getAttribute("type");
            if (artifactType.equalsIgnoreCase(currentArtifactType)) {
                return child.getFdn();
            }
        }

        return null;
    }

    @Step
    public ManagedObject createProjectMo(final String projectName) {
        final String description = "This is a test project";
        final String creator = System.getProperty("user.name");

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("creator", creator);
        attributes.put("description", description);
        attributes.put("creationDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        final String apModelVersion = Config.getProperty("ap.model.version");
        final ManagedObject projectMo = DetachedManagedObject.Builder.newDetachedManagedObject()
            .mibRoot(true)
            .name(projectName)
            .namespace("ap")
            .type("Project")
            .version(apModelVersion)
            .attributes(attributes)
            .build();

        return dpsHelper.createMo(projectMo);
    }

    @Step
    public void createProfileMo(final ManagedObject projectMo, final String profileName) {
        final Map<String, Object> profileAttributes = new HashMap<>();
        profileAttributes.put("profileId", profileName);
        profileAttributes.put(ProfileAttribute.PROPERTIES.toString(), "PROPERTIES");

        final Map<String, Object> version = new HashMap<>();
        version.put(ProfileAttribute.PRODUCT_NUMBER.toString(), "PRODUCT_NUMBER");
        version.put(ProfileAttribute.PRODUCT_RELEASE.toString(), "PRODUCT_RELEASE");
        profileAttributes.put(ProfileAttribute.VERSION.toString(), version);

        profileAttributes.put(ProfileAttribute.GRAPHIC_LOCATION.toString(), "GRAPHIC_LOCATION");
        profileAttributes.put(ProfileAttribute.PROFILE_CONTENT_LOCATION.toString(), "PROFILE_CONTENT_LOCATION");
        profileAttributes.put(ProfileAttribute.FILTER_LOCATION.toString(), "FILTER_LOCATION");

        final Map<String, Object> ciq = new HashMap<>();
        ciq.put(ProfileAttribute.CIQ_LOCATION.toString(), String.format(PROFILE_CIQ_DIRECTORY, projectMo.getName(), profileName));
        profileAttributes.put(ProfileAttribute.CIQ.toString(), ciq);
        profileAttributes.put(ProfileAttribute.DATATYPE.toString(), "INTEGRATION");

        final Map<String, Object> status = new HashMap<>();
        status.put(ProfileAttribute.IS_VALID.toString(), true);
        status.put(ProfileAttribute.PROFILE_DETAILS.toString(), new ArrayList<>());
        profileAttributes.put(ProfileAttribute.STATUS.toString(), status);

        final ManagedObject profileMo = DetachedManagedObject.Builder.newDetachedManagedObject()
            .mibRoot(true)
            .name(profileName)
            .namespace("ap")
            .type("ConfigurationProfile")
            .attributes(profileAttributes)
            .parent(projectMo)
            .build();
        dpsHelper.createMo(profileMo);
    }

    @Step
    public void createRadioNodeMO(final String nodeName) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("managedElementId", nodeName);
        attributes.put("neType", "RadioNode");

        final ManagedObject managedElementMo = DetachedManagedObject.Builder.newDetachedManagedObject()
            .mibRoot(true)
            .name(nodeName)
            .namespace(COM_TOP_NAMESPACE)
            .type("ManagedElement")
            .version(COM_TOP_NAMESPACE_VERSION)
            .attributes(attributes)
            .build();

        dpsHelper.createMo(managedElementMo);
    }

    @Step("Update {0} with snapshot status ({1})")
    public void update_profile_mo_with_snapshot_status(final String profileFdn, final String snapshotStatus) {
        final Map<String, Object> updateParams = new HashMap<>();
        updateParams.put("configSnapshotStatus", snapshotStatus);
        dpsHelper.updateMo(profileFdn, updateParams);
    }

    @Then("^the project (.+) (exists|does not exist)$")
    public void projectExists(final String projectName, final String condition) {
        final ManagedObject mo = dpsHelper.findMoByFdn("Project=" + projectName);
        if ("exists".equals(condition)) {
            assertNotNull("MO Project=" + projectName + " should have been created", mo);
        } else {
            assertNull("MO Project=" + projectName + " should NOT have been created", mo);
        }
    }

    private ManagedObject getFirstNodeMo(final ManagedObject projectMo) {
        return projectMo
            .getChildren()
            .iterator()
            .next();
    }

    private String getActualNodeType(final String nodeType) {
        if ("RadioNode".equalsIgnoreCase(nodeType)) {
            return "ecim";
        } else if ("MSRBS_V1".equalsIgnoreCase(nodeType)) {
            return "msrbs_v1";
        }
        return nodeType;
    }
}
