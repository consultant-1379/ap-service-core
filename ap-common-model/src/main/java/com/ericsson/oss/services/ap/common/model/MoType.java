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
package com.ericsson.oss.services.ap.common.model;

/**
 * Common MO types in the ENM model.
 */
public enum MoType {

    ABSTRACT_AI_OPTIONS("AbstractAutoIntegrationOptions"),
    ABSTRACT_SECURITY("AbstractSecurity"),
    AI_OPTIONS("AutoIntegrationOptions"),
    AP_ACCOUNT("AutoProvisioningAccount"),
    AP_ACCOUNTS("AutoProvisioningAccounts"),
    AP_VNF_NODE("AutoProvisioningVnfNode"),
    AP_VNF_NODES("AutoProvisioningVnfNodes"),
    CM_FUNCTION("CmFunction"),
    CM_NODE_HEARTBEAT_SUPERVISION("CmNodeHeartbeatSupervision"),
    COM_CONNECTIVITY_INFORMATION("ComConnectivityInformation"),
    CONTROLLING_NODES("ControllingNodes"),
    FM_ALARM_SUPERVISION("FmAlarmSupervision"),
    GEOGRAPHIC_LOCATION("GeographicLocation"),
    GEOMETRIC_POINT("GeometricPoint"),
    HEALTH_CHECK("HealthCheck"),
    INV_SUPERVISION("InventorySupervision"),
    LICENSE_OPTIONS("LicenseOptions"),
    MANAGEDELEMENT("ManagedElement"),
    MECONTEXT("MeContext"),
    NETWORK_ELEMENT("NetworkElement"),
    NETWORK_ELEMENT_SECURITY("NetworkElementSecurity"),
    NODE("Node"),
    NODE_ARTIFACT("NodeArtifact"),
    NODE_ARTIFACT_CONTAINER("NodeArtifactContainer"),
    NODE_DHCP("NodeDhcp"),
    NODE_USER_CREDENTIALS("NodeUserCredentials"),
    NODE_STATUS("NodeStatus"),
    NOTIFICATION("Notification"),
    PM_FUNCTION("PmFunction"),
    PROJECT("Project"),
    CONFIGURATION_PROFILE("ConfigurationProfile"),
    SECURITY("Security"),
    SECURITY_FUNCTION("SecurityFunction"),
    SUPERVISION_OPTIONS("SupervisionOptions"),
    HW_ITEM("HwItem"),
    HW_UNIT("HwUnit");

    private final String moName;

    private MoType(final String moName) {
        this.moName = moName;
    }

    @Override
    public String toString() {
        return moName;
    }
}
