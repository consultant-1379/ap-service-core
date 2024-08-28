/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;

/**
 * Interceptor binding annotation for validating an fdn exists. Used on REST endpoint methods in conjunction
 * with the {@link InjectFdn} annotation to inject the resolved fdn into the target method parameter.
 * <p>Takes a usecase name which is used to provide context for the exception mappers
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@InterceptorBinding
public @interface ResolveFdn {
    /**
     * @return UseCaseName Used in exception mappers if exception is thrown
     */
    @Nonbinding UseCaseName usecase() default UseCaseName.UNSUPPORTED;

}
