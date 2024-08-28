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
package com.ericsson.oss.services.ap.core.cli.view.components.factory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.cli.view.components.Component;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;

/**
 * Factory for producing the correct {@link Component} type for the given metadata type. The factory contains all objects of type {@link Component}.
 */
public class ComponentFactory { //NOSONAR EAP7 CDI libraries supports different way of handling generics.

    @Inject
    @Any
    private Instance<Component<? extends Metadata>> viewComponents;

    @SuppressWarnings("unchecked")
    private static String getParameterizedTypeFromComponent(final Component<? extends Metadata> component) {
        final Type[] types = component.getClass().getGenericInterfaces();
        final ParameterizedType parameterizedType = (ParameterizedType) types[0];
        final Type typeArgument = parameterizedType.getActualTypeArguments()[0];

        final Class<Metadata> metaDatatypeClass = (Class<Metadata>) typeArgument;
        return metaDatatypeClass.getCanonicalName();
    }

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
     * The factory will match the LineMetadata type parameter on the component interface to the metadata that needs to be handled.
     *
     * @param metadataToHandle
     *            metadata to find component for
     * @return the component or null if no match
     */
    @SuppressWarnings("unchecked")
    public Component<Metadata> getViewComponent(final Metadata metadataToHandle) {
        Component<Metadata> component = null;
        for (final Component<? extends Metadata> viewComponent : viewComponents) {
            final Type[] metadataToHandleInterfaceTypes = metadataToHandle.getClass().getGenericInterfaces();
            final Class<Metadata> metadataToHandleInterfaceType = (Class<Metadata>) metadataToHandleInterfaceTypes[0];

            if (getParameterizedTypeFromComponent(viewComponent).equals(metadataToHandleInterfaceType.getCanonicalName())) {
                viewComponent.setComponentMetadata(metadataToHandle);
                component = (Component<Metadata>) viewComponent;
                continue;
            }
            viewComponents.destroy(viewComponent);
        }
        return component == null? null : component;
    }
}
