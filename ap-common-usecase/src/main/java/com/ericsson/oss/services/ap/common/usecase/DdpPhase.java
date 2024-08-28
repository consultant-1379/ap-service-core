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
package com.ericsson.oss.services.ap.common.usecase;

/**
 * ENUM specifying the various DDP phases for AutoProvisioning logging.
 */
enum DdpPhase {

    DELETE_NODE,
    DELETE_PROJECT,
    HARDWARE_REPLACE,
    INTEGRATE_NODE,
    ORDER_NODE,
    ORDER_PROJECT,
    PRE_MIGRATION_NODE;
}
