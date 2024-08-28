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
package com.ericsson.oss.services.ap.core.rest.builder

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.Namespace
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.core.rest.model.request.builder.ProfileMoDataBuilder
import com.ericsson.oss.services.ap.core.rest.model.request.profile.ProfileRequest

class ProfileMoDataBuilderSpec extends CdiSpecification {

    @ObjectUnderTest
    private ProfileMoDataBuilder profileMoDataBuilder

    @MockedImplementation
    private static ModelReader modelReader

    private static final String INTEGRATION_DATA_TYPE = "node-plugin-request-action"
    private static final String EXPANSION_DATA_TYPE = "node-plugin-request-action-expansion"
    private static final String PROJECT_FDN = "Project=Project1"
    private static final String CONFIG_SANPSHOT_STATUS = "NOT_STARTED"
    private static ProfileRequest profileRequest

    def "Build profile data successfully for Integration data type"() {
        given: "The build profile service will return mock MoData"
        profileRequest = new ProfileRequest();
        profileRequest.setDataType(INTEGRATION_DATA_TYPE)
        profileRequest.setConfigSnapshotStatus(CONFIG_SANPSHOT_STATUS)
        profileRequest.setDumpTimeStamp(Long.valueOf(1000))

        when: "the build profile data is called"
        profileMoDataBuilder.buildMoData(PROJECT_FDN,profileRequest)

        then: "Profile data returned"
        String dataType = profileRequest.getDataType()
        String configSnapshotStatus = profileRequest.getConfigSnapshotStatus()
        Long dumpTimeStamp = profileRequest.getDumpTimeStamp()
        dataType==INTEGRATION_DATA_TYPE
        configSnapshotStatus==CONFIG_SANPSHOT_STATUS
        dumpTimeStamp.value==1000
    }

    def "Build profile data successfully for Expansion data type"() {
        given: "The build profile service will return mock MoData"
        profileRequest = new ProfileRequest();
        profileRequest.setDataType(EXPANSION_DATA_TYPE)

        when: "the build profile data is called"
        profileMoDataBuilder.buildMoData(PROJECT_FDN,profileRequest)

        then: "the model reader is called once"
        1 * modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString())
    }

    def "Build profile data successfully with data type value as null"() {
        given: "The build profile service will return mock MoData"
        profileRequest = new ProfileRequest();
        profileRequest.setDataType(null)

        when: "the build profile data is called"
        profileMoDataBuilder.buildMoData(PROJECT_FDN,profileRequest)

        then: "Profile data returned"
        String dataType = profileRequest.getDataType()
        dataType==null
    }

}
