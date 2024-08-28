/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import java.util.Arrays;

import com.ericsson.oss.services.ap.core.cli.handlers.CliCommandOption;

/**
 * Utility methods to read the command arguments.
 */
final class CommandArgs {

    private CommandArgs() {

    }

    /**
     * Reads the project or node name from the command identified by '-p' and '-n' respectively.
     *
     * @param command
     *            the command to check
     * @return the project or node name, or a blank string if not a project/node command
     */
    public static String getProjectOrNodeName(final String command) {
        if (!isProjectCommand(command) && !isNodeCommand(command)) {
            return "";
        }

        final String[] commandArgs = command.split("\\s");
        final String projectOrNodearg = isProjectCommand(command) ? CliCommandOption.PROJECT.getShortFlag() : CliCommandOption.NODE.getShortFlag();
        final int projectOrNodeIndex = Arrays.asList(commandArgs).indexOf(projectOrNodearg);
        return commandArgs[projectOrNodeIndex + 1];
    }

    /**
     * Reads the target of the command, project or node.
     *
     * @return project if '-p' otherwise node
     */
    public static String getCommandTarget(final String command) {
        return isProjectCommand(command) ? "project" : "node";
    }

    /**
     * Reads the filename in the command
     *
     * @param command
     *            the command to check
     * @return the filename or blank string if command has no 'file:' prefix
     */
    public static String getFilename(final String command) {
        for (final String arg : command.split("\\s")) {
            if (arg.startsWith("file:")) {
                final String[] argComponents = arg.split(":");
                if (argComponents.length > 1) {
                    return argComponents[1];
                }
            }
        }
        return "";
    }

    /**
     * Reads the hardware serial number in the command.
     *
     * @param command
     *            the command to check
     * @return the hardware serial number or blank string is command has no '-s' flag
     */
    public static String getHarwareSerialNumber(final String command) {
        if (!command.contains(CliCommandOption.HARDWARE_SERIAL_NUMBER.getShortFlag())) {
            return "";
        }

        final String[] commandArgs = command.split("\\s");
        final int hwIdFlagIndex = Arrays.asList(commandArgs).indexOf(CliCommandOption.HARDWARE_SERIAL_NUMBER.getShortFlag());
        return commandArgs[hwIdFlagIndex + 1];
    }

    private static boolean isProjectCommand(final String command) {
        return command.contains(CliCommandOption.PROJECT.getShortFlag());
    }

    private static boolean isNodeCommand(final String command) {
        return command.contains(CliCommandOption.NODE.getShortFlag());
    }
}
