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
package com.ericsson.oss.services.ap.api.model

import spock.lang.Shared
import spock.lang.Specification

/**
 * Unit tests for {@link ModelData}.
 */
class ModelDataSpec extends Specification {

    @Shared ModelData modelData1 = new ModelData('ns1','1.0.0')
    @Shared ModelData modelData2

    def 'An IllegalArgumentException is thrown if ModelData is instantiated with a null or empty value'() {
        when: 'ModelData is instantiated'
            modelData2 = new ModelData(nameSpace,nameSpaceVersion)

        then:
            thrown(IllegalArgumentException)

        where:
            nameSpace << [null, '', 'OSS_TOP', 'OSS_TOP']
            nameSpaceVersion << ['v1.1.1', 'v1.1.1', null, '']
    }

    def 'If two ModelData objects have the same arguments, they are the same instance, and have the same hash code'() {
        when: 'ModelData is instantiated with model data'
            modelData2 = new ModelData(nameSpace,nameSpaceVersion)

        then: 'Test if the two objects are equal'
            modelData2.equals(modelData1) == expectedResult
        and: 'Test if they have the same hash code'
            modelData2.hashCode().equals(modelData1.hashCode()) == expectedResult

        where:
            nameSpace | nameSpaceVersion || expectedResult
            'ns1'     | '1.0.0'          || true
            'ns2'     | '1.0.0'          || false
            'ns1'     | '2.0.0'          || false
    }

    def 'When a ModelData instance is properly defined, then it is valid'() {
        when:
            modelData2 = new ModelData('ns1','1.0.0')

        then:
            modelData2.equals(object) == expectedResult

        where:
            object       || expectedResult
            modelData1   || true
            null         || false
            new Object() || false
    }
}