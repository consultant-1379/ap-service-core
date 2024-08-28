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
package com.ericsson.oss.services.ap.core.rest.client.flowautomation

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class AmosExecuteRequestBodySpec extends CdiSpecification {

    def "when ignoreError in execute request is set with different value then isIgnoreError returns the correct setting"() {
        given: "a request body with ignoreError to true"
            AmosExecuteRequestBody amosExecuteRequestBody = new AmosExecuteRequestBody("LTE01dg2ERBS00002", "scriptname.mos", null, true);
        when: "ignoreError in amos execute request body set to different vaules"
            amosExecuteRequestBody.setIgnoreError(false)
            boolean ignoreError1 = amosExecuteRequestBody.isIgnoreError()
            amosExecuteRequestBody.setIgnoreError(true)
            boolean ignoreError2 = amosExecuteRequestBody.isIgnoreError()

        then: "the isIgnoreError returns the correct setting"
            ignoreError1 == false
            ignoreError2 == true
    }

}
