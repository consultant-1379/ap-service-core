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
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status

import spock.lang.Specification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;

/**
 * Unit tests for {@link ActivityPhaseSpec}.
 */
class ActivityPhaseSpec extends Specification {

    def 'when get activity phase and ap node state is belong to premigration states then premigration phase is returned'() {
        given:
            final String validNodeState = 'PRE_MIGRATION_CANCELLED'

        when:
            final ActivityPhase result = ActivityPhase.getActivityPhase(validNodeState)

        then:
            result.getName() == ActivityPhase.PREMIGRATION_PHASE.getName()
    }

    def 'when get activity phase and ap node state is not belong to defined activity phases then unknown phase is returned'() {
        given:
            final String invalidNodeState = 'MIGRATION_CANCELLED'

        when:
            final ActivityPhase result = ActivityPhase.getActivityPhase(invalidNodeState)

        then:
            result.getName() == ActivityPhase.MIGRATION_PHASE.getName()
    }

    def 'when get activity phase and ap node state is invalid then unknown phase is returned'() {
        given:
            final String invalidNodeState = 'not defined'

        when:
            final ActivityPhase result = ActivityPhase.getActivityPhase(invalidNodeState)

        then:
            thrown(ApApplicationException)
            null == result
    }

}