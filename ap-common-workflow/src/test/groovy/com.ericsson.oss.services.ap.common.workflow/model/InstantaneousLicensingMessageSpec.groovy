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
package com.ericsson.oss.services.ap.common.workflow.model

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.workflow.messages.InstantaneousLicensingMessage

/**
 * Test class for {@link InstantaneousLicensingMessage}
 */
class InstantaneousLicensingMessageSpec extends CdiSpecification {

    def "Verify instantaneous licensing messages content is as expected"() {
        when:
            String message = testCall.toString()
        then:
            message == expectedMessage
        where:
            testCall                                             | expectedMessage
            InstantaneousLicensingMessage.getFailedMessage()     | "INSTANTANEOUS_LICENSING_FAILED"
            InstantaneousLicensingMessage.getCompletedMessage()  | "INSTANTANEOUS_LICENSING_COMPLETED"
            InstantaneousLicensingMessage.getRunningMessage()    | "INSTANTANEOUS_LICENSING_RUNNING"
            InstantaneousLicensingMessage.getResultKey()         | "result"
            InstantaneousLicensingMessage.getAdditionalInfoKey() | "additionalInfo"
            InstantaneousLicensingMessage.getRequestId()         | "requestId"
    }
}
