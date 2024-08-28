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
package com.ericsson.oss.services.ap.api.bind

import spock.lang.Shared
import spock.lang.Specification

/**
 * Unit tests for {@link BatchBindResult}
 */
class BatchBindResultSpec extends Specification {

    private static final Map<String, String> successfulNodes = ['ABC1234567':'Node1', 'ABC1234568':'Node2', 'ABC1234569':'Node3']
    private static final Set<String> HWSerialNumbers = successfulNodes.keySet()

    @Shared BatchBindResult batchBindResult
    @Shared BatchBindResult threeSuccessesTwoFails = new BatchBindResult.Builder()
            .withBindSuccess(HWSerialNumbers[0], successfulNodes.get(HWSerialNumbers[0]))
            .withBindSuccess(HWSerialNumbers[1], successfulNodes.get(HWSerialNumbers[1]))
            .withBindSuccess(HWSerialNumbers[2], successfulNodes.get(HWSerialNumbers[2]))
            .withBindFailure('ErrorMessage1')
            .withBindFailure('ErrorMessage2')
            .build()

    def 'When batch bind has no errors, then the result is successful'() {
        when: 'A batch bind consisting of a single node was successful'
            batchBindResult = new BatchBindResult.Builder()
                    .withBindSuccess(HWSerialNumbers[0], successfulNodes.get(HWSerialNumbers[0]))
                    .build()

        then: 'Only isSuccessful() returns true'
            batchBindResult.isSuccessful()
            !batchBindResult.isFailed()
            !batchBindResult.isPartial()
    }

    def 'When batch bind has errors and is not successful, then the result is failed'() {
        when: 'A batch bind consisting of a single node failed'
            batchBindResult = new BatchBindResult.Builder()
                    .withBindFailure('ErrorMessage1')
                    .build()

        then: 'Only isFailed() returns true'
            !batchBindResult.isSuccessful()
            batchBindResult.isFailed()
            !batchBindResult.isPartial()
    }

    def 'When batch bind has errors and is successful, then the result is a partial success'() {
        when: 'A batch bind consisting of one successful node and one failure'
            batchBindResult = new BatchBindResult.Builder()
                    .withBindSuccess(HWSerialNumbers[0], successfulNodes.get(HWSerialNumbers[0]))
                    .withBindFailure('ErrorMessage1')
                    .build()

        then: 'Only isPartial() returns true'
            !batchBindResult.isSuccessful()
            !batchBindResult.isFailed()
            batchBindResult.isPartial()
    }

    def 'When batch bind has no errors and has no success, then the result is successful'() {
        when: 'An empty batch bind'
            batchBindResult = new BatchBindResult.Builder().build()

        then: 'Only isSuccessful() returns true'
            batchBindResult.isSuccessful()
            !batchBindResult.isFailed()
            !batchBindResult.isPartial()
    }

    def 'The correct number of total, successful and failed binds are returned when requested'() {
        expect:
            threeSuccessesTwoFails.getTotalBinds() == 5
            threeSuccessesTwoFails.getSuccessfulBinds() == 3
            threeSuccessesTwoFails.getFailedBinds() == 2
    }

    def 'When getSuccessfulDetails() is called, then only the details of all successful binds are returned'() {
        when: 'The details of only successful binds are requested'
            final Map<String, String> successfulBindDetails = threeSuccessesTwoFails.getSuccessfulBindDetails()

        then: 'Only the three successful binds are returned'
            successfulBindDetails.size() == 3
            successfulBindDetails == successfulNodes
    }

    def 'When getFailedBindMessages() is called, then only the error messages for all failed binds are returned'() {
        when: 'Only the failed binds are requested'
            final List<String> failedBindErrorMessages = threeSuccessesTwoFails.getFailedBindMessages()

        then: 'Only the error messages from the two failed binds are returned'
            failedBindErrorMessages.size() == 2
            failedBindErrorMessages == ['ErrorMessage1','ErrorMessage2']
    }
}