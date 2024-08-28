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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The CLI command data containing the specified command parameters and properties.
 */
public class CliCommand {

    private final String fullCommand;
    private final String operation;
    private final String[] parameters;
    private final Map<String, Object> properties;

    public CliCommand(final String fullCommand, final Map<String, Object> commandProperties) {
        this.fullCommand = fullCommand.trim().replaceAll("\\s+", " ");
        final String[] commandArgs = getCommandArgsArray(fullCommand);
        operation = commandArgs.length > 1 ? commandArgs[1] : "";
        parameters = commandArgs.length > 2 ? Arrays.copyOfRange(commandArgs, 2, commandArgs.length) : new String[0];
        properties = commandProperties == null ? Collections.<String, Object> emptyMap() : commandProperties;
    }

    private String[] getCommandArgsArray(final String fullCommand) {
        final List<String> list = new ArrayList<>();
        final Matcher commandArgsMatcher = Pattern.compile("(?<commandArg>[^\"]\\S*|\".+?\")\\s*").matcher(fullCommand);
        while (commandArgsMatcher.find()) {
            list.add(commandArgsMatcher.group("commandArg").replace("\"", ""));
        }
        return list.toArray(new String[list.size()]);
    }

    public String getOperation() {
        return operation;
    }

    public String[] getParameters() {
        return parameters;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getFullCommand() {
        return fullCommand;
    }

    @Override
    public String toString() {
        return fullCommand;
    }
}
