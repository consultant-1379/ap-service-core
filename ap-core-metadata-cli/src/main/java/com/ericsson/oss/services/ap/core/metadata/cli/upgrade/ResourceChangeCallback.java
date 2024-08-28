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
package com.ericsson.oss.services.ap.core.metadata.cli.upgrade;

/**
 * An object that wants to receive a callback in response to changes on the file system i.e. new metadata file added or existing file modified should
 * implement this interface and pass to {@link ResourceChangeListener}.
 */
public interface ResourceChangeCallback {

    /**
     * Called by <code>ResourceChangeListner#onEvent(FileResourceEvent)</code>.
     */
    void resourceChanged();
}
