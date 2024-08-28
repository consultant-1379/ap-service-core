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
package com.ericsson.oss.services.ap.core.cli.handlers;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Handles requests from the CLI to bind a project or a node. This will forward batch bind requests to the {@link BatchBindCommandHandler}.
 */
@Handler(name = UseCaseName.BIND)
public class BindCommandHandler extends AbstractCommandHandler {

    private static final String BIND_SUCCESS_MESSAGE = "command.success";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Inject
    private BatchBindCommandHandler batchBindHandler;

    @Override
    public CommandResponseDto processCommand(final CliCommand command) {
        if (isBatchBind(command)) {
            return batchBindHandler.processCommand(command);
        }
        return super.processCommand(command);
    }

    private static boolean isBatchBind(final CliCommand command) {
        return command.getFullCommand().matches(".+file:.+");
    }

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandLine) {
        final String hardwareSerialNumber = getHardwareSerialNumber(commandLine);
        final String nodeFdn = argumentResolver.resolveFdn(commandLine, CommandLogName.BIND);
        getAutoProvisioningService().bind(nodeFdn, hardwareSerialNumber);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.get(BIND_SUCCESS_MESSAGE))
                .build();
    }

    private static String getHardwareSerialNumber(final CommandLine commandOptions) {
        return commandOptions.getOptionValue(CliCommandOption.HARDWARE_SERIAL_NUMBER.getShortForm());
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options bindOptions = new Options();
        bindOptions.addOptionGroup(createOptionGroup(CliCommandOption.NODE));
        bindOptions.addOptionGroup(createOptionGroup(CliCommandOption.HARDWARE_SERIAL_NUMBER));

        return new CommandOptions(bindOptions, true);
    }

    private static OptionGroup createOptionGroup(final CliCommandOption cliCommandOption) {
        final Option nodeOption = createOption(cliCommandOption, true);
        nodeOption.setOptionalArg(false);

        final OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(true);
        optionGroup.addOption(nodeOption);
        return optionGroup;
    }
}
