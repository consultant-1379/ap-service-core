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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ericsson.oss.services.ap.core.metadata.cli.api.GroupMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "viewComponentsMetadata" })
@XmlRootElement(name = "group")
public class Group implements GroupMetadata {

    @XmlElements({ @XmlElement(name = "line", type = Line.class), @XmlElement(name = "table", type = Table.class) })
    protected List<Metadata> viewComponentsMetadata;

    @XmlAttribute(name = "heading")
    protected String heading;

    /**
     * Gets the value of the viewComponentsMetadata property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <code>set</code> method for the lineOrTable property.
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getLineOrTable().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list:
     * <ul>
     * <li>{@link Line}</li>
     * <li>{@link Table}</li>
     * </ul>
     */
    @Override
    public List<Metadata> getViewComponentsMetadata() {
        if (viewComponentsMetadata == null) {
            viewComponentsMetadata = new ArrayList<>();
        }
        return viewComponentsMetadata;
    }

    /**
     * Gets the value of the heading property.
     *
     * @return possible object is {@link String }
     */
    @Override
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the value of the heading property.
     *
     * @param value
     *            allowed object is {@link String }
     */
    public void setHeading(final String value) {
        heading = value;
    }
}