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
package com.ericsson.oss.services.ap.core.rest.client.shm

class ShmMockResponseDataProvider {

    String jsonidealResponse = "{\n" +
            "\t\"supportedPackageDetails\":[{" +
            "\t\"neType\": \"RadioNode\", " +
            "\t\"softwarePackageDetails\": [{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B17\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B17\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B171\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B171\"\n" +
            "\t\t\t}]\n" +
            "}]}]}";

    public String jsonResponseSamePackage = "{\n" +
            "\t\"supportedPackageDetails\":[{" +
            "\t\"neType\": \"RadioNode\", " +
            "\t\"softwarePackageDetails\": [{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B17\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B17\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B17\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B17\"\n" +
            "\t\t\t}]\n" +
            "}]}]}";

    public String jsonResponsePackageNotfound = "{\n" +
            "\t\"supportedPackageDetails\":[]\n" +
            "}";

    public String jsonResponsePackageNotMatch = "{\n" +
            "\t\"supportedPackageDetails\":[{" +
            "\t\"neType\": \"RadioNode\", " +
            "\t\"softwarePackageDetails\": [{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B172\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B172\"\n" +
            "\t\t\t}]\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"packageName\": \"CXP9024418_6_R5B171\",\n" +
            "\t\t\t\"productDetails\": [{\n" +
            "\t\t\t\t\"productName\": \"BASEBAND\",\n" +
            "\t\t\t\t\"productNumber\": \"CXP9024418_6\",\n" +
            "\t\t\t\t\"productRevision\": \"R5B171\"\n" +
            "\t\t\t}]\n" +
            "}]}]}";

    public String jsonResponsePackageerror = "invalid JSON body";

    public String jsonResponseBackupSoftwareVersionPackage = "{\n" +
            "\t\"backupSwVersionDetails\":[{" +
            "\t\t\"nodeFdn\": \"NetworkElement=LTE01dg2ERBS00002\", \n" +
            "\t\t\"backupId\": \"RadioNode_R5B17_release_upgrade_package_22032022_023213_READY_FOR_SERVICE\", \n" +
            "\t\t\"location\": \"ENM\", \n" +
            "\t\t\"swVersion\": \"CXP9024418_6_R5B17\"\n" +
            "\t}]\n" +
            "}";

    public String jsonResponseBackupSoftwareVersionNotFoundPackage = "{\n" +
            "\t\"backupSwVersionDetails\":[]" +
            "}";

    public String jsonResponseBackupSoftwareVersionInvalidPackage = "{\n" +
            "\t\"backupSwVersionDetails\":[{" +
            "\t\t\"nodeFdn\": \"NetworkElement=LTE01dg2ERBS00002\", \n" +
            "\t\t\"backupId\": \"RadioNode_R5B17_release_upgrade_package_22032022_023213_READY_FOR_SERVICE\", \n" +
            "\t\t\"location\": \"ENM\", \n" +
            "\t\t\"swVersion\": \"Unable to retreive Software Version details\"\n" +
            "\t}]\n" +
            "}";

   public String jsonResponseBackupSoftwareVersionerror = "invalid JSON body";

   public String jsonResponseMultipleBackupSoftwareVersionPackage = "{\n" +
           "\t\"backupSwVersionDetails\":[{" +
           "\t\t\"nodeFdn\": \"NetworkElement=LTE01dg2ERBS00002\", \n" +
           "\t\t\"backupId\": \"RadioNode_R5B17_release_upgrade_package_22032022_023213_READY_FOR_SERVICE\", \n" +
           "\t\t\"location\": \"ENM\", \n" +
           "\t\t\"swVersion\": \"CXP9024418_6_R5B17\"\n" +
           "\t},\n" +
           "\t{\n" +
           "\t\t\"nodeFdn\": \"NetworkElement=LTE01dg2ERBS00002\", \n" +
           "\t\t\"backupId\": \"RadioNode_R5B17_release_upgrade_package_22032022_023213_READY_FOR_SERVICE\", \n" +
           "\t\t\"location\": \"ENM\", \n" +
           "\t\t\"swVersion\": \"CXP9024418_6_R5B18\"\n" +
           "\t}]\n" +
           "}";
}
