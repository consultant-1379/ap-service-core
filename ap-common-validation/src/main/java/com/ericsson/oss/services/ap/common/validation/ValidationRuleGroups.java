/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation;

public final class ValidationRuleGroups {

    /**
     * Validation group containing rules to validate order can be executed
     */
    public static final String ORDER = "Order";

    /**
     * Validation group containing rules to validate BulkCM Import Files and License Key Files
     */
    public static final String ORDER_WORKFLOW = "OrderWorkflow";

    /**
     * Validation group containing rules to validate BulkCM Import Files and License Key Files for Migration Node
     */
    public static final String MIGRATION_WORKFLOW = "MigrationWorkflow";

    /**
     * Validation group containing rules to validate order batch can be executed
     */
    public static final String ORDER_BATCH = "OrderBatch";

    /**
     * Validation group containing rules to validate files before creating profile
     */
    public static final String CREATE_PROFILE = "CreateProfile";

    /**
     * Validation group containing rules to validate node expansion can be executed
     */
    public static final String EXPANSION = "Expansion";

    /**
     * Validation group containing rules to validate node replace can be executed
     */
    public static final String HARDWARE_REPLACE = "HardwareReplace";

    /**
     * Validation group containing rules to validate node migration can be executed
     */
    public static final String MIGRATION = "Migration";

    /**
     * Validation group containing rules to validate eoi project can be executed
     */
    public static final String EOI = "Eoi";

    private ValidationRuleGroups() {

    }

}
