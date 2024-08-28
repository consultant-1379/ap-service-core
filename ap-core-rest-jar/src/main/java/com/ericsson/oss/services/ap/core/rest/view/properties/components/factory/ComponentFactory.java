/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.view.properties.components.factory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.components.Component;

/**
 * Factory for producing the correct {@link Component} type for the given metadata type. The factory contains all objects of type {@link Component}.
 */
public class ComponentFactory {

    @Inject
    @Any
    private Instance<Component<? extends Metadata>> viewComponents;

    /**
     * Returns the correct {@link Component} type for the given metadata type.
     * <p>
     * The factory matches the metadata type on the Component interface.
     * <p>
     * For example:
     *
     * <pre>
     * class LineComponent implements Component{@literal <}LineMetadata{@literal >}
     * </pre>
     *
     * The factory matches the LineMetadata type parameter on the component interface to the metadata that needs to be handled.
     *
     * @param metadataToHandle
     *            metadata to find component for
     * @return the component or null if no match
     */
    @SuppressWarnings("unchecked")
    public Component<Metadata> getViewComponent(final Metadata metadataToHandle) {
        for (final Component<? extends Metadata> viewComponent : viewComponents) {
            final Type[] metadataToHandleInterfaceTypes = metadataToHandle.getClass().getGenericInterfaces();
            final Class<Metadata> metadataToHandleInterfaceType = (Class<Metadata>) metadataToHandleInterfaceTypes[0];

            if (getParameterizedTypeFromComponent(viewComponent).equals(metadataToHandleInterfaceType.getCanonicalName())) {
                viewComponent.setComponentMetadata(metadataToHandle);
                return (Component<Metadata>) viewComponent;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getParameterizedTypeFromComponent(final Component<? extends Metadata> component) {
        final Type[] types = component.getClass().getGenericInterfaces();
        final ParameterizedType parameterizedType = (ParameterizedType) types[0];
        final Type typeArgument = parameterizedType.getActualTypeArguments()[0];

        final Class<Metadata> metaDatatypeClass = (Class<Metadata>) typeArgument;
        return metaDatatypeClass.getCanonicalName();
    }
}
