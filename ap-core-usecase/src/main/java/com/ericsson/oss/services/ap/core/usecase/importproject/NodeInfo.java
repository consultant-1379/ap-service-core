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
package com.ericsson.oss.services.ap.core.usecase.importproject; // NOPMD - Too many public methods

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;

/**
 * Data read from nodeInfo.xml.
 */
public class NodeInfo { // NOPMD - Too many fields

    private String name;
    private String nodeType;
    private String nodeIdentifier;
    private String ipAddress;
    private String userLabel;
    private String ossPrefix;
    private String hardwareSerialNumber;
    private String timeZone;
    private String workOrderId;
    private Map<String, Object> nodeLocation;
    private Map<String, Object> nodeAttributes;
    private Map<String, Object> securityAttributes;
    private Map<String, Object> autoIntegrationAttributes;
    private Map<String, Object> nodeUserCredentials;
    private Map<String, List<String>> nodeArtifacts;
    private Map<String, List<String>> configurations;
    private Map<String, Object> supervisionAttributes;
    private Map<String, Object> licenseAttributes;
    private AutomaticLicenseRequestData automaticLicenseReqAttributes;
    private Map<String, Object> configurationAttributes;
    private Map<String, Object> notifications;
    private Map<String, Object> dhcpAttributes;
    private Map<String, Object> controllingNodesAttributes;
    private Map<String, Object> healthCheckAttributes;
    private boolean autoRestoreOnFail;
    private boolean isHardwareReplaceNode;
    private boolean isReconfig;
    private String deployment;
    private ActivityType activity = ActivityType.GREENFIELD_ACTIVITY;
    private Map<String, String> remoteNodeNames;
    private Map<String, String> ignoreErrors;
    private String backupName;
    private String defaultRouterAddress;
    private List<ArtifactDetails> artifactDetailsInStrictSequence;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public void setNodeIdentifier(final String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserLabel() {
        return userLabel;
    }

    public void setUserLabel(final String userLabel) {
        this.userLabel = userLabel;
    }

    public String getOssPrefix() {
        return ossPrefix;
    }

    public void setOssPrefix(final String ossPrefix) {
        this.ossPrefix = ossPrefix;
    }

    public String getHardwareSerialNumber() {
        return hardwareSerialNumber;
    }

    public void setHardwareSerialNumber(final String hardwareSerialNumber) {
        this.hardwareSerialNumber = hardwareSerialNumber;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(final String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public Map<String, Object> getNodeAttributes() {
        return nodeAttributes;
    }

    public void setNodeAttributes(final Map<String, Object> nodeAttributes) {
        this.nodeAttributes = nodeAttributes;
    }

    public Map<String, Object> getIntegrationAttributes() {
        return autoIntegrationAttributes;
    }

    public void setIntegrationAttributes(final Map<String, Object> autoIntegrationAttributes) {
        this.autoIntegrationAttributes = autoIntegrationAttributes;
    }

    public Map<String, Object> getSecurityAttributes() {
        return securityAttributes;
    }

    public void setSecurityAttributes(final Map<String, Object> securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    public Map<String, Object> getNodeUserCredentialAttributes() {
        return nodeUserCredentials;
    }

    public void setNodeUserCredentialAttributes(final Map<String, Object> nodeUserCredentials) {
        this.nodeUserCredentials = nodeUserCredentials;
    }

    public Map<String, List<String>> getNodeArtifacts() {
        return nodeArtifacts;
    }

    public void setNodeArtifacts(final Map<String, List<String>> nodeArtifacts) {
        this.nodeArtifacts = nodeArtifacts;
    }

    public Map<String, List<String>> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(final Map<String, List<String>> configurations) {
        this.configurations = configurations;
    }

    public Map<String, Object> getSupervisionAttributes() {
        return supervisionAttributes;
    }

    public void setSupervisionAttributes(final Map<String, Object> supervisionAttributes) {
        this.supervisionAttributes = supervisionAttributes;
    }

    public boolean isHardwareReplaceNode() {
        return isHardwareReplaceNode;
    }

    public void setHardwareReplaceNode(final boolean isHardwareReplaceNode) {
        this.isHardwareReplaceNode = isHardwareReplaceNode;
    }

    public boolean isReconfig() {
        return isReconfig;
    }

    public void setReconfig(final boolean isReconfig) {
        this.isReconfig = isReconfig;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(final String deployment) {
        this.deployment = deployment;
    }

    public void setLicenseAttributes(final Map<String, Object> licenseAttributes) {
        this.licenseAttributes = licenseAttributes;
    }

    public Map<String, Object> getLicenseAttributes() {
        if (licenseAttributes == null) {
            return Collections.emptyMap();
        }
        return new HashMap<>(licenseAttributes);
    }

    public void setAutomaticLicenseReqAttributes(final AutomaticLicenseRequestData automaticLicenseReqAttributes) {
        this.automaticLicenseReqAttributes = automaticLicenseReqAttributes;
    }

    public AutomaticLicenseRequestData getAutomaticLicenseReqAttributes() {
        return automaticLicenseReqAttributes;
    }

    public Map<String, Object> getConfigurationAttributes() {
        return configurationAttributes;
    }

    public void setConfigurationAttributes(final Map<String, Object> configurationAttributes) {
        this.configurationAttributes = configurationAttributes;
    }

    public Map<String, Object> getNotifications() {
        return notifications;
    }

    public void setNotifications(final Map<String, Object> notifications) {
        this.notifications = notifications;
    }

    public ActivityType getActivity() {
        return activity;
    }

    public void setActivity(final ActivityType activity) {
        this.activity = activity;
    }

    public boolean isAutoRestoreOnFail() {
        return autoRestoreOnFail;
    }

    public void setAutoRestoreOnFail(final boolean autoRestoreOnFail) {
        this.autoRestoreOnFail = autoRestoreOnFail;
    }

    public Map<String, Object> getDhcpAttributes() {
        return dhcpAttributes;
    }

    public void setDhcpAttributes(final Map<String, Object> dhcpAttributes) {
        this.dhcpAttributes = dhcpAttributes;
    }

    public Map<String, Object> getControllingNodesAttributes() {
        return controllingNodesAttributes;
    }

    public void setControllingNodesAttributes(final Map<String, Object> controllingNodesAttributes) {
        this.controllingNodesAttributes = controllingNodesAttributes;
    }

    public Map<String, Object> getNodeLocation() {
        return nodeLocation;
    }

    public void setNodeLocation(final Map<String, Object> nodeLocation) {
        this.nodeLocation = nodeLocation;
    }

    public Map<String, String> getRemoteNodeNames() {
        return remoteNodeNames;
    }

    public void setRemoteNodeNames(final Map<String, String> remoteNodeNames) {
        this.remoteNodeNames = remoteNodeNames;
    }

    public Map<String, String> getIgnoreErrors() {
        return ignoreErrors;
    }

    public void setIgnoreErrors(final Map<String, String> ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }
    public Map<String, Object> getHealthCheckAttributes() {
        return healthCheckAttributes;
    }

    public void setHealthCheckAttributes(final Map<String, Object> healthCheckAttributes) {
        this.healthCheckAttributes = healthCheckAttributes;
    }

    public String getBackupName() {
        return backupName;
    }

    public void setBackupName(final String backupName) {
        this.backupName = backupName;
    }

    public String getDefaultRouterAddress() {
        return defaultRouterAddress;
    }

    public void setDefaultRouterAddress(final String defaultRouterAddress) {
        this.defaultRouterAddress = defaultRouterAddress;
    }

    public List<ArtifactDetails> getArtifactDetailsInStrictSequence() {
        return Collections.unmodifiableList(artifactDetailsInStrictSequence);
    }

    public void setArtifactDetailsInStrictSequence(final List<ArtifactDetails> artifactDetailsInStrictSequence) {
        this.artifactDetailsInStrictSequence = Collections.unmodifiableList(artifactDetailsInStrictSequence);
    }
}
