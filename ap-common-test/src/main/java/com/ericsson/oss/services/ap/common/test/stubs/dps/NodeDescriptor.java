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

import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.FILE_FORMAT;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.GEN_LOCATION;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.RAW_LOCATION;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.TYPE;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.model.NodeType;

/**
 * Describes the contents of an AP node.
 */
public final class NodeDescriptor {

    /* node */
    public static final String PROJECT_NAME = "Project1";
    public static final String PROJECT_FDN = "Project=" + PROJECT_NAME;
    public static final String NODE_NAME = "Node1";
    public static final String NODE_FDN = PROJECT_FDN + ",Node=" + NODE_NAME;
    public static final String IP_ADDRESS = "1.2.3.4";
    public static final String BACKUP_NAME = "\"RadioNode abc_def+0000.zip\"";
    public static final String HARDWARE_SERIAL_NUMBER_VALUE = "ABC1234567";
    public static final String NODE_IDENTIFIER_VALUE = "6607-651-025";
    public static final String USER_LABEL = "Athlone-East";
    public static final String OSS_PREFIX_VALUE = "SubNetwork=EnmSn";
    public static final String VALID_NODE_TYPE = "ERBS";
    public static final String VALID_NODE_TYPE_R6K_IN_AP = "Router60002";
    public static final String VALID_NODE_TYPE_R6K_IN_OSS = "Router6000-2";
    public static final String VALID_DEFAULT_ROUTER = "192.168.0.31";
    public static final String LATITUDE = "53.42911";
    public static final String LONGITUDE = "-7.94398";
    public static final String SHARED_CNF_NODE_TYPE = "SharedCNF";

    /* node security options */
    public static final String RBS_INTEGRITY_CODE_VALUE = "1234";

    private final Collection<Map<String, Object>> artifacts = new ArrayList<>();
    private final Map<String, Object> nodeAttributes = new HashMap<>();
    private final Map<String, Object> autoIntegrationOptions = new HashMap<>();
    private final Map<String, Object> licenseOptions = new HashMap<>();
    private final Map<String, Object> securityOptions = new HashMap<>();
    private final Map<String, Object> artifactContainerOptions = new HashMap<>();

    private String nodeType;
    private String nodeFdn;
    private String apNodeSpecificNamespace;
    private State nodeStatus;

    private NodeDescriptor() {
    }

    public String getProjectName() {
        return nodeFdn.split(",")[0].split("=")[1];
    }

    public String getProjectFdn() {
        return "Project=" + getProjectName();
    }

    public String getNodeName() {
        return nodeFdn.split(",")[1].split("=")[1];
    }

    public String getNodeFdn() {
        return nodeFdn;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getApNodeSpecificNamespace() {
        return apNodeSpecificNamespace;
    }

    public State getNodeStatus() {
        return nodeStatus;
    }

    public Map<String, Object> getNodeAttributes() {
        return nodeAttributes;
    }

    public Map<String, Object> getAutoIntegrationOptions() {
        return autoIntegrationOptions;
    }

    public Map<String, Object> getArtifactContainerOptions() {
        return artifactContainerOptions;
    }

    public Map<String, Object> getLicenseOptions() {
        return licenseOptions;
    }

    public Map<String, Object> getSecurityOptions() {
        return securityOptions;
    }

    public Collection<Map<String, Object>> getArtifacts() {
        return artifacts;
    }

    /**
     * Builds a <code>NodeDescriptor</code>.
     */
    public static class NodeDescriptorBuilder {
        private final NodeDescriptor nodeDescriptor = new NodeDescriptor();

        public NodeDescriptorBuilder(final NodeType nodeType) {
            nodeDescriptor.nodeFdn = NODE_FDN;
            nodeDescriptor.nodeAttributes.put(NODE_TYPE.toString(), nodeType.toString());
            nodeDescriptor.nodeType = nodeType.toString();
            nodeDescriptor.apNodeSpecificNamespace = "ap_" + getActualNodeType(nodeType.toString());
        }

        /**
         * Creates a NodeDescriptorBuilder instance with default configuration data of a node for test.
         *
         * @param nodeType
         *            the type of the node to create
         * @return a NodeDescriptorBuilder instance with default configuration data of a node for test
         */
        public static NodeDescriptorBuilder createDefaultNode(final NodeType nodeType) {
            return new NodeDescriptorBuilder(nodeType)
                    .withNodeAttribute(NodeAttribute.IPADDRESS.toString(), IP_ADDRESS)
                    .withNodeAttribute(NodeAttribute.NODE_IDENTIFIER.toString(), NODE_IDENTIFIER_VALUE)
                    .withNodeAttribute(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), HARDWARE_SERIAL_NUMBER_VALUE)
                    .withNodeStatus(State.READY_FOR_ORDER);
        }

        public static NodeDescriptorBuilder createDefaultEoiNode(final NodeType nodeType) {
            return new NodeDescriptorBuilder(nodeType)
                .withNodeAttribute(NodeAttribute.IPADDRESS.toString(), IP_ADDRESS)
                .withNodeAttribute(NodeAttribute.NODE_IDENTIFIER.toString(), NODE_IDENTIFIER_VALUE)
                .withNodeStatus(State.READY_FOR_EOI_INTEGRATION);
        }


        /**
         * Creates <code>NodeDescriptor</code> with configured values.
         *
         * @return <code>NodeDescriptor</code>
         */
        public NodeDescriptor build() {
            return nodeDescriptor;
        }

        public NodeDescriptorBuilder withNodeFdn(final String nodeFdn) {
            nodeDescriptor.nodeFdn = nodeFdn;
            nodeDescriptor.nodeAttributes.put(NodeAttribute.NAME.toString(), nodeDescriptor.getNodeName());
            return this;
        }

        public NodeDescriptorBuilder withNodeStatus(final State nodeStatus) {
            nodeDescriptor.nodeStatus = nodeStatus;
            return this;
        }

        public NodeDescriptorBuilder withNodeAttribute(final String name, final Object value) {
            nodeDescriptor.nodeAttributes.put(name, value);
            return this;
        }

        public NodeDescriptorBuilder withNodeAttributes(final Map<String, Object> nodeAttributes) {
            nodeDescriptor.nodeAttributes.putAll(nodeAttributes);
            return this;
        }

        public NodeDescriptorBuilder withArtifact(final String type, final String rawLocation, final String genLocation,
                                                  final String fileFormat) {
            final Map<String, Object> artifactAttrs = new HashMap<>();
            artifactAttrs.put(TYPE.toString(), type);
            artifactAttrs.put(RAW_LOCATION.toString(), rawLocation);
            artifactAttrs.put(GEN_LOCATION.toString(), genLocation);
            artifactAttrs.put(FILE_FORMAT.toString(), fileFormat);

            nodeDescriptor.artifacts.add(artifactAttrs);

            return this;
        }

        /**
         * Includes the specified autoIntegration option for the node. Use this method if you need to specify a value other than the default value in
         * the model.
         *
         * @param name
         *            the name of the autointegration option
         * @param value
         *            the value of the autointegration option
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withAutoIntegrationOption(final String name, final Object value) {
            nodeDescriptor.autoIntegrationOptions.put(name, value);
            return this;
        }

        /**
         * Includes the specified artifactContainer option for the node. Use this method if you need to specify a value other than the default value
         * in the model.
         *
         * @param name
         *            the name of the artifactContainer option
         * @param value
         *            the value of the artifactContainer option
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withArtifactContainerOption(final String name, final Object value) {
            nodeDescriptor.artifactContainerOptions.put(name, value);
            return this;
        }

        /**
         * Includes the specified license option for the node. Use this method if you need to specify a value other than the default value in the
         * model.
         *
         * @param name
         *            the name of the license option
         * @param value
         *            the value of the license option
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withLicenseOption(final String name, final Object value) {
            nodeDescriptor.licenseOptions.put(name, value);
            return this;
        }

        /**
         * Includes all of the specified autoIntegration options for the node. Use this method if you need to specify values other than the default
         * values in the model.
         *
         * @param aiOptions
         *            the autointegration option names and values
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withAutoIntegrationOptions(final Map<String, Object> aiOptions) {
            nodeDescriptor.autoIntegrationOptions.putAll(aiOptions);
            return this;
        }

        /**
         * Specify the security attributes for the node.
         * <p>
         * If not called then the node will contain no security options.
         * </p>
         *
         * @param securityOptions
         *            the security attributes for the node
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withSecurityOptions(final Map<String, Object> securityOptions) {
            nodeDescriptor.securityOptions.putAll(securityOptions);
            return this;
        }

        /**
         * Includes the specified security option for the node. Use this method if you need to specify a value other than the default value in the
         * model.
         *
         * @param name
         *            the name of the security option
         * @param value
         *            the value of the security option
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withSecurityOption(final String name, final Object value) {
            nodeDescriptor.securityOptions.put(name, value);
            return this;
        }

        /**
         * Sets the ap node specific namespace for the node type. Used for creation of AutoIntegrationOptions and Security managed objects. If not set
         * then defaults to ap_[nodeType].
         *
         * @param namespace
         *            the AP node specific namespace
         * @return <code>NodeDescriptorBuilder</code>
         */
        public NodeDescriptorBuilder withApNodeSpecificNamespace(final String namespace) {
            nodeDescriptor.apNodeSpecificNamespace = namespace;
            return this;
        }

        private static String getActualNodeType(final String nodeType) {
            if ("RadioNode".equalsIgnoreCase(nodeType)) {
                return "ecim";
            }

            return nodeType;
        }
    }
}
