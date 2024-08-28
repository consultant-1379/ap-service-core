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
package com.ericsson.oss.services.ap.common.test.stubs.dps;

import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_ARTIFACT;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_ARTIFACT_CONTAINER;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS;
import static com.ericsson.oss.services.ap.common.model.MoType.NOTIFICATION;
import static com.ericsson.oss.services.ap.common.model.MoType.SECURITY;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.NODE_ARTIFACT_ID;

import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps;

/**
 * Generate MO data in stubbed DPS.
 */
public class StubbedDpsGenerator {

    private static final String VERSION = "1.0.0";

    private final RuntimeConfigurableDps configurableDps;
    private final DataPersistenceService dps;

    public StubbedDpsGenerator() {
        configurableDps = new RuntimeConfigurableDps();
        dps = configurableDps.build();
    }

    /**
     * Generate the AP node MO and its child MOs based on the supplied node description.
     *
     * @param nodeDescriptor
     *            the description of the AP node
     * @return the generated project which contains all child MOs created for the node
     */
    public ManagedObject generate(final NodeDescriptor nodeDescriptor) {
        final ManagedObject nodeMo = createNodeManagedObject(nodeDescriptor);
        createNodeStatusMo(nodeDescriptor, nodeMo);
        createNodeArtifactMos(nodeDescriptor, nodeMo);
        createAutoIntegrationOptionsMo(nodeDescriptor, nodeMo);
        createNodeSecurityMo(nodeDescriptor, nodeMo);
        createLicenseOptionsMo(nodeDescriptor, nodeMo);

        return nodeMo;
    }

    /**
     * Get the stubbed DPS.
     *
     * @return the stubbed DPS
     */
    public DataPersistenceService getStubbedDps() {
        return dps;
    }

    private ManagedObject createNodeManagedObject(final NodeDescriptor nodeDescriptor) {
        final String nodeFdn = nodeDescriptor.getNodeFdn();

        return configurableDps.addManagedObject()
            .withFdn(nodeFdn)
            .namespace(AP.toString())
            .version(VERSION)
            .type(NODE.toString())
            .addAttributes(nodeDescriptor.getNodeAttributes())
            .generateTree()
            .build();
    }

    private void createNodeStatusMo(final NodeDescriptor nodeDescriptor, final ManagedObject nodeMo) {
        if (nodeDescriptor.getNodeStatus() == null) {
            return;
        }

        final String nodeStatusFdn = getNodeStatusFdn(nodeDescriptor.getNodeFdn());

        configurableDps.addManagedObject()
            .withFdn(nodeStatusFdn)
            .type(NODE_STATUS.toString())
            .namespace(AP.toString())
            .version("1.0.0")
            .addAttribute("state", nodeDescriptor.getNodeStatus().toString())
            .parent(nodeMo)
            .build();
    }

    private void createNodeArtifactMos(final NodeDescriptor nodeDescriptor, ManagedObject nodeMo) {
        if (nodeDescriptor.getArtifacts().isEmpty()) {
            return;
        }

        final ManagedObject artifactContainerMo = addNodeArtifactContainerMo(nodeDescriptor, nodeMo);

        int i = 0;
        for (final Map<String, Object> artifactAttrs : nodeDescriptor.getArtifacts()) {
            final String artifactFdn = getArtifactFdn(nodeDescriptor.getNodeFdn(), ++i);
            artifactAttrs.put(NODE_ARTIFACT_ID.toString(), i);

            configurableDps.addManagedObject()
                .withFdn(artifactFdn)
                .namespace(AP.toString())
                .version(VERSION)
                .type(NODE_ARTIFACT.toString())
                .addAttributes(artifactAttrs)
                .parent(artifactContainerMo)
                .build();
        }
    }

    private ManagedObject addNodeArtifactContainerMo(final NodeDescriptor nodeDescriptor, final ManagedObject nodeMo) {
        final String artifactContainerFdn = getArtifactContainerFdn(nodeDescriptor.getNodeFdn());

        return configurableDps.addManagedObject()
            .withFdn(artifactContainerFdn)
            .namespace(AP.toString())
            .version(VERSION)
            .type(NODE_ARTIFACT_CONTAINER.toString())
            .addAttributes(nodeDescriptor.getArtifactContainerOptions())
            .parent(nodeMo)
            .build();
    }

    private void createAutoIntegrationOptionsMo(final NodeDescriptor nodeDescriptor, final ManagedObject nodeMo) {
        if (nodeDescriptor.getAutoIntegrationOptions().isEmpty()) {
            return;
        }

        final String aiOptionsFdn = getAIOptionsFdn(nodeDescriptor.getNodeFdn());

        configurableDps.addManagedObject()
            .withFdn(aiOptionsFdn)
            .namespace(nodeDescriptor.getApNodeSpecificNamespace())
            .version(VERSION)
            .type(AI_OPTIONS.toString())
            .addAttributes(nodeDescriptor.getAutoIntegrationOptions())
            .parent(nodeMo)
            .build();
    }

    private void createLicenseOptionsMo(final NodeDescriptor nodeDescriptor, final ManagedObject nodeMo) {
        if (nodeDescriptor.getLicenseOptions().isEmpty()) {
            return;
        }

        final String licenseOptionsFdn = getLicenseOptionsFdn(nodeDescriptor.getNodeFdn());

        configurableDps.addManagedObject()
            .withFdn(licenseOptionsFdn)
            .namespace(nodeDescriptor.getApNodeSpecificNamespace())
            .version(VERSION)
            .type(LICENSE_OPTIONS.toString())
            .addAttributes(nodeDescriptor.getLicenseOptions())
            .parent(nodeMo)
            .build();
    }

    private void createNodeSecurityMo(final NodeDescriptor nodeDescriptor, final ManagedObject nodeMo) {
        if (nodeDescriptor.getSecurityOptions().isEmpty()) {
            return;
        }

        final String securityFdn = getSecurityFdn(nodeDescriptor.getNodeFdn());

        configurableDps.addManagedObject()
            .withFdn(securityFdn)
            .namespace(nodeDescriptor.getApNodeSpecificNamespace())
            .version(VERSION)
            .type(SECURITY.toString())
            .addAttributes(nodeDescriptor.getSecurityOptions())
            .parent(nodeMo)
            .build();
    }

    private static String getNodeStatusFdn(final String nodeFdn) {
        return nodeFdn + "," + NODE_STATUS.toString() + "=1";
    }

    private static String getArtifactContainerFdn(final String nodeFdn) {
        return nodeFdn + "," + NODE_ARTIFACT_CONTAINER.toString() + "=1";
    }

    private static String getArtifactFdn(final String nodeFdn, final int index) {
        return getArtifactContainerFdn(nodeFdn) + "," + NODE_ARTIFACT.toString() + "=" + index;
    }

    public String getAIOptionsFdn(final String nodeFdn) {
        return nodeFdn + "," + AI_OPTIONS.toString() + "=1";
    }

    public String getLicenseOptionsFdn(final String nodeFdn) {
        return nodeFdn + "," + LICENSE_OPTIONS.toString() + "=1";
    }

    public String getSecurityFdn(final String nodeFdn) {
        return nodeFdn + "," + SECURITY.toString() + "=1";
    }

    public String getNotificationFdn(final String nodeFdn) {
        return nodeFdn + "," + NOTIFICATION.toString() + "=1";
    }
}
