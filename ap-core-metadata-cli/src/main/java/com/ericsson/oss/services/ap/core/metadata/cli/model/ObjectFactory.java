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

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * <code>com.ericsson.oss.services.ap.core.ui.metadata.model.cli</code> package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. The Java representation of XML
 * content can consist of schema derived interfaces and classes representing the binding of schema type definitions, element declarations and model
 * groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create an instance of {@link ViewItem}.
     *
     * @return the {@link ViewItem}
     */
    public ViewItem createViewitem() {
        return new ViewItem();
    }

    /**
     * Create an instance of {@link Group}.
     *
     * @return the {@link Group}
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link Line}.
     *
     * @return the {@link Line}
     */
    public Line createLine() {
        return new Line();
    }

    /**
     * Create an instance of {@link Attribute}.
     *
     * @return the {@link Attribute}
     */
    public Attribute createAttribute() {
        return new Attribute();
    }

    /**
     * Create an instance of {@link Table}.
     *
     * @return the {@link Table}
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link View}.
     *
     * @return the {@link View}
     */
    public View createView() {
        return new View();
    }

    /**
     * Create an instance of {@link CliViews}.
     *
     * @return the {@link CliViews}
     */
    public CliViews createCliViewMetadata() {
        return new CliViews();
    }
}
