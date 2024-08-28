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
package com.ericsson.oss.services.ap.api.validation.rules;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Group for rules.
 */
@Retention(RUNTIME)
@Target({ TYPE })
public @interface Group {

    /**
     * The name of the rule group.
     * @return String
     *     the name of the group
     */
    String name();

    /**
     * The priority of the group. Lower numbers means higher priority.
     * @return int
     *     the group priority
     */
    int priority() default 0;

    /**
     * Whether the validation engine aborts on the failure of this group, or continues.
     * 
     * @return boolean
     */
    boolean abortOnFail() default false;
}
