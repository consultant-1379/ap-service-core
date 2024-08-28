/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.commons;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import com.ericsson.oss.services.ap.core.cli.CommandOptions;

/**
 * Extends Apache commons CLI {@link BasicParser} to provide custom behaviour for parsing of AP specific commands.
 */
public class ExtendedCommandParser extends BasicParser {

    public CommandLine parse(final CommandOptions commandOptions, final String[] arguments) throws ParseException {
        final CommandLine commandLine = super.parse(commandOptions.getOptions(), arguments);

        if (commandOptions.isFailOnLeftoverArgs() && commandLine.getArgs().length > 0) {
            throw new ParseException("Unexpected arguments in command");
        }

        return commandLine;
    }
}
