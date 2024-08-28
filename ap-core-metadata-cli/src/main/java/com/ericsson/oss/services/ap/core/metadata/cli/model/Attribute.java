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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "label" })
@XmlRootElement(name = "attribute")
public class Attribute implements AttributeMetadata {

    protected String label;

    @XmlAttribute(name = "name", required = true)
    protected String name;

    @XmlAttribute(name = "tabbed", required = false)
    protected Boolean tabbed = Boolean.FALSE;

    @XmlAttribute(name = "renderer", required = false)
    protected String renderer;

    /**
     * Gets the value of the label property.
     *
     * @return possible object is {@link String}
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     *
     * @param label
     *            allowed object is {@link String}
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param name
     *            allowed object is {@link String}
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the value of the renderer property.
     *
     * @return possible object is {@link String}
     */
    @Override
    public String getRenderer() {
        return renderer;
    }

    /**
     * Sets the value of the renderer property.
     *
     * @param renderer
     *            allowed object is {@link String}
     */
    public void setRenderer(final String renderer) {
        this.renderer = renderer;
    }

    /**
     * Gets the value of the tabbed property.
     *
     * @return possible object is {@link Boolean}
     */
    @Override
    public Boolean getTabbed() {
        return tabbed;
    }

    /**
     * Sets the value of the tabbed property
     *
     * @param tabbed
     *            allowed object is {@link Boolean}
     */
    public void setTabbed(final Boolean tabbed) {
        this.tabbed = tabbed;
    }

}