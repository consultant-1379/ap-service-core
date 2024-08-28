/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.data;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Map;

import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.AutoIntegrationOptions;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeArtifact;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeConfig;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeUserCredentials;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.SecurityConfiguration;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.Notification;
import com.google.common.collect.ImmutableMap;

/**
 * Provides data for the import tests.
 */
public class ImportTestData {

    public static final Map<String, Object> NODE_DATA = ImmutableMap.<String, Object> builder()

        .put("nodeUserCredentials", new NodeUserCredentials(
            ImmutableMap.<String, String> builder()
                .put("secureUserName", "user1")
                .put("securePassword", "pass1")
                .build()))
        /**********
         * ERBS data
         ***********/

        .put("erbsNodeIdentifier", "6607-651-025")
        .put("erbsSecurityConfig", new SecurityConfiguration(
            ImmutableMap.<String, String> builder()
                .put("minimumSecurityLevel", "1")
                .put("optimumSecurityLevel", "2")
                .put("enrollmentMode", "SCEP")
                .put("ipSecLevel", "CUS")
                .put("subjectAltNameType", "IPV4")
                .put("subjectAltName", "1.2.3.4")
                .build()))
        .put("erbsAutoIntegrationOptions", new AutoIntegrationOptions(
            ImmutableMap.<String, String> builder()
                .put("upgradePackageName", "erbsUpgradePackage")
                .put("basicPackageName", "erbsBasicPackage")
                .put("unlockCells", "true")
                .put("uploadCVAfterIntegration", "false")
                .build()))
        .put("erbsArtifacts", newArrayList(
            new NodeArtifact("siteInstallation", "siteInstall.xml"),
            new NodeArtifact("siteEquipment", "siteEquipment.xml"),
            new NodeArtifact("siteBasic", "siteBasic.xml")))
        .put("erbsConfigurations", newArrayList(
            new NodeConfig("radio.xml"),
            new NodeConfig("transport.xml")))
        .put("erbsConfigurationsMinimal", newArrayList(
            new NodeConfig("radio.xml")))

        /**********
         * ECIM data
         ***********/
        .put("ecimNodeIdentifier", "18.Q4-R57A02")
        .put("ecimSecurityConfig", new SecurityConfiguration(
            ImmutableMap.<String, String> builder()
                .put("ipSecLevel", "OAM")
                .put("subjectAltNameType", "IPV4")
                .put("subjectAltName", "1.2.3.4")
                .build()))
        .put("ecimAutoIntegrationOptions", new AutoIntegrationOptions(
            ImmutableMap.<String, String> builder()
                .put("upgradePackageName", "erbsUpgradePackage")
                .build()))

        .put("ecimArtifacts", newArrayList(
            new NodeArtifact("siteInstallation", "siteInstall.xml"),
            new NodeArtifact("siteEquipment", "siteEquipment.xml"),
            new NodeArtifact("siteBasic", "siteBasic.xml")))

        .put("ecimConfigurations", newArrayList(
            new NodeConfig("nodeConfiguration", "radio.xml"),
            new NodeConfig("nodeConfiguration", "transport.xml"),
            new NodeConfig("optionalFeature", "optionalFeature.xml"),
            new NodeConfig("baseline", "baseline.mos"),
            new NodeConfig("unlockCell", "unlockCell.xml")))

        .put("rnConfigurations", newArrayList(
            new NodeConfig("optionalFeature", "optionalFeature.xml")))

        .put("ecimConfigurationsMinimal", newArrayList(
            new NodeConfig("optionalFeature", "optionalFeature.xml")))

        .put("ecimNotification", new Notification(
                ImmutableMap.<String, String> builder()
                    .put("email", "test@ericsson.com")
                    .build()))

        .build();

    public static final Map<String, Map<String, String>> NODE_DEFAULT_DATA = ImmutableMap.<String, Map<String, String>> builder()

        /*******************
         * ERBS default data
         ******************/

        .put("erbs", ImmutableMap.<String, String> builder()
            .put("securityConfiguration", "erbsSecurityConfig")
            .put("autoIntegrationOptions", "erbsAutoIntegrationOptions")
            .put("nodeUserCredentials", "nodeUserCredentials")
            .put("artifacts", "erbsArtifacts")
            .put("configurations", "erbsConfigurations")
            .build())

        /*******************
         * ECIM default data
         ******************/

        .put("RadioNode", ImmutableMap.<String, String> builder()
            .put("securityConfiguration", "ecimSecurityConfig")
            .put("autoIntegrationOptions", "ecimAutoIntegrationOptions")
            .put("nodeUserCredentials", "nodeUserCredentials")
            .put("artifacts", "ecimArtifacts")
            .put("configurations", "ecimConfigurations")
            .put("notification", "ecimNotification")
            .build())
        .build();

    public static final Map<String, Map<String, String>> NODE_DEFAULT_DATA_MINIMAL = ImmutableMap.<String, Map<String, String>> builder()

        /*******************
         * ERBS default data
         ******************/

        .put("erbs", ImmutableMap.<String, String> builder()
            .put("securityConfiguration", "erbsSecurityConfig")
            .put("autoIntegrationOptions", "erbsAutoIntegrationOptions")
            .put("nodeUserCredentials", "nodeUserCredentials")
            .put("artifacts", "erbsArtifacts")
            .put("configurations", "erbsConfigurationsMinimal")
            .build())

        /*******************
         * ECIM default data
         ******************/

        .put("RadioNode", ImmutableMap.<String, String> builder()
            .put("securityConfiguration", "ecimSecurityConfig")
            .put("autoIntegrationOptions", "ecimAutoIntegrationOptions")
            .put("nodeUserCredentials", "nodeUserCredentials")
            .put("artifacts", "ecimArtifacts")
            .put("configurations", "ecimConfigurationsMinimal")
            .put("notification", "ecimNotification")
            .build())
        .build();
}
