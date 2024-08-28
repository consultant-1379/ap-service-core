/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException

class ActivityTypeSpec extends CdiSpecification {

    def "When given a valid activity string input then expected ActivityType can be retrieved"() {
        when: "Call from method to convert string to ActivityType"
        def enumActivity = ActivityType.from(activity)

        then: "Get ActivityType as expected"
        enumActivity.equals(expectedEnumActivity)

        where:
        activity           |   expectedEnumActivity
        "expansion"        |   ActivityType.EXPANSION_ACTIVITY
        "greenfield"       |   ActivityType.GREENFIELD_ACTIVITY
        "hardwareReplace"  |   ActivityType.HARDWARE_REPLACE_ACTIVITY
    }

    def "When given an invalid string input then exception is thrown"() {
        when: "Call fromString method with invalid string status"
        def enumActivity = ActivityType.from("Unknown_Activity")

        then: "InvalidArgumentsException is thrown"
        enumActivity == null
        thrown(InvalidArgumentsException)
    }
}
