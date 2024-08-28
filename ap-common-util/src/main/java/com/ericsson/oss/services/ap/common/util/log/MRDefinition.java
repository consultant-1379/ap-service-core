/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.log;

/**
 * This ENUM class is for MR ids with the name matching what the MR title is defined as.
 */
public enum MRDefinition {

    AP_AI_STATUS_IMPROVEMENT("105 65-0334/46967"),
    AP_ALTERNATIVE_FINGERPRINT("105 65-0334/66305"),
    AP_APPLY_AMOS_SCRIPT("105 65-0334/57351"),
    AP_APPLY_NETCONF_POST_SYNC("105 65-0334/63523"),
    AP_AUTOMATIC_HOUSEKEEPING("105 65-0334/63984"),
    AP_DHCP("105 65-0334/61563"),
    AP_DYNAMIC_MOM_LOADING("105 65-0334/64516"),
    AP_ENHANCED_EXPANSION("105 65-0334/76205"),
    AP_EXPANSION_APPLY_AMOS_SCRIPT("105 65-0334/67482"),
    AP_EXPANSION_HEALTHCHECK("105 65-0334/58635"),
    AP_EXPANSION_NETCONF_VALIDATION("105 65-0334/58636"),
    AP_HARDWAREREPLACE_BACKUP("105 65-0334/62165"),
    AP_HARDWAREREPLACE_CHECK_BACKUP_AND_SW_COMPATIBILITY("105 65-0334/78134"),
    AP_HARDWAREREPLACE_CONTROLLER6610("105 65-0334/73251"),
    AP_HARDWAREREPLACE_DHCP("105 65-0334/61564"),
    AP_HARDWAREREPLACE_NO_SERIALNUMBER("105 65-0334/62164"),
    AP_HARDWAREREPLACE_OPERATOR_DEFINED_SECURITY("105 65-0334/67461"),
    AP_HARDWAREREPLACE_STATUS_IMPROVEMENT("105 65-0334/65008"),
    AP_INSTANTANEOUS_LICENSE("105 65-0334/68080"),
    AP_INTEGRATE_CONTROLLER_6610("105 65-0334/64580"),
    AP_INTEGRATE_FRONTHAUL_6000("105 65-0334/78335"),
    AP_INTEGRATE_ROUTER_60002("105 65-0334/79091"),
    AP_INTEGRATE_ROUTER_6673("105 65-0334/69133"),
    AP_INTEGRATED_PROVISIONING("105 65-0334/59211"),
    AP_INTEGRATION_AUTOMATE_NHC("105 65-0334/70133"),
    AP_NETCONF_EXPANSION("105 65-0334/67543"),
    AP_INTEGRATION_MIGRATION("105 65-0334/70692"),
    AP_POST_INTEGRATION_SCRIPT_SEQUENCE("105 65-0334/74723"),
    AP_SKIP_IMPORT_FAILED_CONFIGURATION("105 65-0334/78832");

    private final String mrId;

    MRDefinition(final String mrId) {
        this.mrId = mrId;
    }

    @Override
    public String toString() {
        return mrId;
    }
}
