/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.message.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link MessageResourcesProducer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageResourcesProducerTest {

    @Mock
    private Annotated annotated;

    @Mock
    @SuppressWarnings("rawtypes")
    private Bean bean;

    @Mock
    private InjectionPoint injectionPoint;

    @Mock
    private MessageResourceInfo messageResourceInfo;

    private final MessageResourcesProducer messageResourcesProducer = new MessageResourcesProducer();

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        when(injectionPoint.getAnnotated()).thenReturn(annotated);
        when(injectionPoint.getBean()).thenReturn(bean);
    }

    @Test
    public void whenCreatingResourcesAndInjectionPointHasNoAnnotationThenMessageResourceWithDefaultPropertyFileIsReturned() throws IOException {
        when(annotated.getAnnotation(MessageResourceInfo.class)).thenReturn(null);
        final MessageResources result = messageResourcesProducer.createResource(injectionPoint);
        assertEquals(123, result.getInt("integer"));
    }

    @Test
    public void whenCreatingResourcesAndInjectionPointAnnotationHasNoNameThenDefaultMessageResourcesIsReturned() throws IOException {
        when(annotated.getAnnotation(MessageResourceInfo.class)).thenReturn(messageResourceInfo);
        when(messageResourceInfo.messageResource()).thenReturn("");
        final MessageResources result = messageResourcesProducer.createResource(injectionPoint);
        assertEquals(123, result.getInt("integer"));
    }

    @Test
    public void whenCreatingResourcesAndInjectionPointHasANameThenDefinedMessageResourcesIsReturned() throws IOException {
        when(annotated.getAnnotation(MessageResourceInfo.class)).thenReturn(messageResourceInfo);
        when(messageResourceInfo.messageResource()).thenReturn("test-messages.properties");
        final MessageResources result = messageResourcesProducer.createResource(injectionPoint);
        assertEquals(123, result.getInt("int1"));
    }
}
