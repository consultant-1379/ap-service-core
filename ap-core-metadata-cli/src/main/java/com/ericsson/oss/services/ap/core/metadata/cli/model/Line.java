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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ericsson.oss.services.ap.core.metadata.cli.api.LineMetadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "attribute" })
@XmlRootElement(name = "line")
public class Line implements LineMetadata {

    @XmlElement(required = true)
    protected Attribute attribute;

    @XmlAttribute(name = "style")
    protected String style;

    /**
     * Gets the value of the attribute property.
     *
     * @return possible object is {@link Attribute }
     */
    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the value of the attribute property.
     *
     * @param value
     *            allowed object is {@link Attribute }
     */
    public void setAttribute(final Attribute value) {
        attribute = value;
    }

    /**
     * Gets the value of the style property.
     *
     * @return possible object is {@link String }
     */
    @Override
    public String getStyle() {
        return style;
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