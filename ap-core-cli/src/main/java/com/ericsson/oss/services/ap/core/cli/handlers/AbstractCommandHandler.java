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
package com.ericsson.oss.services.ap.core.cli.handlers;

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CliCommandHandler;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.commons.ExtendedCommandParser;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Abstract implementation of a {@link CliCommandHandler}.
 */
public abstract class AbstractCommandHandler implements CliCommandHandler {

    private static final String ERROR_INVALID_SYNTAX = "invalid.command.syntax";
    private static final String SOLUTION_INVALID_SYNTAX = "invalid.command.syntax.solution";
    private static final String AP_CORE = "apcore";

    private final ApMessages apMessages = new ApMessages();

    protected AutoProvisioningService autoProvisioningCore;

    @Inject
    private ExceptionMapperFactory exceptionMapperFactory;

    @Override
    public CommandResponseDto processCommand(final CliCommand command) {
        try {
            return executeCommand(command);
        } catch (final ParseException e) {
            return getInvalidCommandSyntaxResponse(command);
        } catch (final Exception e) {
            return exceptionMapperFactory.find(e).toCommandResponse(command.getFullCommand(), e);
        }
    }

    private CommandResponseDto executeCommand(final CliCommand command) throws ParseException {
        final Map<String, Object> commandProperties = command.getProperties();
        final CommandLine commandOptions = validateCommand(command);
        return executeCommand(command.getFullCommand(), commandProperties, commandOptions);
    }

    private CommandResponseDto getInvalidCommandSyntaxResponse(final CliCommand command) {
        return new CommandResponseDtoBuilder()
                .fullCommand(command.getFullCommand())
                .statusCode(ResponseStatus.COMMAND_SYNTAX_ERROR)
                .errorCode(CliErrorCodes.INVALID_COMMAND_SYNTAX_ERROR_CODE)
                .statusMessage(apMessages.get(ERROR_INVALID_SYNTAX))
                .solution(apMessages.format(SOLUTION_INVALID_SYNTAX, command.getOperation()))
                .build();
    }

    /**
     * Creates an {@link Option} with the command flags defined by the supplied {@link CliCommandOption}.
     *
     * @param cliCommandOption
     *            the input {@link CliCommandOption}
     * @param hasArgument
     *            whether the option has an argument
     * @return the created {@link Option}
     */
    protected static Option createOption(final CliCommandOption cliCommandOption, final boolean hasArgument) {
        return new Option(cliCommandOption.getShortForm(), hasArgument, cliCommandOption.getDescription());
    }

    /**
     * Validate the command syntax.
     *
     * @param cliCommand
     *            the CLI command
     * @return the parsed command line if valid
     * @throws ParseException
     *             if command arguments are not valid
     */
    protected CommandLine validateCommand(final CliCommand cliCommand) throws ParseException {
        final ExtendedCommandParser parser = new ExtendedCommandParser();
        final CommandOptions options = getCommandOptions();
        return parser.parse(options, cliCommand.getParameters());
    }

    /**
     * Perform the command execution. Will only be called once the command options have been successfully validated.
     *
     * @param fullCommand
     *            the full command string
     * @param commandProperties
     *            a map of command properties
     * @param commandOptions
     *            the parsed command line options
     * @return the command response
     */
    protected abstract CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandOptions);

    /**
     * Gets the command options.
     *
     * @return the command options
     */
    protected abstract CommandOptions getCommandOptions();

    public AutoProvisioningService getAutoProvisioningService() {
        if (autoProvisioningCore == null) {
            final ServiceFinderBean serviceFinder = new ServiceFinderBean();
            autoProvisioningCore = serviceFinder.find(AutoProvisioningService.class, AP_CORE);
        }
        return autoProvisioningCore;
    }
}
