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
package com.ericsson.oss.services.ap.common.model;

/**
 * An attribute in the <code>AutoProvisioningAccount</code> model.
 */
public enum ApAccountAttribute {

    ACCOUNT_TYPE("accountType"),
    NODE_TYPE("nodeType"),
    USERNAME("userName"),
    PASSWORD("password"),
    NODENAME("nodeName");

    private String attributeName;

    private ApAccountAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
