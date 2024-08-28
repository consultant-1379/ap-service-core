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
package com.ericsson.oss.services.ap.api.status;

/**
 * Status entry names for <code>ap status -n</code> reporting.
 */
public enum StatusEntryNames {

    ACTIVATE_OPTIONAL_FEATURES("Activate Optional Features"),
    ADD_NODE_TASK("Add Node"),
    AIWS_NOTIFICATION("Node Connection to AIWS Notification"),
    ASSIGN_TARGET_GROUP("Assign Target Groups"),
    CANCEL_RECEIVED("Cancel Notification"),
    CANCEL_SECURITY_TASK("Cancel Security"),
    CLEAN_UP_TASK("Clean up"),
    CONFIGURE_SOFTWARE_MANAGEMENT("Configure Software Management"),
    CREATE_CV_TASK("Create CV"),
    CREATE_NODE_USER_CREDENTIALS("Create Security Credentials"),
    DELETE_LICENSE_KEY_FILE_TASK("Delete License Key File"),
    DHCP_CONFIGURATION("Configure DHCP"),
    DHCP_REMOVE_CLIENT("Remove DHCP Client Configuration"),
    ENROLL_IPSEC_CERTIFICATE("Enroll IPSec Certificate"),
    ENROLL_OAM_CERTIFICATE("Enroll OAM Certificate"),
    EXPANSION_NOTIFICATION("Expansion Notification"),
    GENERATE_PROVISIONING_ARTIFACTS("Generate Provisioning Artifacts"),
    GENERATE_SECURITY_TASK("Generate Security"),
    GPS_POSITION_CHECK_TASK("GPS Position Check"),
    HARDWARE_BIND_TASK("Hardware Bind"),
    IMPORT_CONFIGURATIONS_TASK("Initiate Import Configurations"),
    IMPORT_LICENSE_KEY_FILE_TASK("Import License Key File"),
    NODE_APPLYING_CONFIGURATION("Node Applying Configuration"),
    NODE_DOWNLOADING_CONFIGURATIONS("Node Downloading Configurations"),
    NODE_ESTABLISHING_CONTACT("Node Establishing Contact"),
    NODE_INSTALLING_SOFTWARE("Node Installing Software"),
    NODE_SENDING_NODE_UP("Node Sending Node Up"),
    NODE_STARTING_SOFTWARE("Node Starting Software"),
    NODE_UP("Node Up Notification"),
    REMOVE_NODE_TASK("Remove Node"),
    RETRIEVE_LICENSE_TASK("Retrieve License"),
    RUN_HEALTH_CHECK("Run Health Check"),
    SET_MANAGEMENT_STATE("Set Management State"),
    SITE_CONFIG_COMPLETE("Site Config Complete Notification"),
    SNMP_CONFIGURATION("Configure SNMP Security"),
    SYNC_NODE("Initiate Node Synchronization"),
    SYNC_NODE_NOTIFICATION("Node Synchronization Notification"),
    UNLOCK_CELLS("Unlock Cells"),
    UPDATE_NODE_TASK("Update Node"),
    UPLOAD_CONFIGURATION("Upload Configuration"),
    UPLOAD_CV_TASK("Initiate Upload CV"),
    VALIDATE_CONFIGURATIONS_TASK("Validate Configuration"),
    EOI_ADD_NODE_TASK("EOI Add Node"),
    EOI_REMOVE_NODE_TASK("EOI Remove Node"),
    EOI_SECURITY_CREDENTIALS_TASK("EOI Security Credential"),
    EOI_CREDENTIALS_FAILED_TASK("EOI Credential failed"),
    EOI_GENERATE_ENROLLMENT_INFO_TASK("EOI Generate Enrollment Info"),
    EOI_CANCEL_ENROLLMENT_TASK("EOI Cancel Enrollment"),
    EOI_LDAP_CONFIGURATION_TASK("EOI Ldap Configuration"),
    EOI_DELETE_LDAP_TASK("EOI Delete Ldap"),
    EOI_SNMP_CONFIGURATION_TASK("EOI SNMP Configuration"),
    EOI_SNMP_FAILED_TASK("EOI SNMP Failed"),
    EOI_GENERATE_ARTIFACT_TASK("EOI Generate Artifact"),
    EOI_GENERATE_ARTIFACT_FAILED_TASK("EOI Generate Artifact Failed");

    private final String entryName;

    StatusEntryNames(final String entryName) {
        this.entryName = entryName;
    }

    @Override
    public final String toString() {
        return entryName;
    }
}
