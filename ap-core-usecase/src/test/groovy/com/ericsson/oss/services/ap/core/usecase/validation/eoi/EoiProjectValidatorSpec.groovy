/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationEngine
import com.ericsson.oss.services.ap.core.usecase.importproject.EoiProjectValidator

import javax.inject.Inject

class EoiProjectValidatorSpec extends CdiSpecification {

    @ObjectUnderTest
    private EoiProjectValidator eoiProjectValidator

    @Inject
    private ValidationEngine validationEngine

    final Map<String, Object> elements = new HashMap<>()


    def "when eoi project validated successfully THEN no validation exception thrown"() {
        given: "a valid project"
        Map<String, Object> attributes = [
                nodeName : "LTE01",
                neType   : "ERBS",
                cnfType  : "cnftype",
                ipAddress: "1.2.3.4"
        ]

        final List list = new ArrayList()
        list.add(attributes)

        elements.put("networkelements", list)
        validationEngine.validate(_) >> true

        when: "validate the project"
        eoiProjectValidator.validateStandardProject(elements)

        then: "the validation is successful and no exception thrown"
        1 * validationEngine.validate(_)
        noExceptionThrown()
    }
}
