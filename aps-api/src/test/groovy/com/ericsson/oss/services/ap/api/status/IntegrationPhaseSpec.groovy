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
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status

import spock.lang.Specification

/**
 * Unit tests for {@link IntegrationPhase}.
 */
class IntegrationPhaseSpec extends Specification {

    def 'when get integration phase and ap node state is valid then correct integration phase is returned'() {
        when:
            final String validNodeState = 'ORDER_COMPLETED'

            final IntegrationPhase result = IntegrationPhase.getIntegrationPhase(validNodeState)

        then:
            result.getName() == IntegrationPhase.IN_PROGRESS.getName()
    }

    def 'when get integration phase and state is invalid then illegal argument exception is thrown'() {
        given:
            final String invalidNodeState = 'new state'

        when:
            IntegrationPhase.getIntegrationPhase(invalidNodeState)

        then:
            thrown(IllegalArgumentException)
    }

}