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
package com.ericsson.oss.services.ap.common.model;

/**
 * Attribute names in the {@code ControllingNodes} MO model.
 */
public enum ControllingNodesAttribute {

    CONTROLLING_BSC("controllingBsc", "bsc"),
    CONTROLLING_RNC("controllingRnc", "rnc");

    private final String attributeName;
    private final String tagName;

    ControllingNodesAttribute(final String attributeName, final String tagName) {
        this.attributeName = attributeName;
        this.tagName = tagName;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public String getTagName() {
        return this.tagName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
