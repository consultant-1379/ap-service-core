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
package com.ericsson.oss.services.ap.common.model

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Test class for {@link NodeAttribute}
 */
class NodeAttributeSpec extends CdiSpecification {

    def NodeAttribute nodeAttribute

    def "Verify toString method retrieves node attribute value"() {
        when:
            def attribute = nodeAttribute.toString()

        then:
            expectedValue.equals(attribute)

        where:
                  nodeAttribute                       |       expectedValue
            NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID | "activeWorkflowInstanceId"
            NodeAttribute.BACKUP                      | "backup"
            NodeAttribute.BACKUP_NAME                 | "backupName"
            NodeAttribute.DEPLOYMENT                  | "deployment"
            NodeAttribute.HARDWARE_SERIAL_NUMBER      | "hardwareSerialNumber"
            NodeAttribute.IPADDRESS                   | "ipAddress"
            NodeAttribute.IS_HARDWARE_REPLACE_NODE    | "isHardwareReplaceNode"
            NodeAttribute.IS_NODE_MIGRATION           | "isNodeMigration"
            NodeAttribute.IS_ROLLBACK                 | "isRollback"
            NodeAttribute.NAME                        | "name"
            NodeAttribute.NODE_IDENTIFIER             | "nodeIdentifier"
            NodeAttribute.NODE_TYPE                   | "nodeType"
            NodeAttribute.NODE_LOCATION               | "nodeLocation"
            NodeAttribute.OSS_PREFIX                  | "ossPrefix"
            NodeAttribute.SNMP_USER                   | "snmpUser"
            NodeAttribute.TIMEZONE                    | "timeZone"
            NodeAttribute.USER_LABEL                  | "userLabel"
            NodeAttribute.WAITING_FOR_MESSAGE         | "waitingForMessage"
            NodeAttribute.WORKFLOW_INSTANCE_ID_LIST   | "workflowInstanceIdList"
            NodeAttribute.WORK_ORDER_ID               | "workOrderId"
            NodeAttribute.SERIAL_NUMBER               | "serialNumber"
            NodeAttribute.PRODUCT_DATA_SERIAL_NUMBER  | "productData.serialNumber"
            NodeAttribute.PLAT_FORM                   | "platform"
    }
}
