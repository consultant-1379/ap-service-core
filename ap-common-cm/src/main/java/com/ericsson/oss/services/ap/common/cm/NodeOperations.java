/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.cm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerNonCDIImpl;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.model.CmNodeHeartbeatSupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.FmAlarmSupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.InventorySupervisionAttribute;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.PmFunctionAttribute;
import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Common set of node operations such as adding and removing a NetworkElement. Supports invocation in both CDI and NON-CDI contexts.
 */
public class NodeOperations {

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_INTERVAL_IN_MS = 100;
    private static final double RETRY_EXPONENTIAL_BACKOFF = 2.0;
    private static final String NETWORK_ELEMENT_NAMESPACE = "OSS_NE_DEF";
    private static final String GEO_NAMESPACE = "OSS_GEO";
    private static final String MANAGEMENT_STATE_ELEMENT = "managementState";
    private static final String MAINTENANCE = "MAINTENANCE";
    private static final String NORMAL = "NORMAL";
    private static final String MANUAL = "MANUAL";
    private static final String AUTOMATIC = "AUTOMATIC";
    private static final String TARGET_GROUPS = "targetGroups";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    private ModelReader modelReader;

    /**
     * Adds the node to ENM. On successful execution the NetworkElement and ConnectivityInformation MOs will be created.
     * GeographicLocation and GeometricPoint MOs are created if location is included in NodeInfo
     *
     * @param addNodeRequest request containing details for adding the node
     */
    public void addNode(final AddNodeRequest addNodeRequest) {
        final String nodeFdn = addNodeRequest.getNodeFdn();
        final Map<String, Object> apNodeMoAttrs = getDpsOperations().readMoAttributes(nodeFdn);
        logger.info("Retrieved AP node {} with attributes {}", nodeFdn, apNodeMoAttrs);
        createNetworkElementInNewTx(addNodeRequest, apNodeMoAttrs);

        try {
            createConnectivityInfoInNewTx(addNodeRequest, apNodeMoAttrs);
            createGeoLocationMos(addNodeRequest, apNodeMoAttrs);
        } catch (final Exception e) {
            deleteNetworkElementInNewTx(addNodeRequest.getNodeName());
            throw e;
        }
    }

    /**
     * Adds the eoi node to ENM. On successful execution the NetworkElement and ConnectivityInformation MOs will be created.
     * GeographicLocation and GeometricPoint MOs are created if location is included in NodeInfo
     *
     * @param addNodeRequest request containing details for adding the node
     */
    public void eoiAddNode(final AddNodeRequest addNodeRequest) {
        logger.info("eoiAddNode method with addNodeRequest:{}", addNodeRequest);
        final String nodeFdn = addNodeRequest.getNodeFdn();
        final Map<String, Object> apNodeMoAttrs = getDpsOperations().readMoAttributes(nodeFdn);
        logger.info("Retrieved EOI AP node {} with attributes {}", nodeFdn, apNodeMoAttrs);
        createNetworkElementInNewTx(addNodeRequest, apNodeMoAttrs);

        try {
            createConnectivityInfoInNewTx(addNodeRequest, apNodeMoAttrs);
        } catch (final Exception e) {
            deleteNetworkElementInNewTx(addNodeRequest.getNodeName());
            throw e;
        }
    }

    /**
     * Updates NetworkElement with provided attribute name and value
     *
     * @param nodeName  the name of the AP node and NetworkElement
     * @param attrName  the name of attribute to update
     * @param attrValue the value of attribute to update
     */
    public void updateNode(final String nodeName, final String attrName, final String attrValue) {
        final String networkElementFdn = getNetworkElementFdn(nodeName);
        getDpsOperations().updateMo(networkElementFdn, attrName, attrValue);
    }

    /**
     * Updates NetworkElement with provided attributes
     *
     * @param nodeName   the name of the AP node and NetworkElement
     * @param attributes the attributes to update
     */
    public void updateNode(final String nodeName, final Map<String, Object> attributes) {
        final String networkElementFdn = getNetworkElementFdn(nodeName);
        getDpsOperations().updateMo(networkElementFdn, attributes);
    }

    /**
     * Removes the node from ENM. On successful execution the NetworkElement along with all children are removed, and all supervisions are disabled.
     *
     * @param nodeName the name of the AP node and NetworkElement
     */
    public void removeNode(final String nodeName) {
        setCmHeartbeatSupervisionInNewTx(nodeName, false);
        setFmSupervisionInNewTx(nodeName, false);
        setPmSupervisionInNewTx(nodeName, false);
        setInvSupervisionInNewTx(nodeName, false);
        deleteNrmDataFromEnmInNewTx(nodeName);
        deleteNetworkElementInNewTx(nodeName);
    }

    /**
     * Removes the node from ENM. On successful execution the NetworkElement along with all children are removed, and all supervisions are disabled.
     *
     * @param nodeName
     *            the name of the AP node and NetworkElement
     */
    public void eoiRemoveNode(final String nodeName) {
        setCmHeartbeatSupervisionInNewTx(nodeName, false);
        setFmSupervisionInNewTx(nodeName, false);
        setPmSupervisionInNewTx(nodeName, false);
        deleteNrmDataFromEnmInNewTx(nodeName);
        deleteNetworkElementInNewTx(nodeName);
    }

    /**
     * Turns on heart beat supervision for the node.
     *
     * @param nodeName the name of the AP node and NetworkElement
     */
    public void activateCmNodeHeartbeatSupervision(final String nodeName) {
        setCmHeartbeatSupervisionInNewTx(nodeName, true);
    }

    /**
     * Get {@link SupervisionMoType} for the specified node.
     *
     * @param nodeName          the node name
     * @param supervisionMoType the supervision mo type
     * @return supervision state
     */
    public boolean getSupervisionStatus(final String nodeName, final SupervisionMoType supervisionMoType) {
        boolean supervisionStatus = false;
        switch (supervisionMoType) {
            case CM:
                supervisionStatus = getCmHeartbeatSupervisionInNewTx(nodeName);
                break;
            case PM:
                supervisionStatus = getPmSupervisionInNewTx(nodeName);
                break;
            case FM:
                supervisionStatus = getFmSupervisionInNewTx(nodeName);
                break;
            case INVENTORY:
                supervisionStatus = getInvSupervisionInNewTx(nodeName);
                break;
            default:
                logger.info("No expected supervision {} for node {}", supervisionMoType, nodeName);
        }
        return supervisionStatus;
    }

    /**
     * Turns on {@link SupervisionMoType} for the specified node.
     *
     * @param nodeName          the node name
     * @param supervisionMoType the supervision mo type
     */
    public void setSupervisionStatus(final String nodeName, final SupervisionMoType supervisionMoType,final boolean isSupervisionToEnable) {
        switch (supervisionMoType) {
            case PM:
                setPmSupervisionInNewTx(nodeName, isSupervisionToEnable);
                break;
            case FM:
                setFmSupervisionInNewTx(nodeName, isSupervisionToEnable);
                break;
            case INVENTORY:
                setInvSupervisionInNewTx(nodeName, isSupervisionToEnable);
                break;
            case CM:
                setCmHeartbeatSupervisionInNewTx(nodeName, isSupervisionToEnable);
                break;
            default:
                logger.info("No supervision match found for {} for node {}", supervisionMoType, nodeName);
        }
    }

    /**
     * fetch the TargetGroups of the specified node.
     *
     * @param apNodeFdn the node name
     * @return a list of target groups
     */
    public List<String> getTargetGroup(final String apNodeFdn) {
        final String securityFdn = getSecurityFdn(apNodeFdn);
        List<String> targetGroupList = new ArrayList<>();
        if (getDpsOperations().existsMoByFdn(securityFdn)) {
            final Map<String, Object> securityAttributes = getDpsOperations().readMoAttributes(getSecurityFdn(apNodeFdn));
            if (securityAttributes != null && securityAttributes.get(TARGET_GROUPS) != null) {
                final Object targetGroup = securityAttributes.get(TARGET_GROUPS);
                if (targetGroup instanceof String) {
                    targetGroupList = new ArrayList<>(Arrays.asList(((String) targetGroup).split(",")));
                }
            }
        }
        return targetGroupList;
    }

    /**
     * Set the maintenance state for the NetworkElement of the specified node.
     *
     * @param apNodeFdn the node name
     * @param state     the state could be MAINTENANCE or NORMAL
     */
    public void setManagementState(final String apNodeFdn, final String state) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        getDpsOperations().updateMo(getNetworkElementFdn(nodeName), NetworkElementAttribute.MANAGEMENT_STATE.toString(), state);
        logger.info("update of networkElement {} attribute managementState {}", nodeName, state);
    }

    /**
     * Checks if an update on the Network Element managementState is required and valid.
     *
     * @param state     the state could be MAINTENANCE or NORMAL
     * @param apNodeFdn the node name
     * @return true if the update is valid, false if not
     */
    public boolean isUpdateValid(final String state, final String apNodeFdn) {
        String maintenanceMode = StringUtils.EMPTY;
        if (NORMAL.equals(state)) {
            maintenanceMode = fetchMaintenanceMode(apNodeFdn);
        }
        return !MANUAL.equals(maintenanceMode);
    }

    private void createNetworkElementInNewTx(final AddNodeRequest addNodeRequest, final Map<String, Object> apNodeAttributes) {
        final String ossModelIdentity = (String) apNodeAttributes.get(NodeAttribute.NODE_IDENTIFIER.toString());
        final String ossPrefix = (String) apNodeAttributes.get(NodeAttribute.OSS_PREFIX.toString());
        final String nodeType = nodeTypeMapper.toOssRepresentation((String) apNodeAttributes.get(NodeAttribute.NODE_TYPE.toString()));
        final String timeZone = (String) apNodeAttributes.get(NodeAttribute.TIMEZONE.toString());
        final String userLabel = (String) apNodeAttributes.get(NodeAttribute.USER_LABEL.toString());
        final ModelData modelData = getModelReader().getLatestPrimaryTypeModel(NETWORK_ELEMENT_NAMESPACE, MoType.NETWORK_ELEMENT.toString());

        final Map<String, Object> attributesNetworkElement = getNetworkElementAttributes(addNodeRequest, ossModelIdentity, ossPrefix, nodeType,
            timeZone, userLabel);

        final RetriableCommand<?> createNetworkElementCommand = (final RetryContext retryContext) -> {
            logger.info("Creating NetworkElement for AP node {} with attributes {}", addNodeRequest.getNodeFdn(), attributesNetworkElement);
            getDpsOperations().createRootMo(getNetworkElementFdn(addNodeRequest.getNodeName()), modelData, attributesNetworkElement);
            return null;
        };
        executeRetriableCommand(createNetworkElementCommand);
    }

    private static Map<String, Object> getNetworkElementAttributes(final AddNodeRequest addNodeRequest, final String ossModelIdentity,
        final String ossPrefix, final String nodeType, final String timeZone, final String userLabel) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NetworkElementAttribute.NE_TYPE.toString(), nodeType);
        attributes.put(NetworkElementAttribute.NETWORK_ELEMENT_ID.toString(), addNodeRequest.getNodeName());

        attributes.put(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString(), ossModelIdentity);
        attributes.put(NetworkElementAttribute.MANAGEMENT_STATE.toString(), MAINTENANCE);
        if (StringUtils.isNotBlank(ossPrefix)) {
            attributes.put(NetworkElementAttribute.OSS_PREFIX.toString(), ossPrefix);
        }
        if (StringUtils.isNotBlank(timeZone)) {
            attributes.put(NetworkElementAttribute.TIMEZONE.toString(), timeZone);
        }
        if (StringUtils.isNotBlank(userLabel)) {
            attributes.put(NetworkElementAttribute.USER_LABEL.toString(), userLabel);
        }
        attributes.putAll(addNodeRequest.getNetworkElementAttributes());
        return attributes;
    }

    private void createGeoLocationMos(final AddNodeRequest addNodeRequest, final Map<String, Object> apNodeMoAttrs) {
        final Map<String, Object> nodeLocationAttributes = (Map<String, Object>) apNodeMoAttrs.get(NodeAttribute.NODE_LOCATION.toString());

        if (MapUtils.isNotEmpty(nodeLocationAttributes)) {
            createGeographicLocationInNewTx(addNodeRequest);
            createGeometricPointInNewTx(addNodeRequest, nodeLocationAttributes);
        }
    }

    private void createGeographicLocationInNewTx(final AddNodeRequest addNodeRequest) {
        final String networkElementFdn = getNetworkElementFdn(addNodeRequest.getNodeName());
        final Map<String, Object> geographicLocationAttributes = new HashMap<>();

        final ModelData modelData = getModelReader().getLatestPrimaryTypeModel(GEO_NAMESPACE, MoType.GEOGRAPHIC_LOCATION.toString());
        final String geographicLocationFdn = networkElementFdn + "," + MoType.GEOGRAPHIC_LOCATION.toString() + "=1";
        logger.info("Creating {} MO for AP node {} with attributes {}", MoType.GEOGRAPHIC_LOCATION, addNodeRequest.getNodeFdn(),
            geographicLocationAttributes);

        getDpsOperations().createRootMo(geographicLocationFdn, modelData, geographicLocationAttributes);
    }

    private void createGeometricPointInNewTx(final AddNodeRequest addNodeRequest, final Map<String, Object> nodeLocationAttributes) {
        final String geographicLocationFdn = getGeographicLocationFdn(addNodeRequest.getNodeName());
        final double longitudeDouble = Double.parseDouble((String) nodeLocationAttributes.get(LONGITUDE));
        final double latitudeDouble = Double.parseDouble((String) nodeLocationAttributes.get(LATITUDE));

        final Map<String, Object> geometricPointAttributes = new HashMap<>();
        geometricPointAttributes.put(LONGITUDE, longitudeDouble);
        geometricPointAttributes.put(LATITUDE, latitudeDouble);

        final ModelData modelData = getModelReader().getLatestPrimaryTypeModel(GEO_NAMESPACE, MoType.GEOMETRIC_POINT.toString());
        final String geometricPointFdn = geographicLocationFdn + "," + MoType.GEOMETRIC_POINT.toString() + "=1";
        logger.info("Creating {} MO for AP node {} with attributes {}", MoType.GEOMETRIC_POINT, addNodeRequest.getNodeFdn(),
            nodeLocationAttributes);

        getDpsOperations().createRootMo(geometricPointFdn, modelData, geometricPointAttributes);
    }

    private void createConnectivityInfoInNewTx(final AddNodeRequest addNodeRequest, final Map<String, Object> apNodeAttributes) {
        final String networkElementFdn = getNetworkElementFdn(addNodeRequest.getNodeName());
        final String ipAddress = (String) apNodeAttributes.get(NodeAttribute.IPADDRESS.toString());

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NetworkElementAttribute.IP_ADDRESS.toString(), ipAddress);
        attributes.putAll(addNodeRequest.getConnInfoAttributes());

        final ModelData modelData = getModelReader().getLatestPrimaryTypeModel(addNodeRequest.getConnInfoNamespace(),
            addNodeRequest.getConnInfoModelName());

        final String connectivityInformationFdn = networkElementFdn + "," + addNodeRequest.getConnInfoModelName() + "=1";
        logger.info("Creating {} MO for AP node {} with attributes {}", addNodeRequest.getConnInfoModelName(), addNodeRequest.getNodeFdn(),
            attributes);
        getDpsOperations().createRootMo(connectivityInformationFdn, modelData, attributes);
    }


    private void setCmHeartbeatSupervisionInNewTx(final String nodeName, final boolean enableSupervision) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.CM_NODE_HEARTBEAT_SUPERVISION.toString() + "=1";
        getDpsOperations().updateMo(supervisionFdn, CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString(), enableSupervision);
    }

    private boolean getCmHeartbeatSupervisionInNewTx(final String nodeName) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.CM_NODE_HEARTBEAT_SUPERVISION.toString() + "=1";
        return (boolean) getDpsOperations().readMoAttributes(supervisionFdn).get(CmNodeHeartbeatSupervisionAttribute.ACTIVE.toString());
    }

    private void setFmSupervisionInNewTx(final String nodeName, final boolean enableSupervision) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.FM_ALARM_SUPERVISION.toString() + "=1";
        getDpsOperations().updateMo(supervisionFdn, FmAlarmSupervisionAttribute.ACTIVE.toString(), enableSupervision);
    }

    private boolean getFmSupervisionInNewTx(final String nodeName) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.FM_ALARM_SUPERVISION.toString() + "=1";
        return (boolean) getDpsOperations().readMoAttributes(supervisionFdn).get(FmAlarmSupervisionAttribute.ACTIVE.toString());
    }

    private void setPmSupervisionInNewTx(final String nodeName, final boolean enableSupervision) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.PM_FUNCTION.toString() + "=1";
        getDpsOperations().updateMo(supervisionFdn, PmFunctionAttribute.PM_ENABLED.toString(), enableSupervision);
    }

    private boolean getPmSupervisionInNewTx(final String nodeName) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.PM_FUNCTION.toString() + "=1";
        return (boolean) getDpsOperations().readMoAttributes(supervisionFdn).get(PmFunctionAttribute.PM_ENABLED.toString());
    }

    private void setInvSupervisionInNewTx(final String nodeName, final boolean enableSupervision) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.INV_SUPERVISION.toString() + "=1";
        getDpsOperations().updateMo(supervisionFdn, InventorySupervisionAttribute.ACTIVE.toString(), enableSupervision);
    }

    private boolean getInvSupervisionInNewTx(final String nodeName) {
        final String supervisionFdn = getNetworkElementFdn(nodeName) + "," + MoType.INV_SUPERVISION.toString() + "=1";
        return (boolean) getDpsOperations().readMoAttributes(supervisionFdn).get(InventorySupervisionAttribute.ACTIVE.toString());
    }

    private void deleteNrmDataFromEnmInNewTx(final String nodeName) {
        final String networkElementFdn = getNetworkElementFdn(nodeName);
        final String cmFunctionFdn = networkElementFdn + "," + MoType.CM_FUNCTION + "=1";
        try {
            getDpsOperations().performMoAction(cmFunctionFdn, "deleteNrmDataFromEnm");
        } catch (final ApServiceException e) {
            logger.warn("Error performing action deleteNrmDataFromEnm for MO {}: {}", networkElementFdn, e.getCause().getMessage(), e);
        }
    }

    private void deleteNetworkElementInNewTx(final String nodeName) {
        final RetriableCommand<?> deleteNetworkElementCommand = (final RetryContext retryContext) -> {
            getDpsOperations().deleteMo(getNetworkElementFdn(nodeName));
            return null;
        };
        executeRetriableCommand(deleteNetworkElementCommand);
    }

    private static void executeRetriableCommand(final RetriableCommand<?> retriableCommand) {
        final RetryManager retryManager = new RetryManagerNonCDIImpl();

        try {
            retryManager.executeCommand(getRetryPolicy(), retriableCommand);
        } catch (final RetriableCommandException e) {
            throw new ApServiceException(e.getMessage(), e);
        }
    }

    private static RetryPolicy getRetryPolicy() {
        return RetryPolicy.builder()
            .attempts(MAX_RETRIES)
            .waitInterval(RETRY_INTERVAL_IN_MS, TimeUnit.MILLISECONDS)
            .exponentialBackoff(RETRY_EXPONENTIAL_BACKOFF)
            .retryOn(Exception.class)
            .build();
    }

    private static String getNetworkElementFdn(final String nodeName) {
        return MoType.NETWORK_ELEMENT.toString() + "=" + nodeName;
    }

    private static String getGeographicLocationFdn(final String nodeName) {
        return MoType.NETWORK_ELEMENT.toString() + "=" + nodeName + "," + MoType.GEOGRAPHIC_LOCATION.toString() + "=1";
    }

    private DpsOperations getDpsOperations() {
        if (dpsOperations == null) {
            dpsOperations = new DpsOperations();
        }
        return dpsOperations;
    }

    private ModelReader getModelReader() {
        if (modelReader == null) {
            modelReader = new ModelReader();
        }
        return modelReader;
    }

    private String fetchMaintenanceMode(final String apNodeFdn) {
        final String supervisionOptionsFdn = String.format("%s,%s=1", apNodeFdn, MoType.SUPERVISION_OPTIONS.toString());
        Object maintenanceMode = null;
        if (getDpsOperations().existsMoByFdn(supervisionOptionsFdn)) {
            maintenanceMode = getDpsOperations().readMoAttributes(supervisionOptionsFdn).get(MANAGEMENT_STATE_ELEMENT);
        }
        return maintenanceMode != null ? maintenanceMode.toString() : AUTOMATIC;
    }

    private static String getSecurityFdn(final String apNodeFdn) {
        return new StringBuilder()
            .append(apNodeFdn)
            .append(",")
            .append(MoType.SECURITY.toString())
            .append("=1")
            .toString();
    }
}
