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
package com.ericsson.oss.services.ap.core.rest.model.nodeproperty;

import java.util.List;
/**
 * POJO model for representing a Node properties.
 */
public class NodeProperties  {

    private List<Object> attributes;
    private List<AttributeGroup> attributeGroups;

    public List<Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(final List<Object> attributes) {
        this.attributes = attributes;
    }

    public List<AttributeGroup> getAttributeGroups() {
        return attributeGroups;
    }

    public void setAttributeGroups(final List<AttributeGroup> attributeGroups) {
        this.attributeGroups = attributeGroups;
    }
}
