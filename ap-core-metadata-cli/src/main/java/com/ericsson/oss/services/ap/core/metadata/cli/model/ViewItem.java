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

import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "viewComponentsMetadata" })
@XmlRootElement(name = "viewitem")
public class ViewItem implements ViewItemMetadata {

    @XmlElements({
            @XmlElement(name = "group", type = Group.class),
            @XmlElement(name = "line", type = Line.class),
            @XmlElement(name = "table", type = Table.class) })
    protected List<Metadata> viewComponentsMetadata;

    @XmlAttribute(name = "type", required = true)
    protected String type;

    @XmlAttribute(name = "moStruct")
    protected String moStruct;

    /**
     * Gets the value of the viewComponentsMetadata property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <code>set</code> method for the viewComponentsMetadata property.
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getGroupOrLineOrTable().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list:
     * <ul>
     * <li>{@link Group}</li>
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
     * Gets the value of the type property.
     *
     * @return possible object is {@link String}
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param type
     *            allowed object is {@link String}
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the value of the moStruct property.
     *
     * @return the value of the moStruct property. Possible object is {@link String}
     */
    @Override
    public String getMoStruct() {
        return moStruct;
    }

    /**
     * Sets the value of the moStruct property.
     *
     * @param moStruct
     *            the MO struct property. Allowed object is {@link String}
     */
    public void setMoStruct(final String moStruct) {
        this.moStruct = moStruct;
    }
}
