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

import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEvent;
import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEventType;
import com.ericsson.oss.itpf.sdk.resources.file.listener.FileResourceListener;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Implements Service Framework's <code>FileResourceListener</code> interface in order to listen to changes on the file system i.e. new metadata file
 * added or existing file modified.
 */
public class ResourceChangeListener implements FileResourceListener {

    private ResourceChangeCallback resourceChangeCallback;

    /**
     * Sets the callback listner.
     *
     * @param resourceChangeCallback
     *            object that implements the <code>ResourceChangeCallback</code> interface. Will be used to call back on directory change event
     */
    public void setCallbackListner(final ResourceChangeCallback resourceChangeCallback) {
        this.resourceChangeCallback = resourceChangeCallback;
    }

    /**
     * Called to register an instance of this class with Service Framework
     *
     * @return true if registration was successful
     */
    public boolean registerListner() {
        return Resources.registerListener(this);
    }

    /**
     * The directory on the file system to listen to changes on.
     *
     * @return the URI
     */
    @Override
    public String getURI() {
        return DirectoryConfiguration.getCliMetaDataDirectory();
    }

    /**
     * The event types that will trigger a callback.
     *
     * @return event types that will trigger a callback
     */
    @Override
    public FileResourceEventType[] getEventTypes() {
        return new FileResourceEventType[] { FileResourceEventType.FILE_MODIFIED, FileResourceEventType.FILE_CREATED };
    }

    /**
     * Called by Service Framework for resource change events. Will trigger a callback to interested object implementing the
     * <code>ResourceChangeCallback</code> interface.
     *
     * @param fileResourceEvent
     *            the file event
     */
    @Override
    public void onEvent(final FileResourceEvent fileResourceEvent) {
        resourceChangeCallback.resourceChanged();
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null || object.getClass() != this.getClass()) {
            return false;
        }

        return ((ResourceChangeListener) object).getURI().equals(getURI());
    }

    @Override
    public int hashCode() {
        final int result = 17;
        return 37 * result + DirectoryConfiguration.getCliMetaDataDirectory().hashCode();
    }
}
