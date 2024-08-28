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
package com.ericsson.oss.services.ap.core.usecase;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Factory class to instantiate named usecase implementations.
 * <p>
 * Class is recognised as a usecase if it is annotated with the {@link UseCase} qualifier.
 */
public class UseCaseFactory {

    @Inject
    @Any
    @UseCase
    private Instance<Object> usecases;

    /**
     * Gets an instance of the named usecase.
     *
     * @param useCaseName
     *            the name of the usecase
     * @param <T>
     *            the usecase
     * @return usecase instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getNamedUsecase(final UseCaseName useCaseName) {
        final Object useCase = getUseCase(useCaseName);
        return (T) useCase;
    }

    private Object getUseCase(final UseCaseName useCaseName) {
        Object matchingUseCase = null;
        for (final Object usecase : usecases) {
            final Class<?> useCaseClass = usecase.getClass().isAnnotationPresent(UseCase.class) ? usecase.getClass()
                    : usecase.getClass().getSuperclass();
            if (useCaseClass.isAnnotationPresent(UseCase.class) && useCaseName == useCaseClass.getAnnotation(UseCase.class).name()) {
                matchingUseCase = usecase;
                continue;
            }
            usecases.destroy(usecase);
        }
        if(matchingUseCase == null) {
                throw new IllegalArgumentException("No matching UseCase found for " + useCaseName);
        }
        else {
                return matchingUseCase;
        }
    }
}
