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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Returns {@code ExceptionMapper} instances for a given {@link Throwable}.
 */
public class ExceptionMapperFactory {

    @Inject
    @DefaultExceptionMapper
    private ExceptionMapper<? extends Throwable> defaultExceptionMapper;

    @Inject
    @Any
    private Instance<ExceptionMapper<? extends Throwable>> exceptionMapperInstances;

    private Map<String, ExceptionMapper<? extends Throwable>> exceptionMappers = new HashMap<>();

    /**
     * Finds and caches all declared {@link ExceptionMapper} upon startup.
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void cacheExceptionMappers() {
        final boolean hasAnyInstances = exceptionMapperInstances != null &&
            exceptionMapperInstances.iterator() != null &&
            exceptionMapperInstances.iterator().hasNext();

        if (hasAnyInstances) {
            for (final ExceptionMapper<? extends Throwable> exceptionMapper : exceptionMapperInstances) {
                final Class<?> parameterizedClass = getParameterizedClass((ExceptionMapper<? extends Throwable>) exceptionMapper);

                if (parameterizedClass != null) {
                    exceptionMappers.put(parameterizedClass.getName(), exceptionMapper);
                }
            }
        }
    }

    /**
     * Find {@code ExceptionMapper} which handles the given {@link Throwable}. Looks for an exact match, if no match found then the default
     * {@code ExceptionMapper} is returned.
     *
     * @param genericThrowable the throwable to be mapped
     * @return {@code ExceptionMapper} for the specific exception or the default if no exact match found
     */
    @SuppressWarnings("unchecked")
    public ExceptionMapper<Throwable> find(final Throwable genericThrowable) {
        return (ExceptionMapper<Throwable>) exceptionMappers.getOrDefault(genericThrowable.getClass().getName(), defaultExceptionMapper);
    }

    private Class<?> getParameterizedClass(final ExceptionMapper<? extends Throwable> exceptionMapper) {
        final Class<?> parametrizedClass = extractParametrizedClassFromTypes(exceptionMapper.getClass().getGenericInterfaces());

        /*
         * If we have a WeldProxy class here, it won't be able to find the ParametrizedType on the Proxy,
         * so we need to get the Generic Superclass, which is the ExceptionMapper itself and try again.
         *
         * We're unproxying the class type here in order to check for the exception type.
         */
        if (parametrizedClass == null) {
            final Type exceptionMapperSuperclass = exceptionMapper.getClass().getGenericSuperclass();
            return extractParametrizedClassFromTypes(((Class<?>) exceptionMapperSuperclass).getGenericInterfaces());
        }

        return parametrizedClass;
    }

    private Class<?> extractParametrizedClassFromTypes(final Type[] types) {
        if (types.length == 0) {
            return null;
        }

        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                final Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();

                if (actualTypeArguments.length > 0) {
                    return (Class<?>) actualTypeArguments[0];
                }
            }
        }

        return null;
    }
}