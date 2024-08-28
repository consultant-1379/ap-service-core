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
package com.ericsson.oss.services.ap.core.rest.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Validation constraint ensuring name is not null, name length range is [3,100) and name only contains alphanumeric characters and special characters
 * dot(.), dash(-) and underscore(_)
 */
@Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters inclusive")
@NotNull
@Pattern(regexp = "^([a-zA-Z0-9._-])*$", message = "Only alphanumeric characters and special characters dot(.), dash(-) and underscore(_) are allowed")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Constraint(validatedBy = {})
@Documented
public @interface ValidName {

    String message() default "Name is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
