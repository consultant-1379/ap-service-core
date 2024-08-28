/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.messages

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class SkipMessageSpec extends CdiSpecification {

    def "When getMessageKey method is called then expected SKIP message key is returned"() {
        given: "generate a SkipMessage"
            SkipMessage skipMessage = new SkipMessage()

        when: "call from method to get SKIP message key"
            def skipMessageKey = skipMessage.getMessageKey()

        then: "get SKIP message key as expected"
            skipMessageKey.equals("SKIP")
    }
}