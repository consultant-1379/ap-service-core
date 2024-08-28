/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.test.util.setup

import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.HEALTH_CHECK_PROFILE_NAME
import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.POST_REPORT_IDS
import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.PRE_REPORT_IDS
import static com.ericsson.oss.services.ap.common.model.MoType.HEALTH_CHECK
import static com.ericsson.oss.services.ap.common.model.MoType.NODE
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS
import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute

class MoCreatorSpec {

    private static RuntimeConfigurableDps dps

    private static final String NE_TYPE = "neType"
    private static final String SECURE_USER = "secureUserName"
    private static final String SECURE_PASSWORD = "secureUserPassword"
    private static final String VERSION = "1.0.0"

    /**
     * Sets the RuntimeConfigurableDps using the given instance. Must be called before using any other methods within this class.
     *
     * @param dps
     *          The dps instance to be used by the MoCreatorSpec
     */
    def static setDps(final RuntimeConfigurableDps dps) {
        this.dps = dps
    }

    /**
     * Create Node MO with optional attribute(s)
     * @param nodeFdn
     *          The FDN to create the node Mo with
     * @param parentMo
     *          The ProjectMo to create the node under
     * @param attributes
     *          OPTIONAL attribute(s) to add to the created node Mo
     * @return NodeMo
     */
    def static createNodeMo(final String nodeFdn, final ManagedObject parentMo, final Map<String, Object> attributes = new HashMap<String, Object>()) {
        if (!attributes.containsKey(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString())) {
            attributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), false)
        }
        return  dps.addManagedObject()
                .withFdn(nodeFdn)
                .type(NODE.toString())
                .namespace(AP.toString())
                .version(VERSION)
                .parent(parentMo)
                .addAttributes(attributes)
                .build()
    }

    /**
     * Creates a Project MO using a given FDN
     * @param projectFdn
     *          The FDN to create the project Mo with
     * @return ProjectMo
     */
    def static createProjectMo(String projectFdn) {
        return dps.addManagedObject()
                .withFdn(projectFdn)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version(VERSION)
                .build()
    }

    /**
     * Create Node Status MO with attributes
     * @param nodeFdn
     *          The FDN to create the node status Mo with
     * @param nodeMo
     *          The parent node Mo to change the status of
     * @param attributes
     *          OPTIONAL attribute(s) to be added to the NodeStatusMo
     * @return NodeStatusMo
     */
    def static createNodeStatusMo(final String nodeFdn, final ManagedObject nodeMo, final Map<String, Object> attributes = new HashMap<String, Object>()) {
        if (!attributes.containsKey(NodeStatusAttribute.STATUS_ENTRIES.toString())) {
            attributes.put(NodeStatusAttribute.STATUS_ENTRIES.toString(), new ArrayList<String>())
        }
        return dps.addManagedObject()
                .withFdn(nodeFdn + ",NodeStatus=1")
                .type(NODE_STATUS.toString())
                .namespace(AP.toString())
                .version(VERSION)
                .parent(nodeMo)
                .addAttributes(attributes)
                .build()
    }

    /**
     * Create ManagedElement MO with OssPrefix and attributes
     * @param nodeName
     *          The node name under which to create the ManagedElement MO
     * @param nodeType
     *          The type of node to create the MO for
     * @return ManagedElementMo
     */
    def static createManagedElementMo(final String nodeName, final String nodeType) {
        final Map<String, Object> managedElementAttributes = new HashMap<>()
        managedElementAttributes.put("managedElementId", nodeName)
        managedElementAttributes.put(NE_TYPE, nodeType)
        return dps.addManagedObject()
                .withFdn(String.format("SubNetwork=EnmSn,ManagedElement=%s", nodeName))
                .type("ManagedElement")
                .version(VERSION)
                .name(nodeName)
                .addAttributes(managedElementAttributes)
                .build()
    }

    /**
     * Create NetworkElement MO with attributes
     * @param nodeName
     *          The node name under which to create the NetworkElement MO
     * @param nodeType
     *          The type of node to create the NetworkElement MO for
     * @param persistenceObject
     *          The persistenceObject to use for the target
     * @return NetworkElementMo
     */
    def static createNetworkElementMo(final String nodeName, final String nodeType, final PersistenceObject persistenceObject) {
        final Map<String, Object> networkElementAttributes = new HashMap<>()
        networkElementAttributes.put("networkElementId", nodeName)
        networkElementAttributes.put(NE_TYPE, nodeType)
        networkElementAttributes.put("ossPrefix", "SubNetwork=EnmSn")
        networkElementAttributes.put("ossModelIdentity", "1998-184-092")
        networkElementAttributes.put("ipAddress", "22.22.22.22")

        return dps.addManagedObject()
                .withFdn(String.format("NetworkElement=%s", nodeName))
                .type("NetworkElement")
                .namespace("OSS_NE_DEF")
                .version(VERSION)
                .target(persistenceObject)
                .name(nodeName)
                .addAttributes(networkElementAttributes)
                .build()
    }

    /**
     * Create NetworkElement MO with specified attributes
     * @param nodeName
     *          The node name under which to create the NetworkElement MO
     * @param nodeType
     *          The type of node to create the NetworkElement MO for
     * @param persistenceObject
     *          The persistenceObject to use for the target
     * @param networkElementAttributes
     *          The networkElementAttributes to use for the attributes
     * @return NetworkElementMo
     */
    def static createNetworkElementMoWithAtrributes(final String nodeName, final PersistenceObject persistenceObject, final Map<String, Object> networkElementAttributes) {
        return dps.addManagedObject()
                .withFdn(String.format("NetworkElement=%s", nodeName))
                .type("NetworkElement")
                .namespace("OSS_NE_DEF")
                .version(VERSION)
                .target(persistenceObject)
                .name(nodeName)
                .addAttributes(networkElementAttributes)
                .build()
    }
    /**
     * Create CMFunction MO and attributes
     * @param networkElementMo
     *          The parent node Mo to change the status of
     * @param status
     *          The  sync status to use for the managed object being added
     * @return NetworkElementMo
     */
    def static createCmFunctionMo(final ManagedObject networkElementMo, final String status) {
        final Map<String, Object> cmFunctionAttributes = new HashMap<String, Object>()
        cmFunctionAttributes.put("syncStatus", status)

        return dps.addManagedObject().parent(networkElementMo)
        .type("CmFunction")
        .version("1.0.1")
        .name("1")
        .addAttributes(cmFunctionAttributes)
        .build()
    }

    /**
     * Create SecurityFunction MO and attributes
     * @param nodeName
     *          The node name under which to create SecurityFunction MO
     * @param networkElementMo
     *          The parent MO to use
     * @return SecurityFunctionMo
     */
    def static createSecurityFunctionMo(final String nodeName, final ManagedObject networkElementMo){
        return dps.addManagedObject()
                .withFdn(String.format("NetworkElement=%s,SecurityFunction=1", nodeName))
                .type("SecurityFunction")
                .namespace("OSS_NE_SEC_DEF")
                .version(VERSION)
                .name("1")
                .parent(networkElementMo)
                .build()
    }

    /**
     * Create NetworkElementSecurity MO and attributes
     * @param nodeName
     *          The node name under which to create NetworkElementSecurity MO
     * @param securityFunctionMo
     *          The parent Mo to use
     * @return NetworkElementSecurityMo
     */
    def static createNetworkElementSecurityMo(final String nodeName, final ManagedObject securityFunctionMo){
        final Map<String, Object> networkElementSecurityAttributes = new HashMap<>()
        networkElementSecurityAttributes.put(SECURE_USER, SECURE_USER)
        networkElementSecurityAttributes.put(SECURE_PASSWORD, SECURE_PASSWORD)
        return dps.addManagedObject()
                .withFdn(String.format("NetworkElement=%s,SecurityFunction=1,NetworkElementSecurity=1", nodeName))
                .type("NetworkElementSecurity")
                .version(VERSION)
                .name("1")
                .parent(securityFunctionMo)
                .addAttributes(networkElementSecurityAttributes)
                .build()
    }

    /**
     * Create Node Artifact MO with attributes
     * @param artifactFdn
     *          The FDN to create the artifact Mo with
     * @param nodeMo
     *          The parent node Mo to add the artifact to
     * @param attributes
     *          OPTIONAL attribute(s) to be added to the NodeArtifactMo
     * @return NodeStatusMo
     */
    def static createNodeArtifactMo(String artifactFdn, ManagedObject nodeMo, def attributes) {
        return dps.addManagedObject()
                .withFdn(artifactFdn)
                .type(MoType.NODE_ARTIFACT.toString())
                .namespace(AP.toString())
                .version("2.0.0")
                .parent(nodeMo)
                .addAttributes(attributes)
                .build()
    }

    /**
     * Create HealthCheck MO and attributes
     * @param nodeFdn
     *          The node fdn under which to create HealthCheck MO
     * @param nodeMo
     *          The parent Mo to use
     * @param healthCheckProfileName
     *          HealthCheckProfileName attribute
     * @return HealthCheckMo
     */
    def static createHealthCheckMo(final String nodeFdn, final ManagedObject nodeMo, final String healthCheckProfileName) {
        final Map<String, Object> healthCheckAttributes = new HashMap<>()
        healthCheckAttributes.put(HEALTH_CHECK_PROFILE_NAME.toString(), healthCheckProfileName)
        healthCheckAttributes.put(PRE_REPORT_IDS.toString(), new ArrayList<String>())
        healthCheckAttributes.put(POST_REPORT_IDS.toString(), new ArrayList<String>())

        return dps.addManagedObject()
                .withFdn(nodeFdn + ",HealthCheck=1")
                .type(HEALTH_CHECK.toString())
                .namespace(AP.toString())
                .version("1.0.0")
                .parent(nodeMo)
                .addAttributes(healthCheckAttributes)
                .build()
    }
}
