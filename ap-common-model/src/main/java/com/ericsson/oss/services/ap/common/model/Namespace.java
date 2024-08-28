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
 * Common namespaces in the ENM model.
 */
public enum Namespace {

    AP("ap"),
    OSS_NE_DEF("OSS_NE_DEF"),
    RCS_HW_IM("RcsHwIM"),
    IPR_HW_IM("IPR_HwIM"),
    ERBS_NODE_MODEL("ERBS_NODE_MODEL"),
    RBS_NODE_MODEL("RBS_NODE_MODEL"),
    OPTO_HW_IM("OPTOFH_HwIM");
    

    private String formattedNamespace;

    private Namespace(final String formattedNamespace) {
        this.formattedNamespace = formattedNamespace;
    }

    @Override
    public String toString() {
        return formattedNamespace;
    }
}
