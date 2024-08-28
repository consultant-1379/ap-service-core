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

package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Unit tests for {@link ValidStatesForEventMapper}.
 */
class ValidStatesForEventMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ValidStatesForEventMapper validStatesForEventMapper

    def "When get valid states then states are returned and internal states are not displayed" () {

        when:
        final String result = validStatesForEventMapper.getValidStates("order")

        then:
        assertEquals("Order Failed, Order Cancelled", result)
    }


    def "When get valid states then command name is case insensitive" () {

        when:
        final String result = validStatesForEventMapper.getValidStates("BIND")

        then:
        assertEquals("Bind Completed, Order Completed, Hardware Replace Bind Completed, Pre Migration Bind Completed, Pre Migration Completed", result)
    }


    def "When get valid states and input has no transition event then empty string is returned" () {

        when:
        final String result = validStatesForEventMapper.getValidStates("resume")

        then:
        assertTrue(result.isEmpty())
    }

    def "When get valid states command name is null then throw NullPointerException" () {

        when:
        validStatesForEventMapper.getValidStates(null)

        then:
        thrown(NullPointerException)
    }
}
