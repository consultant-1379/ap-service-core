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
 * An attribute in the AP <code>Node</code> model.
 */
public enum NodeAttribute {

    ACTIVE_WORKFLOW_INSTANCE_ID("activeWorkflowInstanceId"),
    BACKUP("backup"), // BACKUP stands for same thing as BACKUP_NAME, only used for backwards compatible with hardware replace nodeInfo schema
    BACKUP_NAME("backupName"),
    DEPLOYMENT("deployment"),
    HARDWARE_SERIAL_NUMBER("hardwareSerialNumber"),
    IPADDRESS("ipAddress"),
    IS_HARDWARE_REPLACE_NODE("isHardwareReplaceNode"),
    IS_NODE_MIGRATION("isNodeMigration"),
    IS_ROLLBACK("isRollback"),
    NAME("name"),
    NODE_IDENTIFIER("nodeIdentifier"),
    NODE_TYPE("nodeType"),
    NODE_LOCATION("nodeLocation"),
    OSS_PREFIX("ossPrefix"),
    SNMP_USER("snmpUser"),
    TIMEZONE("timeZone"),
    USER_LABEL("userLabel"),
    WAITING_FOR_MESSAGE("waitingForMessage"),
    WORKFLOW_INSTANCE_ID_LIST("workflowInstanceIdList"),
    WORK_ORDER_ID("workOrderId"),
    SERIAL_NUMBER("serialNumber"),
    PRODUCT_DATA_SERIAL_NUMBER("productData.serialNumber"),
    PLAT_FORM("platform"),
    NODE_ID("NodeId");

    private String attributeName;

    private NodeAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
