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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.core.rest.model.profile.Profile
import com.ericsson.oss.services.ap.core.rest.model.profile.ProfileData

class ProfileDataBuilderSpec extends CdiSpecification {

    @ObjectUnderTest
    ProfileDataBuilder profileDataBuilder;

    private static final String CIQ_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/ciq/ciq.csv"
    private static final String DETAIL1 = "detail1"

    private static final String GRAPHIC_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/graphic.png"
    private static final String PRODUCT_NUMBER = "R23456"
    private static final String PROFILE_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/profile1.zip"
    private static final String PROFILE_NAME = "profile1"
    private static final String PROJECT_NAME = "project1"
    private static final String PROPERTIES = "{\"prop\":\"prop1\"}"
    private static final String DATA_TYPE = "node-plugin-request-action"
    private static final String CONFIG_SNAPSHOT_STATUS = "NOT_STARTED"
    private static final Long DEFAULT_DUMP_TIMESTAMP = Long.valueOf(0)
    private static final String FILTER_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/get-node-config-snapshot.xml"

    private static MoData moData
    private static String fdn
    private static List<MoData> moDataList
    private ProfileData data
    private static Profile profile

    def setupSpec() {
        fdn = String.format("Project=%s,Profile=%s", PROJECT_NAME, PROFILE_NAME)
        Map<String, Object> attributes = [
                "profileId"             : PROFILE_NAME,
                "version"               : [
                        "productNumber" : PRODUCT_NUMBER,
                        "productRelease": "R345556"
                ],
                "properties"            : PROPERTIES,
                "graphicLocation"       : GRAPHIC_LOCATION,
                "profileContentLocation": PROFILE_LOCATION,
                "ciq"                   : [
                        "ciqLocation": CIQ_LOCATION
                ],
                "profileStatus"         : [
                        "isValid"       : true,
                        "profileDetails": [DETAIL1]],
                "dataType" : DATA_TYPE,
                "configSnapshotStatus"  : CONFIG_SNAPSHOT_STATUS,
                "dumpTimeStamp"     : DEFAULT_DUMP_TIMESTAMP,
                "filterLocation"       : FILTER_LOCATION
        ]
        final Map<String, Object> profileAttributes = new LinkedHashMap<>()
        profileAttributes.putAll(new TreeMap<>(attributes))
        moData = new MoData(fdn, profileAttributes, MoType.CONFIGURATION_PROFILE.toString(), null)
    }

    def "Build profile data successfully"() {
        given: "The build profile service will return mock MoData"
        profile = new Profile()
        profile.setDataType(DATA_TYPE)
        profile.setConfigSnapshotStatus(CONFIG_SNAPSHOT_STATUS)
        profile.setDumpTimeStamp(DEFAULT_DUMP_TIMESTAMP)
        moDataList = new ArrayList<>()
        moDataList.add(moData)

        when: "the build profile data is called"
        data = profileDataBuilder.buildProfileData(moDataList)

        then: "Profile data returned"
        profile.getDataType() == DATA_TYPE
        profile.getConfigSnapshotStatus() == CONFIG_SNAPSHOT_STATUS
        profile.getDumpTimeStamp().equals(DEFAULT_DUMP_TIMESTAMP)
    }
}
