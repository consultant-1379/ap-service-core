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
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class CreateLicenseRequestSpec extends CdiSpecification {

    private static final String FINGERPRINT = "fingerprint_value"
    private static final String SWLTID = "swltId_value"
    private static final String SOFTWARE_PACKAGE_NAME = "softwarePackageName_value"
    private static final String HARDWARE_TYPE = "hardwareType_value"
    private static final List<String> RADIO_ACCESS_TECHNOLOGIES = new ArrayList<>()
    private static final String GROUP_ID = "groupId"

    def setup() {
        RADIO_ACCESS_TECHNOLOGIES.add("LTE")
    }

    def "Test Create License Request constructor works as expected"() {
        when: "Create object with params passed into the constructor"
            CreateLicenseRequestDto request = new CreateLicenseRequestDto(FINGERPRINT, SWLTID, SOFTWARE_PACKAGE_NAME,
                    HARDWARE_TYPE, RADIO_ACCESS_TECHNOLOGIES, GROUP_ID)

        then: "Getters return expected values"
            request.getFingerprint() == FINGERPRINT
            request.getSwltId() == SWLTID
            request.getSoftwarePackageName() == SOFTWARE_PACKAGE_NAME
            request.getHardwareType() == HARDWARE_TYPE
            request.getRadioAccessTechnologies() == RADIO_ACCESS_TECHNOLOGIES
            request.getGroupId() == GROUP_ID
    }

    def "Setters work as expected" () {
        given: "Object created"
            CreateLicenseRequestDto request = new CreateLicenseRequestDto()

        when: "Setters are called"
            request.setFingerprint(FINGERPRINT)
            request.setSwltId(SWLTID)
            request.setSoftwarePackageName(SOFTWARE_PACKAGE_NAME)
            request.setHardwareType(HARDWARE_TYPE)
            request.setRadioAccessTechnologies(RADIO_ACCESS_TECHNOLOGIES)
            request.setGroupId(GROUP_ID)

        then: "Getters return expected values"
            request.getFingerprint() == FINGERPRINT
            request.getSwltId() == SWLTID
            request.getSoftwarePackageName() == SOFTWARE_PACKAGE_NAME
            request.getHardwareType() == HARDWARE_TYPE
            request.getRadioAccessTechnologies() == RADIO_ACCESS_TECHNOLOGIES
            request.getGroupId() == GROUP_ID
    }
}
