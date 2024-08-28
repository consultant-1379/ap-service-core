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
package com.ericsson.oss.services.ap.core.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Artifact;

/**
 * Commons sets of ERBS project options which can be used for generating a project.
 */
public class ErbsProjectOptions {

    private ErbsProjectOptions() {
    }

    /**
     * Map with all boolean AutoIntegration attributes set to true
     */
    private static final Map<String, Object> ALL_AI_OPTIONS = new HashMap<>();

    /**
     * Map containing attributes required for OMSEC security
     */
    private static final Map<String, Object> OMSEC_SECURITY_OPTIONS = new HashMap<>();

    /**
     * Map containing attributes required for IPSEC security
     */
    private static final Map<String, Object> IPSEC_SECURITY_OPTIONS = new HashMap<>();

    /**
     * Map containing the classpath location for each type of artifact - SiteBasic, SiteInstallation, SiteEquipment
     */
    private static final Collection<Artifact> DEFAULT_ARTIFACTS = new ArrayList<>();

    static {
        ALL_AI_OPTIONS.put("unlockCells", true);
        ALL_AI_OPTIONS.put("uploadCVAfterIntegration", true);

        OMSEC_SECURITY_OPTIONS.put("minimumSecurityLevel", "1");
        OMSEC_SECURITY_OPTIONS.put("optimumSecurityLevel", "2");
        OMSEC_SECURITY_OPTIONS.put("enrollmentMode", "SCEP");

        IPSEC_SECURITY_OPTIONS.putAll(OMSEC_SECURITY_OPTIONS);
        IPSEC_SECURITY_OPTIONS.put("ipSecLevel", "CUSOAM");
        IPSEC_SECURITY_OPTIONS.put("subjectAltNameType", "IPV4");
        IPSEC_SECURITY_OPTIONS.put("subjectAltName", "1.2.3.4");

        DEFAULT_ARTIFACTS.add(new Artifact("siteBasic", "node-artifacts/erbs/SiteBasic.xml"));
        DEFAULT_ARTIFACTS.add(new Artifact("siteEquipment", "node-artifacts/erbs/SiteEquipment.xml"));
        DEFAULT_ARTIFACTS.add(new Artifact("siteInstallation", "node-artifacts/erbs/SiteInstall.xml"));
    }

    /**
     * Gets map containing all boolean autointegration attributes set to true;
     *
     * @return auto integration attributes
     */
    public static Map<String, Object> getAllAutoIntegrationAttributes() {
        return new HashMap<>(ALL_AI_OPTIONS);
    }

    /**
     * Gets map containing all security options required for OMSEC.
     *
     * @return OMSEC security attributes
     */
    public static Map<String, Object> getOmSecSecurityAttributes() {
        return new HashMap<>(OMSEC_SECURITY_OPTIONS);

    }

    /**
     * Gets map containing all security options required for IPSEC.
     *
     * @return IPSEC security attributes
     */
    public static Map<String, Object> getIpSecSecurityAttributes() {
        return new HashMap<>(IPSEC_SECURITY_OPTIONS);

    }

    /**
     * @return the collection containing classpath locations for default set of artifacts - SiteBasic, SiteInstallation, SiteEquipment
     */
    public static Collection<Artifact> getDefaultArtifacts() {
        return new ArrayList<>(DEFAULT_ARTIFACTS);
    }

    /**
     * Gets an artifact based on its type
     *
     * @param artifactType
     *            the type of the artifact
     * @return the artifact
     */
    public static Artifact getDefaultArtifact(final String artifactType) {
        for (final Artifact artifact : DEFAULT_ARTIFACTS) {
            if (artifact.getArtifactType().equals(artifactType)) {
                return artifact;
            }
        }

        return null;
    }
}
