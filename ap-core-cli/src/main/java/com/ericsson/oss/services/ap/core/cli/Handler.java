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
package com.ericsson.oss.services.ap.core.cli;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;

/**
 * Handler annotation to tag all command handlers. All command handlers wishing to be found by the CliCommandHandlerFactory must be annotated with
 * this annotation. For example:
 *
 * <pre>
 * &#64;Handler(name = UseCaseName.ORDER)
 * public class OrderCommandHandler extends CliCommandHandler {
 * </pre>
 */
@Qualifier
@Retention(RUNTIME)
@Target({ TYPE, METHOD, FIELD, PARAMETER })
public @interface Handler {

    @Nonbinding
    UseCaseName name() default UseCaseName.UNSUPPORTED;
}
