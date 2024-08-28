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
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.testutils

import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.model.MoType

class TestConfigurationProfileFactory {

    def static setupFullProfile(fdn, profileName, properties, details, modelData) {
        List<Map<String, Object>> configurations = [
                [
                        "name"   : "profile.zip",
                        "content": "dGVzdA=="
                ]
        ]
        Map<String, Object> profileWithFiles = [
                "name"          : profileName,
                "graphic"       : [
                        "name"   : "graphic.png",
                        "content": "dGVzdA=="
                ],
                "ciq"           : [
                        "name"   : "ciq.csv",
                        "content": "dGVzdA=="
                ],
                "configurations": configurations,
                "properties"    : properties,
                "profileStatus" : [
                        "isValid"       : true,
                        "profileDetails": [details]
                ],
                "version"       : [
                        "productNumber" : "R3773",
                        "productRelease": "R473939"
                ],
                "dataType"      : "INTEGRATION",
                "getConfigScript"       : [
                        "name"   : "get-node-config-snapshot.xml",
                        "content": "dGVzdA=="
                ]
        ]
        final Map<String, Object> profileAttributes = new LinkedHashMap<>()
        profileAttributes.putAll(new TreeMap<>(profileWithFiles))

        return new MoData(fdn, profileAttributes, MoType.CONFIGURATION_PROFILE.toString(), modelData)
    }

    def static setupProfileWithoutFiles(fdn, profileName, properties, details, modelData) {
        Map<String, Object> profileWithoutFiles = [
                "name"          : profileName,
                "configurations": [],
                "properties"    : properties,
                "profileStatus" : [
                        "isValid"       : true,
                        "profileDetails": [details]
                ],
                "version"       : [
                        "productNumber" : "R3773",
                        "productRelease": "R473939"
                ],
                "dataType"      : "INTEGRATION"
        ]
        final Map<String, Object> profileAttributesWithoutFiles = new LinkedHashMap<>()
        profileAttributesWithoutFiles.putAll(new TreeMap<>(profileWithoutFiles))

        return new MoData(fdn, profileAttributesWithoutFiles, MoType.CONFIGURATION_PROFILE.toString(), modelData)
    }
}
