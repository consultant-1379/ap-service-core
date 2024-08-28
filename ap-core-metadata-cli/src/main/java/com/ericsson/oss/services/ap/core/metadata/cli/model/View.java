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

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;

/**
 * This class encapsulates a JAXB model of an XML metadata element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "viewitems" })
@XmlRootElement(name = "view")
public class View implements ViewMetadata {

    @XmlElement(name = "viewitem", type = ViewItem.class)
    protected List<ViewItemMetadata> viewitems;

    @XmlAttribute(name = "id", required = true)
    protected String id;

    /**
     * Gets the value of the viewitem property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <code>set</code> method for the viewitems property.
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getViewitem().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list:
     * <ul>
     * <li>{@link ViewItem}</li>
     * </ul>
     */
    @Override
    public List<ViewItemMetadata> getViewItems() {
        if (viewitems == null) {
            viewitems = new ArrayList<>();
        }
        return viewitems;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *            allowed object is {@link String }
     */
    public void setId(final String value) {
        id = value;
    }
}