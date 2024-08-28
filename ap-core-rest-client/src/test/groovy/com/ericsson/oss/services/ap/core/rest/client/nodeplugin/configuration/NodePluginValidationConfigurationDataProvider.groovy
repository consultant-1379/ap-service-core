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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration

class NodePluginValidationConfigurationDataProvider {

    public String jsonNotAllFileAreValidated = "{\n" +
            "\t\"status\": \"FAILED\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml \",\n" +
            "\t\t\t\"result\": \"WARNING\",\n" +
            "\t\t\t\"validationDetails\": [{\n" +
            "\t\t\t\t\"validationMessage\": \"Some validation warning here \",\n" +
            "\t\t\t\t\"correctiveAction\": \"\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml \",\n" +
            "\t\t\t\"result\": \"SUCCESS\",\n" +
            "\t\t\t\"validationDetails\": []\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"Radio.xml \",\n" +
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

    public String jsonAllFileAreValidated = "{\n" +
            "\t\"status\": \"SUCCESS\",\n" +
            "\t\"message\": \"\",\n" +
            "\t\"validationResults\": [{\n" +
            "\t\t\t\"configurationName\": \"siteEquipment.xml \",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"siteBasic.xml \",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"configurationName\": \"Radio.xml \",\n" +
            "\t\t\t\"result\": \"SUCCESS\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}"

    public String jsonCapabililties = "[\n"+
            "\t{\n" +
            "\t\t\"versionOfInterface\":\"v0\",\n"+
            "\t\t\"capabilities\":[\n"+
            "\t\t\t\"VALIDATE\"\n"+
            "\t\t]\n"+
            "\t},\n"+
            "\t{\n"+
            "\t\t\"versionOfInterface\":\"v1\",\n"+
            "\t\t\"capabilities\":[\n"+
            "\t\t\t\"VALIDATEDELTA\",\n"+
            "\t\t\t\"VALIDATE\",\n"+
            "\t\t\t\"EDIT\",\n"+
            "\t\t\t\"GENERATE\"\n"+
            "\t\t],\n"+
            "\t\t\"applicationUri\":\"configuration-generator\"\n"+
            "\t}\n"+
            "]";
}
