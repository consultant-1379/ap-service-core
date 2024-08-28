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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util

class NodePluginMockResponseDataProvider {

    public String jsonNotAllFileAreValidatedWithFailure = "{\n" +
            "\t\"status\": \"FAILED\",\n" +

            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"Radio.xml\",\n" +
            "\t\t\t\"result\": \"FAILED\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\t\"validationMessage\": \"some xml syntax error in line 5 \",\n" +
            "\t\t\t\t\t\"correctiveAction\": \"Please fix the error and redo again.\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"validationMessage\": \"some xml syntax error in line 15 \",\n" +
            "\t\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t\t}\n" +
            "\t\t\t]\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonNotAllFileAreValidatedWithWarning = "{\n" +
            "\t\"status\": \"WARNING\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonNotAllFileAreValidatedWithNoDetails = "{\n" +
            "\t\"status\": \"FAILED\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml\",\n" +
            "\t\t\t\"result\": \"FAILED\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonNotAllFileAreValidatedWithMultipleWarnings ="{\n" +
            "\t\"status\": \"WARNING\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t\t},\n" +
            "\t\t\t\t{\n" +
            "\t\t\t\t\t\"validationMessage\": \"Other validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t\t}\n" +
            "\t\t\t]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonAllFileAreValidatedWithSuccess = "{\n" +
            "\t\"status\": \"SUCCESS\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"Radio.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonWithUnknownStatus = "{\n" +
            "\t\"status\": \"INVALID\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml \",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"Radio.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonErrorResponse ="{\n" +
            "  \"error\": {\n" +
            "      \"description\": \"No matching Mom found for request.\",\n" +
            "      \"code\": \"404\"\n" +
            "  }\n" +
            "}"

    public String jsonPreconfigurationIsValidatedWithWarning = "{\n" +
            "\t\"status\": \"WARNING\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"preconfiguration_LTE01dg2ERBS00001.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"radio.xml\",\n" +
            "\t\t\t\"result\": \"SUCCESS\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonAllFileAreValidatedWithWarning = "{\n" +
            "\t\"status\": \"WARNING\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"preconfiguration_LTE01dg2ERBS00001.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"radio.xml\",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"
}
