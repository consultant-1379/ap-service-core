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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEvent;
import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEventType;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Unit tests {@link ResourceChangeListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceChangeListenerTest {

    private final ResourceChangeListener resourceChangeListener = new ResourceChangeListener();

    @Mock
    private ResourceChangeCallback resourceChangeCallback;

    @Mock
    private FileResourceEvent fileResourceEvent;

    @Test
    public void whenResourceChangeListenerIsCreated_thenTheUriHasBeenInitializedCorrectly() {
        assertThat("URI not initalized", resourceChangeListener.getURI(), equalTo(DirectoryConfiguration.getCliMetaDataDirectory()));
    }

    @Test
    public void whenResourceChangeListenerIsCreated_thenTheEventTypesHaveBeenInitializedCorrectly() {
        final FileResourceEventType[] actualEventTypes = resourceChangeListener.getEventTypes();
        assertThat("Event types not initalized", Arrays.asList(actualEventTypes),
                hasItems(FileResourceEventType.FILE_MODIFIED, FileResourceEventType.FILE_CREATED));
    }

    @Test
    public void whenResourceChangeListenerIsCreated_thenTheEventCallbackHasBeenInitializedCorrectly() {
        resourceChangeListener.setCallbackListner(resourceChangeCallback);
        resourceChangeListener.onEvent(fileResourceEvent);
        verify(resourceChangeCallback, times(1)).resourceChanged();
    }

    @Test
    public void whenComparingTwoListeners_thenEqualsShouldReturnTrue() {
        assertEquals(resourceChangeListener, new ResourceChangeListener());
    }

    @Test
    public void whenCompartingTwoListeners_andOneIsNull_thenEqualsShouldBeFalse() {
        assertNotEquals(resourceChangeListener, null);
    }

    @Test
    public void whenComparingTwoListeners_andTheyAreEqual_thenHashCodesShouldBeEqual() {
        assertEquals(resourceChangeListener.hashCode(), new ResourceChangeListener().hashCode());
    }
}
