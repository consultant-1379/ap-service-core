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

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;

/**
 * This is the root of the JAXB metadata model. Anyone wishing to use the metadata model should start with this type and drill into the model as
 * required.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "views" })
@XmlRootElement(name = "cliViewMetadata")
public class CliViews {

    @XmlElement(name = "view", type = View.class)
    protected List<ViewMetadata> views;

    @XmlAttribute(name = "namespace", required = true)
    protected String namespace;

    /**
     * Gets the value of the view property.
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
     * present inside the JAXB object. This is why there is not a <code>set</code> method for the view property.
     * <p>
     * Objects of the following type(s) are allowed in the list: {@link View}
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getView().add(newItem);
     * </pre>
     * <p>
     *
     * @return a list of {@link ViewMetadata} defined by the view property
     */
    public List<ViewMetadata> getViews() {
        if (views == null) {
            views = new ArrayList<>();
        }
        return views;
    }

    /**
     * Gets the value of the namespace property.
     *
     * @return possible object is {@link String}
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the value of the namespace property.
     *
     * @param value
     *            allowed object is {@link String }
     */
    public void setNamespace(final String value) {
        namespace = value;
    }
}
