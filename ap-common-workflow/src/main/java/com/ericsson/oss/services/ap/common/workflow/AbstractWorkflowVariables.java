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
package com.ericsson.oss.services.ap.common.workflow; // NOPMD - Too many public methods

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * This abstract class defines the non-node specific workflow variables used by the service tasks.
 */
public abstract class AbstractWorkflowVariables implements Serializable { // NOPMD - Too many fields

    private static final long serialVersionUID = -3010218168113767166L;

    public static final String WORKFLOW_VARIABLES_KEY = "WorkflowVariables";
    public static final String DELETE_IGNORE_NETWORK_ELEMENT_KEY = "deleteIgnoresNetworkElement";
    public static final String DHCP_CLIENT_ID_TO_REMOVE_KEY = "dhcpClientIdToRemove";

    private final Map<SupervisionMoType, Boolean> enableSupervision = new EnumMap<>(SupervisionMoType.class);
    private final Map<SupervisionMoType, Boolean> disableSupervision = new EnumMap<>(SupervisionMoType.class);
    private final Map<SupervisionMoType, Boolean> originalSupervision = new EnumMap<>(SupervisionMoType.class);

    private String userId;
    private String apNodeFdn;

    private String lastBackupName;
    private String originalBackupName;

    private String fingerPrint;
    private String sequenceNumber;

    private boolean validationRequired = true;
    private boolean securityEnabled;
    private boolean importPreMigrationConfigurations;
    private boolean importNodeConfigurations;
    private boolean installLicense;
    private boolean importLicenseKeyFile;
    private boolean activateOptionalFeatures;
    private boolean applyBaselines;
    private boolean remoteNodeConfiguration;
    private boolean unlockCells;
    private boolean importConfigurationInStrictSequence;

    private String hardwareSerialNumber;
    private String ossPrefix;

    private boolean orderSuccessful = true;
    private boolean preMigrationSuccessful = true;
    private boolean bindSuccessful = true;

    private boolean unorderOrRollbackError;
    private boolean eoiRollbackError;
    private boolean integrationTaskWarning;
    private boolean migrationTaskWarning;
    private boolean preMigrationTaskWarning;
    private boolean rbsIntegrationCompleted;
    private boolean createUserCredentials;
    private boolean hardwareReplaceRollbackError;

    private long orderStartTime;
    private long integrationStartTime;
    private long migrationStartTime;
    private long hardwareReplaceStartTime;
    private long suspendStartTime;
    private long totalSuspendTime;

    private String nodeType;
    private String activity;

    private boolean isDhcpConfiguration;
    private boolean isDhcpSuccess;
    private String initialIpAddress;
    private String defaultRouter;
    private List<String> ntpServers;
    private List<String> dnsServers;
    private String oldHardwareSerialNumber;
    private boolean isMigrationNode;

    private String baseUrl = "";
    private String sessionId = "";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public AbstractWorkflowVariables() {
        for (final SupervisionMoType supervisionType : SupervisionMoType.values()) {
            enableSupervision.put(supervisionType, Boolean.FALSE);
            disableSupervision.put(supervisionType, Boolean.FALSE);
            originalSupervision.put(supervisionType, Boolean.FALSE);
        }
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getNodeName() {
        return FDN.get(apNodeFdn).getRdnValue();
    }

    public void setApNodeFdn(final String apNodeFdn) {
        this.apNodeFdn = apNodeFdn;
    }

    public String getApNodeFdn() {
        return apNodeFdn;
    }

    public void setSecurityEnabled(final boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public boolean isImportNodeConfigurations() {
        return importNodeConfigurations;
    }

    public void setImportNodeConfigurations(final boolean importNodeConfigurations) {
        this.importNodeConfigurations = importNodeConfigurations;
    }

    public boolean isImportPreMigrationConfigurations() {
        return importPreMigrationConfigurations;
    }

    public void setImportPreMigrationConfigurations(final boolean importPreMigrationConfigurations) {
        this.importPreMigrationConfigurations = importPreMigrationConfigurations;
    }

    public void setLastBackupName(final String lastBackupName) {
        this.lastBackupName = lastBackupName;
    }

    public String getLastBackupName() {
        return lastBackupName;
    }

    public void setOriginalBackupName(final String originalBackupName) {
        this.originalBackupName = originalBackupName;
    }

    public String getOriginalBackupName() {
        return originalBackupName;
    }

    public boolean isInstallLicense() {
        return installLicense;
    }

    public void setInstallLicense(final boolean installLicense) {
        this.installLicense = installLicense;
    }

    public boolean isActivateOptionalFeatures() {
        return activateOptionalFeatures;
    }

    public void setActivateOptionalFeatures(final boolean activateOptionalFeatures) {
        this.activateOptionalFeatures = activateOptionalFeatures;
    }

    public boolean isApplyBaselines() {
        return applyBaselines;
    }

    public void setApplyBaselines(final boolean applyBaselines) {
        this.applyBaselines = applyBaselines;
    }

    public boolean isRemoteNodeConfiguration() {
        return remoteNodeConfiguration;
    }

    public void setRemoteNodeConfiguration(final boolean remoteNodeConfiguration) {
        this.remoteNodeConfiguration = remoteNodeConfiguration;
    }

    public boolean isUnlockCells() {
        return unlockCells;
    }

    public void setUnlockCells(final boolean unlockCells) {
        this.unlockCells = unlockCells;
    }

    public void setHardwareSerialNumber(final String hardwareSerialNumber) {
        this.hardwareSerialNumber = hardwareSerialNumber;
    }

    public String getHardwareSerialNumber() {
        return hardwareSerialNumber;
    }

    public void setOssPrefix(final String ossPrefix) {
        this.ossPrefix = ossPrefix;
    }

    public String getOssPrefix() {
        return ossPrefix;
    }

    public boolean isOrderSuccessful() {
        return orderSuccessful;
    }

    public void setOrderSuccessful(final boolean orderSuccessful) {
        this.orderSuccessful = orderSuccessful;
    }

    public boolean isBindSuccessful() {
        return bindSuccessful;
    }

    public void setBindSuccessful(final boolean bindSuccessful) {
        this.bindSuccessful = bindSuccessful;
    }

    public boolean isUnorderOrRollbackError() {
        return unorderOrRollbackError;
    }

    public void setUnorderOrRollbackError(final boolean unorderOrRollbackError) {
        this.unorderOrRollbackError = unorderOrRollbackError;
    }

    public boolean isEoiRollbackError() {
        return eoiRollbackError;
    }

    public void setEoiRollbackError(final boolean eoiRollbackError) {
        this.eoiRollbackError = eoiRollbackError;
    }

    public boolean isIntegrationTaskWarning() {
        return integrationTaskWarning;
    }

    public void setIntegrationTaskWarning(final boolean integrationTaskWarning) {
        this.integrationTaskWarning = integrationTaskWarning;
    }

    public boolean isPreMigrationTaskWarning() {
        return preMigrationTaskWarning;
    }

    public void setPreMigrationTaskWarning(final boolean preMigrationTaskWarning) {
        this.preMigrationTaskWarning = preMigrationTaskWarning;
    }

    public long getOrderStartTime() {
        return orderStartTime;
    }

    public void setOrderStartTime(final long startTime) {
        orderStartTime = startTime;
    }

    public long getIntegrationStartTime() {
        return integrationStartTime;
    }

    public void setIntegrationStartTime(final long integrationStartTime) {
        this.integrationStartTime = integrationStartTime;
    }

    public long getHardwareReplaceStartTime() {
        return hardwareReplaceStartTime;
    }

    public void setHardwareReplaceStartTime(final long hardwareReplaceStartTime) {
        this.hardwareReplaceStartTime = hardwareReplaceStartTime;
    }

    public void startSuspendTimer() {
        this.suspendStartTime = System.currentTimeMillis();
    }

    public void endSuspendTimer() {
        this.totalSuspendTime = System.currentTimeMillis() - this.suspendStartTime + this.totalSuspendTime;
    }

    public long getTotalSuspendTime() {
        return totalSuspendTime;
    }

    public boolean isRbsIntegrationCompleted() {
        return rbsIntegrationCompleted;
    }

    public void setRbsIntegrationCompleted(final boolean rbsIntegrationCompleted) {
        this.rbsIntegrationCompleted = rbsIntegrationCompleted;
    }

    public boolean createUserCredentials() {
        return createUserCredentials;
    }

    public void setCreateUserCredentials(final boolean createUserCredentials) {
        this.createUserCredentials = createUserCredentials;
    }

    public boolean isHardwareReplaceRollbackError() {
        return hardwareReplaceRollbackError;
    }

    public void setHardwareReplaceRollbackError(final boolean hardwareReplaceRollbackError) {
        this.hardwareReplaceRollbackError = hardwareReplaceRollbackError;
    }

    public boolean isEnableSupervision(final SupervisionMoType supervisionType) {
        return enableSupervision.get(supervisionType);
    }

    public void setEnableSupervision(final SupervisionMoType supervisionType, final boolean enableSupervision) {
        this.enableSupervision.put(supervisionType, enableSupervision);
    }

    public void setDisableSupervision(final SupervisionMoType supervisionType, final boolean disableSupervision) {
        this.disableSupervision.put(supervisionType, disableSupervision);
    }

    public void setOriginalSupervision(final SupervisionMoType supervisionType, final boolean enableSupervision) {
        this.originalSupervision.put(supervisionType, enableSupervision);
    }

    public boolean isOriginalSupervision(final SupervisionMoType supervisionType) {
        return originalSupervision.get(supervisionType);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isImportLicenseKeyFile() {
        return importLicenseKeyFile;
    }

    public void setImportLicenseKeyFile(final boolean importLicenseKeyFile) {
        this.importLicenseKeyFile = importLicenseKeyFile;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(final String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(final String activity) {
        this.activity = activity;
    }

    public boolean isValidationRequired() {
        return validationRequired;
    }

    public void setValidationRequired(final boolean validationRequired) {
        this.validationRequired = validationRequired;
    }

    public boolean isDhcpConfiguration() {
        return isDhcpConfiguration;
    }

    public void setDhcpConfiguration(final boolean dhcpConfiguration) {
        this.isDhcpConfiguration = dhcpConfiguration;
    }

    public boolean isDhcpSuccess() {
        return isDhcpSuccess;
    }

    public void setDhcpSuccess(final boolean dhcpSuccess) {
        this.isDhcpSuccess = dhcpSuccess;
    }

    public String getInitialIpAddress() {
        return initialIpAddress;
    }

    public void setInitialIpAddress(final String initialIpAddress) {
        this.initialIpAddress = initialIpAddress;
    }

    public String getDefaultRouter() {
        return defaultRouter;
    }

    public void setDefaultRouter(final String defaultRouter) {
        this.defaultRouter = defaultRouter;
    }

    public List<String> getNtpServers() {
        return ntpServers;
    }

    public void setNtpServers(final List<String> ntpServers) {
        this.ntpServers = ntpServers;
    }

    public List<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(final List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }

    public String getOldHardwareSerialNumber() {
        return oldHardwareSerialNumber;
    }

    public void setOldHardwareSerialNumber(final String oldHardwareSerialNumber) {
        this.oldHardwareSerialNumber = oldHardwareSerialNumber;
    }

    public long getMigrationStartTime() {
        return migrationStartTime;
    }

    public void setMigrationStartTime(long migrationStartTime) {
        this.migrationStartTime = migrationStartTime;
    }

    public boolean isPreMigrationSuccessful() {
        return preMigrationSuccessful;
    }

    public void setPreMigrationSuccessful(boolean preMigrationSuccessful) {
        this.preMigrationSuccessful = preMigrationSuccessful;
    }

    public boolean isMigrationNodeUsecase() {
        return isMigrationNode;
    }

    public void setMigrationNode(boolean migrationNode) {
        isMigrationNode = migrationNode;
    }

    public boolean isMigrationTaskWarning() {
        return migrationTaskWarning;
    }

    public void setMigrationTaskWarning(final boolean migrationTaskWarning) {
        this.migrationTaskWarning = migrationTaskWarning;
    }

    public boolean isImportConfigurationInStrictSequence() {
        return importConfigurationInStrictSequence;
    }

    public void setImportConfigurationInStrictSequence(final boolean importConfigurationInStrictSequence) {
        this.importConfigurationInStrictSequence = importConfigurationInStrictSequence;
    }


}
