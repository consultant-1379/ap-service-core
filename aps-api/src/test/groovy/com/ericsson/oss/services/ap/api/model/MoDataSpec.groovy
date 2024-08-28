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
 * Unit tests for {@link MoData}.
 */
class MoDataSpec extends Specification {

    private static final String ATTRIBUTE_NAME = 'key'
    private static final Object ATTRIBUTE_VALUE = new Object()
    private static final String FDN = 'MeContext=1,ManagedElement=1'
    private static final String MO_TYPE = 'MeContext'
    private static final MODEL_DATA = new ModelData('OSS_TOP','v1.1.1')
    @Shared Map<String, Object> attributes = [(ATTRIBUTE_NAME):ATTRIBUTE_VALUE]

    @Shared MoData moData = new MoData(FDN, attributes, MO_TYPE, MODEL_DATA)

    def 'The correct fields are returned when they are requested'() {
        expect:
            moData.getFdn() == FDN
            moData.getAttributes() == attributes
            moData.getModelData() == MODEL_DATA
            moData.getType() == MO_TYPE
    }

    def 'Requesting a specific attribute will return it if it exists, otherwise return null'() {
        expect:
            moData.getAttribute(ATTRIBUTE_NAME) == ATTRIBUTE_VALUE
            moData.getAttribute("wrongKey") == null
    }
}