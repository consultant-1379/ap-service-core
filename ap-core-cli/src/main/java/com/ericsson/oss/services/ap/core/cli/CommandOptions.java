/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli;

import org.apache.commons.cli.Options;

/**
 * The command options. Provide additional properties to apache commons cli <code>Options</code> for parsing of AP commands.
 */
public class CommandOptions {

    private final Options options;
    private final boolean failOnLeftoverArgs;

    /**
     * Command options
     *
     * @param options
     *            command options
     * @param failOnLeftoverArgs
     *            true if command parsing should fail if there are unspecified arguments
     */
    public CommandOptions(final Options options, final boolean failOnLeftoverArgs) {
        this.options = options;
        this.failOnLeftoverArgs = failOnLeftoverArgs;
    }

    public Options getOptions() {
        return options;
    }

    public boolean isFailOnLeftoverArgs() {
        return failOnLeftoverArgs;
    }
}
