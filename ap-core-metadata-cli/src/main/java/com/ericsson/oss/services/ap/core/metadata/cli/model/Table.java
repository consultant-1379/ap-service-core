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

package com.ericsson.oss.services.ap.core.metadata.cli.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.TableMetadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "attributes" })
@XmlRootElement(name = "table")
public class Table implements TableMetadata {

    @XmlElement(name = "attribute", type = Attribute.class)
    protected List<AttributeMetadata> attributes;

    @XmlAttribute(name = "style")
    protected String style;

    @XmlAttribute(name = "filter")
    protected String filter;

    @XmlAttribute(name = "heading")
    protected String heading;

    @Override
    public List<AttributeMetadata> getAttributes() {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        return attributes;
    }

    @Override
    public String getStyle() {
        return style;
    }

    @Override
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     *
     * @param filter
     *            the filter property
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    @Override
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the value of the style property.
     *
     * @param value
     *            allowed object is {@link String }
     */
    public void setStyle(final String value) {
        style = value;
    }
}
