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
package com.ericsson.oss.services.ap.core.rest.validation

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

import com.ericsson.cds.cdi.support.spock.CdiSpecification

import spock.lang.Unroll

class ValidNameSpec extends CdiSpecification {

    private Validator validator


    def setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory()
        validator = factory.getValidator()
    }

    @Unroll
    def "Name validation"() {
        given: "dummyPojo is created with annotated field"
        DummyPojo dummyPojo = new DummyPojo()
        dummyPojo.name = name

        when: "validation rules are executed"
        final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(dummyPojo)

        then: "validation contains correct number of violations"
        constraintViolations.size() == numberOfViolations

        where:
        description                                        | name             | numberOfViolations
        "validation passes for valid name"                 | "Project1"       | 0
        "validation passes for name containing _"          | "Project_mine_1" | 0
        "validation passes for name containing -"          | "Project-mine-1" | 0
        "validation passes for name containing ."          | "ProjectV19.4"   | 0
        "validation passes for name containing numbers"    | "ProjectV194"    | 0
        "validation fails for characters less than 3"      | "Pr"             | 1
        "validation fails for null"                        | null             | 1
        "validation fails for name containing white space" | "Project 1"      | 1
        "validation fails for name containing >"           | "Project>1"      | 1
        "validation fails for name containing >!£"         | "Project>!£1"    | 1
        "validation fails for name %%"                     | "%%"             | 2
        "validation fails for name containing brackets"    | "Project(1)"     | 1

    }

    class DummyPojo {

        @ValidName
        String name;
    }

}
