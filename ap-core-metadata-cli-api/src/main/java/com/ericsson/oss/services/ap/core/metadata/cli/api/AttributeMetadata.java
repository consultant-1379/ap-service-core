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
package com.ericsson.oss.services.ap.core.metadata.cli.api;

/**
 * Defines the metadata that is provided for an attribute element. This metadata is accessed from {@link TableMetadata#getAttributes()} and
 * {@link LineMetadata#getAttribute()}
 */
public interface AttributeMetadata extends Metadata {

    /**
     * Returns the attribute label. The label is more descriptive and human readable than the raw attribute name.
     *
     * @return the attribute label or null if no label applies
     */
    String getLabel();

    /**
     * Returns the attribute name. This is will be raw name in the data source.
     *
     * @return the attribute name
     */
    String getName();

    /**
     * Returns the renderer. This is will be used by the <code>TextRendererFactory</code> to lookup a <code>TextRenderer</code>.
     *
     * @return the renderer
     */
    String getRenderer();

    /**
     * Specifies if the value should be tabbed. False by default.
     *
     * @return the renderer
     */
    Boolean getTabbed();
}
