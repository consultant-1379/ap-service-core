/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test;

import static com.ericsson.oss.services.ap.arquillian.util.data.dps.model.DetachedManagedObject.Builder.newDetachedManagedObject;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.data.managedobject.OSSMosGenerator;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.test.statements.EnvironmentStatements;
import com.ericsson.oss.services.ap.core.test.statements.ErrorStatements;
import com.ericsson.oss.services.ap.core.test.statements.NodeStatements;
import com.ericsson.oss.services.ap.core.test.statements.RestStatements;
import com.ericsson.oss.services.ap.core.test.statements.SecurityStatements;
import com.ericsson.oss.services.ap.core.test.statements.ZipStatements;
import com.google.common.collect.ImmutableMap;

/**
 * Main test class, to be extended by all test classes.
 */
public abstract class ServiceCoreTest {

    private final static String NETWORK_ELEMENT_SECURITY_NAMESPACE = "OSS_NE_SEC_DEF";

    @Inject
    private Dps dpsHelper;

    @Inject
    private ModelReader modelReader;

    @Inject
    protected EnvironmentStatements environmentStatements;

    @Inject
    protected ErrorStatements errorStatements;

    @Inject
    protected SecurityStatements securityStatements;

    @Inject
    protected NodeStatements nodeStatements;

    @Inject
    protected ZipStatements zipStatements;

    @Inject
    protected RestStatements restStatements;

    @Before
    public final void initialize() {
        environmentStatements.clear();
        errorStatements.clear();
        securityStatements.clear();
        nodeStatements.clear();
        zipStatements.clear();
        restStatements.clear();
    }

    protected String nodeName;
    protected String projectName;
    protected String nodeType;
    protected String remoteNodeName;
    protected String remoteNodeType;

    protected ManagedObject createNetworkElementMo(final String nodeName, final String nodeType) {
        final ManagedObject networkElementMo = newDetachedManagedObject().namespace("OSS_NE_DEF").type("NetworkElement").version("2.0.0")
            .name(nodeName).mibRoot(true).parent(null).attributes(ImmutableMap.<String, Object> builder().put("neType", nodeType)
                .put("ossModelIdentity", getOssModelIdentity(nodeType)).build())
            .build();
        return dpsHelper.createMo(networkElementMo);
    }

    protected ManagedObject createSecurityFunctionMo(final ManagedObject networkElementMo) {
        final Map<String, Object> securityFunctionAttributes = new HashMap<>();
        final ManagedObject securityFunctioMo = newDetachedManagedObject()
            .namespace(NETWORK_ELEMENT_SECURITY_NAMESPACE)
            .type("SecurityFunction")
            .version("1.0.0")
            .name("1")
            .mibRoot(true)
            .attributes(securityFunctionAttributes)
            .parent(networkElementMo)
            .build();

        return dpsHelper.createMo(securityFunctioMo);
    }

    protected ManagedObject createNetworkElementSecurityMo(final ManagedObject securityFunctionMo) {
        final Map<String, Object> networkElementSecurityAttributes = new HashMap<>();
        networkElementSecurityAttributes.put("secureUserName", "secureUser");
        networkElementSecurityAttributes.put("secureUserPassword", "securePassword");

        final ManagedObject networkElementSecurityMo = newDetachedManagedObject()
            .type("NetworkElementSecurity")
            .namespace(NETWORK_ELEMENT_SECURITY_NAMESPACE)
            .version("4.1.0")
            .name("1")
            .mibRoot(true)
            .parent(securityFunctionMo)
            .attributes(networkElementSecurityAttributes)
            .build();

        return dpsHelper.createMo(networkElementSecurityMo);
    }

    protected void createBscControllingNode(final String nodeName) {
        final Map<String, Object> attributes = ImmutableMap.<String, Object> builder()
            .put("neType", "BSC")
            .put("ossModelIdentity", "BSC-G17.Q4-R1C-APG43L-3.4.0-R5A")
            .put("platformType", "ECIM")
            .put("ossPrefix", String.format("MeContext=%s", nodeName))
            .put("timeZone", "GMT")
            .build();

        final ManagedObject networkElementMo = newDetachedManagedObject()
            .mibRoot(true)
            .parent(null)
            .namespace("OSS_NE_DEF")
            .type("NetworkElement")
            .version("2.0.0")
            .name(nodeName).attributes(attributes)
            .build();

        dpsHelper.createMo(networkElementMo);
    }

    protected void createRncControllingNode(final String nodeName) {
        final Map<String, Object> attributes = ImmutableMap.<String, Object> builder()
            .put("neType", "RNC")
            .put("ossModelIdentity", "16A-V.6.940")
            .put("platformType", "CPP")
            .put("ossPrefix", String.format("MeContext=%s", nodeName))
            .build();

        final ManagedObject networkElementMo = newDetachedManagedObject()
            .mibRoot(true)
            .parent(null)
            .namespace("OSS_NE_DEF")
            .type("NetworkElement")
            .version("2.0.0")
            .name(nodeName).attributes(attributes)
            .build();

        dpsHelper.createMo(networkElementMo);
    }

    protected void createCMManagedObject(final String nodeName) {
        final ManagedObject mo = dpsHelper.findMoByFdn("NetworkElement=" + nodeName);
        final ManagedObject generatedCmFunction = newDetachedManagedObject().namespace("OSS_NE_CM_DEF").type("CmFunction").version("1.0.1").name("1")
            .attributes(ImmutableMap.<String, Object> builder().put("CmFunctionId", "1").put("syncStatus", "SYNCHRONIZED").build()).parent(mo)
            .mibRoot(true).build();
        dpsHelper.createMo(generatedCmFunction);
    }

    protected void createCMManagedObjectNotSync(final String nodeName) {
        final ManagedObject mo = dpsHelper.findMoByFdn("NetworkElement=" + nodeName);
        final ManagedObject generatedCmFunction = newDetachedManagedObject().namespace("OSS_NE_CM_DEF").type("CmFunction").version("1.0.1").name("1")
            .attributes(ImmutableMap.<String, Object> builder().put("CmFunctionId", "1").put("syncStatus", "UNSYNCHRONIZED").build()).parent(mo)
            .mibRoot(true).build();
        dpsHelper.createMo(generatedCmFunction);
    }

    protected void createProjectMo(final String projectName) {
        final ModelData projectModelData = modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.PROJECT.toString());
        final ManagedObject projectMo = newDetachedManagedObject()
            .mibRoot(true)
            .namespace(projectModelData.getNameSpace())
            .type(MoType.PROJECT.toString())
            .version(projectModelData.getVersion())
            .name(projectName)
            .attributes(ImmutableMap.<String, Object> builder()
                .put("creator", "Jimmy")
                .put("description", "Dummy Project")
                .put("creationDate", "2018-08-17 14:45:34")
                .build())
            .build();
        dpsHelper.createMo(projectMo);
    }

    protected void setNodeStatus(final String nodeFdn, final String state) {
        dpsHelper.updateMo(nodeFdn + "," + MoType.NODE_STATUS.toString() + "=1",
            ImmutableMap.<String, Object> builder().put(NodeStatusAttribute.STATE.toString(), state).build());
    }

    private String getOssModelIdentity(final String nodeType) {
        if ("erbs".equalsIgnoreCase(nodeType)) {
            return OSSMosGenerator.ERBS_OSS_MODEL_IDENTITY;
        }
        if ("MSRBS_V1".equalsIgnoreCase(nodeType)) {
            return OSSMosGenerator.MSRBS_V1_OSS_MODEL_IDENTITY;
        }
        return OSSMosGenerator.RADIO_NODE_OSS_MODEL_IDENTITY; // RadioNode OMI
    }

    protected void checkArtifactMOAreCreatedInOrder(final String configurationFilesOrder) {
        String[] configurationFiles = configurationFilesOrder.split(",");
        for (String item : configurationFiles) {
            String[] keyValue = item.split("=");
            final String fdn = String.format("Project=%s,Node=%s,NodeArtifactContainer=1,NodeArtifact=%s", projectName, nodeName, keyValue[0]);
            final ManagedObject mo = dpsHelper.findMoByFdn(fdn);
            assertThat(mo).as("Managed object of " + fdn + " is null!").isNotNull();
            String artifactName = mo.getAttribute(NodeArtifactAttribute.NAME.toString());
            assertThat(artifactName.equals(keyValue[1]))
                    .as("Artifact file is not in expected order! Expected " + fdn + ":" + keyValue[1] + ", But got " + artifactName).isTrue();
        }
    }
}
