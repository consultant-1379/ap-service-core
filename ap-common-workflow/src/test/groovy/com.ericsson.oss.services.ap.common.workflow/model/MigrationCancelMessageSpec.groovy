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
import com.ericsson.oss.services.ap.common.workflow.messages.MigrationCancelMessage

/**
 * Test class for {@link MigrationCancelMessage}
 */
class MigrationCancelMessageSpec extends CdiSpecification {

    def "Verify getMessageKey method retrieve expected key value"() {
        when:
            def messageKey = MigrationCancelMessage.getMessageKey()

        then:
            "MIGRATION_CANCEL".equals(messageKey)
    }
}