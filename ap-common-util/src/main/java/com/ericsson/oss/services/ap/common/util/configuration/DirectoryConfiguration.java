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
package com.ericsson.oss.services.ap.common.util.configuration;

/**
 * Exposes locations of Auto Provisioning directories on the ENM filesystem.
 */
public final class DirectoryConfiguration {

    private static final String ROOT_AP_SFS_DIRECTORY = "/ericsson/autoprovisioning/";

    private static final String ARTIFACTS_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts";
    private static final String BIND_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/generated/bind";
    private static final String DOWNLOAD_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/download";
    private static final String GENERATED_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/generated";
    private static final String PROFILE_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "projects";
    private static final String RAW_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/raw";
    private static final String TEMPORARY_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/temp";

    private static final String RESTORE_DIRECTORY = "/ericsson/tor/data/enmbur";

    private static final String CLI_METADATA_DIRECTORY = "/opt/ericsson/autoprovisioning/metadata/cli";
    private static final String SAMPLES_DIRECTORY = "/opt/ericsson/autoprovisioning/artifacts/schemadata/samples";
    private static final String SCHEMAS_DIRECTORY = "/opt/ericsson/autoprovisioning/artifacts/schemadata/schemas";

    private DirectoryConfiguration() {

    }

    /**
     * Gets the absolute path to the base directory for all Auto Provisioning artifacts.
     *
     * @return the artifacts directory
     */
    public static String getArtifactsDirectory() {
        return ARTIFACTS_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing bound files created during bind.
     *
     * @return bind directory
     */
    public static String getBindDirectory() {
        return BIND_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing CLI metadata.
     *
     * @return CLI metadata directory
     */
    public static String getCliMetaDataDirectory() {
        return CLI_METADATA_DIRECTORY;
    }

    /**
     * Gets the absolute path to the download staging directory.
     *
     * @return download directory
     */
    public static String getDownloadDirectory() {
        return DOWNLOAD_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing generated artifacts for all nodes.
     *
     * @return generated artifacts directory
     */
    public static String getGeneratedDirectory() {
        return GENERATED_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing raw artifacts for all nodes.
     *
     * @return raw artifacts directory
     */
    public static String getRawDirectory() {
        return RAW_DIRECTORY;
    }

    /**
     * Gets the absolute path to the ENM "Backup And Restore" directory.
     * <p>
     * Will contain the <code>enmrestoredata.txt</code> if an ENM restore is ongoing.
     *
     * @return restore directory
     */
    public static String getRestoreDirectory() {
        return RESTORE_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing schemas for all node types.
     *
     * @return schemas directory
     */
    public static String getSchemasDirectory() {
        return SCHEMAS_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing samples for all node types.
     *
     * @return samples directory
     */
    public static String getSamplesDirectory() {
        return SAMPLES_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing temporary artifacts for all node types.
     *
     * @return temporary directory
     */
    public static String getTemporaryDirectory() {
        return TEMPORARY_DIRECTORY;
    }

    /**
     * Gets the absolute path to the directory containing all profile configurations.
     *
     * @return profile directory
     */
    public static String getProfileDirectory() {
        return PROFILE_DIRECTORY;
    }
}
