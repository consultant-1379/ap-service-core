/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

/**
 * ENUM containing the CM CLI options/flags for AP commands.
 * <p>
 * Contains the long and short form for each flag, and a description.
 */
public enum CliCommandOption {

    DEPLOYMENT("d", "Name of the Deployment"),
    HARDWARE_SERIAL_NUMBER("s", "Hardware serial number of the node to integrate"),
    IGNORE_NETWORK_ELEMENT("i", "Whether to ignore the NetworkElement when deleting AP-related MOs"),
    INITIAL_ARTIFACT("i", "Download request for raw artifacts (supplied by the operator in the AP project)"),
    NO_VALIDATION("nv", "Whether to execute validation of the AP project being ordered"),
    NODE("n", "Name of the AP node"),
    ORDERED_ARTIFACT("o", "Download request for generated artifacts (artifacts generated during the AP order phase)"),
    PROJECT("p", "Name of the AP project"),
    SAMPLES_AND_SCHEMAS("x", "Download request for sample AP projects and schema files"),
    CIQ_GENERATION("ciq", "Download generated CIQ file containing value headers only"),
    BACKUP_NAME("b", "Name of the backup file to use"),
    DEFAULT_ROUTER("r", "The default router IP address for DHCP configuration");

    private String shortForm;
    private String description;
    private String shortFlag;

    CliCommandOption(final String shortForm, final String description) {
        this.shortForm = shortForm;
        this.description = description;
        shortFlag = "-" + shortForm;
    }

    /**
     * Gets the short form of the option.
     * <p>
     * <b>n</b> for the <code>node</code> option.
     *
     * @return option's short form
     */
    public String getShortForm() {
        return shortForm;
    }

    /**
     * Gets the description of the option.
     *
     * @return option's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the short form flag of the option.
     * <p>
     * <b>-n</b> for the <code>node</code> option.
     *
     * @return option's short form flag
     */
    public String getShortFlag() {
        return shortFlag;
    }
}
