/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.usecase;

/**
 * ENUM specifying the various phases of AutoProvisioning that are to be logged for DDP.
 */
public enum CommandLogName {

    ACTIVATE_OPTIONAL_FEATURES(DdpPhase.INTEGRATE_NODE),
    ADD_NODE(DdpPhase.ORDER_NODE),
    AIWS_DOWNLOAD_CONFIGURATION_FILE(DdpPhase.INTEGRATE_NODE),
    ASSIGN_TARGET_GROUPS,
    BIND,
    BIND_DURING_ORDER(DdpPhase.ORDER_NODE),
    CANCEL,
    CANCEL_SECURITY(DdpPhase.DELETE_NODE),
    CREATE_AND_WRITE_PROJECT_ARTIFACTS(DdpPhase.ORDER_PROJECT),
    CREATE_BACKUP(DdpPhase.INTEGRATE_NODE),
    CREATE_CV(DdpPhase.INTEGRATE_NODE),
    CREATE_FILE_ARTIFACT(DdpPhase.ORDER_NODE),
    CREATE_NODE_CHILDREN_MOS(DdpPhase.ORDER_NODE),
    CREATE_NODE_MO(DdpPhase.ORDER_NODE),
    CREATE_NODE_USER_CREDENTIALS(DdpPhase.ORDER_NODE),
    CREATE_PROFILE,
    CREATE_PROJECT,
    CREATE_PROJECT_MO(DdpPhase.ORDER_PROJECT),
    DELETE_LICENSE_KEY_FILE(DdpPhase.ORDER_NODE),
    DELETE_NODE,
    DELETE_NODE_MO(DdpPhase.DELETE_NODE),
    DELETE_PROFILE,
    DELETE_PROJECT,
    DELETE_PROJECT_MO(DdpPhase.DELETE_PROJECT),
    DELETE_RAW_AND_GENERATED_NODE_ARTIFACTS(DdpPhase.DELETE_NODE),
    DELETE_RAW_AND_GENERATED_PROJECT_ARTIFACTS(DdpPhase.DELETE_PROJECT),
    DHCP_DELETE_CLIENT(DdpPhase.INTEGRATE_NODE),
    DHCP_CONFIGURATION(DdpPhase.ORDER_NODE),
    DISABLE,
    DISABLE_SUPERVISION(DdpPhase.PRE_MIGRATION_NODE),
    DOWNLOAD_ARTIFACT,
    DOWNLOAD_SCHEMA,
    DOWNLOAD_CIQ,
    DUMP_SNAPSHOT,
    ENABLE,
    ENABLE_SUPERVISION(DdpPhase.INTEGRATE_NODE),
    EXPANSION,
    EXPORT_CIQ,
    GENERATE_HARDWARE_REPLACE_NODE_DATA(DdpPhase.HARDWARE_REPLACE),
    GENERATE_SECURITY(DdpPhase.ORDER_NODE),
    GET_SNAPSHOT,
    GPS_POSITION_CHECK(DdpPhase.INTEGRATE_NODE),
    HARDWARE_REPLACE_BIND(DdpPhase.HARDWARE_REPLACE),
    IMPORT_CONFIGURATIONS(DdpPhase.INTEGRATE_NODE),
    IMPORT_LICENSEKEYFILE(DdpPhase.ORDER_NODE),
    INITIATE_SYNC_NODE(DdpPhase.INTEGRATE_NODE),
    INSTALL_LICENSE_KEY_FILE(DdpPhase.INTEGRATE_NODE),
    INTEGRATE,
    MIGRATION,
    MODIFY_PROFILE,
    NODE_PROVISIONING_TOOL,
    ORDER_NODE,
    ORDER_PROJECT,
    EOI_ORDER_PROJECT,
    PRE_MIGRATION_NODE,
    RECONFIGURATION,
    REMOVE_BACKUP(DdpPhase.DELETE_NODE),
    REMOVE_NODE(DdpPhase.DELETE_NODE),
    RESUME,
    RESTORE,
    SETUP_CONFIGURATION(DdpPhase.ORDER_NODE),
    SKIP,
    SNMP_CONFIGURATION(DdpPhase.INTEGRATE_NODE),
    STATUS,
    STATUS_DEPLOYMENT,
    STATUS_NODE,
    STATUS_PROJECT,
    UNLOCK_CELLS(DdpPhase.INTEGRATE_NODE),
    UNSUPPORTED,
    UPDATE_SWM(DdpPhase.INTEGRATE_NODE),
    UPLOAD_ARTIFACT,
    UPLOAD_BACKUP(DdpPhase.INTEGRATE_NODE),
    UPLOAD_CV(DdpPhase.INTEGRATE_NODE),
    VALIDATE_ARTIFACTS(DdpPhase.ORDER_NODE),
    VALIDATE_PROJECT(DdpPhase.ORDER_PROJECT),
    VALIDATE_CONFIGURATIONS(DdpPhase.ORDER_NODE),
    VIEW,
    VIEW_ALL_PROFILES,
    VIEW_NODE,
    VIEW_NODE_TYPES,
    VIEW_PROFILES,
    VIEW_PROJECT,
    VIEW_TEMPLATES;

    public static final String AP_PREFIX = "AUTO_PROVISIONING";

    private String ddpPhase = "";
    private boolean hasDdpPhase = false;

    private CommandLogName() {

    }

    private CommandLogName(final DdpPhase ddpPhase) {
        this.ddpPhase = ddpPhase.toString();
        hasDdpPhase = true;
    }

    /**
     * The command's key to be used for logging errors for the command.
     * <p>
     * All entries will start with the prefix {@value #AP_PREFIX}.
     */
    @Override
    public String toString() {
        if (hasDdpPhase) {
            return String.format("%s.%s.%s", AP_PREFIX, ddpPhase, super.toString());
        }
        return String.format("%s.%s", AP_PREFIX, super.toString());
    }
}
