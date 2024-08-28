/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject.data

/**
 * Provides projectInfo file content for tests.
 */
class ProjectData {

    static String STANDARD_PROJECT_INFO =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<projectInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ProjectInfo.xsd\">\n" +
    "    <name>Project1</name>\n" +
    "    <description>Sample Project</description>\n" +
    "    <creator>AP_User</creator>\n" +
    "\n" +
    "</projectInfo>"

    static String NODE_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"NodeInfo.xsd\">" +
    "<name>Node1</name>" +
    "<nodeIdentifier>16A-R28CJ</nodeIdentifier>" +
    "<ipAddress>10.10.10.10</ipAddress>" +
    "<nodeType>RadioNode</nodeType>" +
    "<autoIntegration>" +
    "<upgradePackageName>CXP9024418/2_R9JX</upgradePackageName>" +
    "</autoIntegration>" +
    "<license>" +
    "<installLicense>true</installLicense>" +
    "<automaticLicenseRequest>LicenseRequest.xml</automaticLicenseRequest>" +
    "</license>" +
    "<artifacts>" +
    "<siteBasic>SiteBasic.xml</siteBasic>" +
    "<siteInstallation>SiteInstallation.xml</siteInstallation>" +
    "<siteEquipment>SiteEquipment.xml</siteEquipment>" +
    "</artifacts>" +
    "</nodeInfo>"

    static String REPLACE_NODE_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"HardwareReplaceNodeInfo.xsd\">" +
    "<name>HardwareReplace_RadioNode_Node1</name>" +
    "<hardwareSerialNumber>B441580584</hardwareSerialNumber>" +
    "</nodeInfo>"

    static String EXPANSION_NODE_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ExpansionNodeInfo.xsd\">" +
    "<name>Expansion_RadioNode_Node</name>" +
    "<license>" +
    "<installLicense>false</installLicense>" +
    "</license>" +
    "<autoRestoreOnFail>false</autoRestoreOnFail>" +
    "<artifacts>" +
    "<configurations>" +
    "<nodeConfiguration>radio_netconf.xml</nodeConfiguration>" +
    "</configurations>" +
    "</artifacts>" +
    "</nodeInfo>"

    static String LICENSE_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<licenseRequest>" +
    "<radioAccessTechnologies>LTE,NR</radioAccessTechnologies>" +
    "<swltId>LCS_945587_10081</swltId>" +
    "<groupId>949525</groupId>" +
    "<hardwareType>BB6648</hardwareType>" +
    "</licenseRequest>"

    static String MIGRATION_NODE_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"MigrationNodeInfo.xsd\">" +
    "<name>Migration_RadioNode_Node</name>" +
    "<autoIntegration>" +
    "<upgradePackageName>CXP9024418/2_R9JX</upgradePackageName>" +
    "</autoIntegration>" +
    "<license>" +
    "<installLicense>true</installLicense>" +
    "<automaticLicenseRequest>LicenseRequest.xml</automaticLicenseRequest>" +
    "</license>" +
    "<artifacts>" +
    "<siteBasic>SiteBasic.xml</siteBasic>" +
    "<siteInstallation>SiteInstallation.xml</siteInstallation>" +
    "<siteEquipment>SiteEquipment.xml</siteEquipment>" +
    "</artifacts>" +
    "</nodeInfo>"
}
