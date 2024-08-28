/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.message.resources;

import java.io.IOException;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Produces a {@link MessageResources} initialised with the class that is injecting and the message resource bundle that is being used.
 */
public class MessageResourcesProducer {

    private static final String AP_MESSAGE_PROPERTIES_FILE = "ap_messages.properties";

    @Produces
    @Default
    @MessageResourceInfo
    public MessageResources createResource(final InjectionPoint injPoint) throws IOException {
        final MessageResourceInfo messageResource = injPoint.getAnnotated().getAnnotation(MessageResourceInfo.class);
        final Class<?> thisClass = injPoint.getBean().getBeanClass();
        final String resource = getResource(messageResource);
        return new PropertyMessageResources(thisClass, resource);
    }

    private static String getResource(final MessageResourceInfo messageResource) {
        if (messageResource == null) {
            return AP_MESSAGE_PROPERTIES_FILE;
        }

        final String resourceName = messageResource.messageResource();
        return resourceName.isEmpty() ? AP_MESSAGE_PROPERTIES_FILE : resourceName;
    }
}
