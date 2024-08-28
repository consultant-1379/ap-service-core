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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Artifact;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.google.common.collect.ImmutableMap;

public class ProjectArtifactData {


    public static final Map<String, List<Artifact>> PROJECT_DATA = ImmutableMap.<String, List<Artifact>> builder()

            /**********
             * ERBS data
             ***********/

            .put("ERBS", Arrays.asList(
                    new Artifact("siteBasic", "node-artifacts/erbs/SiteBasic.xml"),
                    new Artifact("siteEquipment", "node-artifacts/erbs/SiteEquipment.xml"),
                    new Artifact("siteInstallation", "node-artifacts/erbs/SiteInstall.xml"),
                    new Artifact("configuration", "node-artifacts/erbs/radio.xml", "BULK_3GPP", null),
                    new Artifact("configuration", "node-artifacts/erbs/transport.xml", "BULK_3GPP", null)))

            /**********
             * ECIM data
             ***********/

            .put("RadioNode", Arrays.asList(
                    new Artifact("siteBasic", "node-artifacts/ecim/SiteBasic.xml"),
                    new Artifact("siteEquipment", "node-artifacts/ecim/SiteEquipment.xml"),
                    new Artifact("siteInstallation", "node-artifacts/ecim/SiteInstall.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/ecim/transport.xml", "BULK_3GPP", null),
                    new Artifact("nodeConfiguration", "node-artifacts/ecim/radio.xml", "BULK_3GPP", null),
                    new Artifact("optionalFeature", "node-artifacts/ecim/optionalFeature.xml", "BULK_3GPP", null),
                    new Artifact("unlockCell", "node-artifacts/ecim/unlockCell.xml", "BULK_3GPP", null)))

            /**********
             * Msrbs_v1 data
             ***********/

            .put("MSRBS_V1", Arrays.asList(
                    new Artifact("siteInstallation", "node-artifacts/msrbs_v1/SiteInstall.xml"),
                    new Artifact("icf", "node-artifacts/msrbs_v1/icf.xml"),
                    new Artifact("ccf", "node-artifacts/msrbs_v1/ccf.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/msrbs_v1/Configuration-file.xml")))

            /**********
             * vPP data
             ***********/

            .put("vPP", Arrays.asList(
                    new Artifact("siteBasic", "node-artifacts/vnf/sitebasic.xml"),
                    new Artifact("virtualNetworkDescriptor", "node-artifacts/vnf/VirtualNetworkMappings.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/vnf/radio.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/vnf/transport.xml")))

            /**********
             * vSD data
             ***********/

            .put("vSD", Arrays.asList(
                    new Artifact("siteBasic", "node-artifacts/vnf/sitebasic.xml"),
                    new Artifact("virtualNetworkDescriptor", "node-artifacts/vnf/VirtualNetworkMappings.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/vnf/radio.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/vnf/transport.xml")))

            .build();

    private static final Map<String, List<Artifact>> NETCONF_PROJECT_DATA = ImmutableMap.<String, List<Artifact>> builder()

            /**********
             * ECIM data
             ***********/

            .put("RadioNode", Arrays.asList(
                    new Artifact("siteBasic", "node-artifacts/ecim/netconf/SiteBasic.xml", "NETCONF", null),
                    new Artifact("siteEquipment", "node-artifacts/ecim/netconf/SiteEquipment.xml", "NETCONF", null),
                    new Artifact("siteInstallation", "node-artifacts/ecim/netconf/SiteInstall.xml"),
                    new Artifact("nodeConfiguration", "node-artifacts/ecim/netconf/transport.xml", "NETCONF", null),
                    new Artifact("nodeConfiguration", "node-artifacts/ecim/netconf/radio.xml", "NETCONF", null),
                    new Artifact("optionalFeature", "node-artifacts/ecim/netconf/optionalFeature.xml", "NETCONF", null),
                    new Artifact("unlockCell", "node-artifacts/ecim/netconf/unlockCell.xml", "NETCONF", null)))

            .build();

    public static final Map<String, Map<String, List<Artifact>>> PROJECT_DATA_WITH_FORMAT = ImmutableMap.<String, Map<String, List<Artifact>>> builder()
            .put(ArtifactFileFormat.BULK_3GPP.toString(), PROJECT_DATA)
            .put(ArtifactFileFormat.NETCONF.toString(), NETCONF_PROJECT_DATA)
            .build();
}
