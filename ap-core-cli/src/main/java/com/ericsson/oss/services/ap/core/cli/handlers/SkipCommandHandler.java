/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Skip handler for skip importing the failed artifact request from CLI.
 */
@Handler(name = UseCaseName.SKIP)
public class SkipCommandHandler extends AbstractCommandHandler {

    private static final String SKIP_SUCCESS_MESSAGE = "node.command.successful";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandOptions) {
        final String nodeFdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.SKIP);
        getAutoProvisioningService().skip(nodeFdn);

        final String nodeName = commandOptions.getOptionValue(CliCommandOption.NODE.getShortForm());
        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(SKIP_SUCCESS_MESSAGE, nodeName))
                .build();
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options skipOptions = new Options();

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setRequired(true);
        skipOptions.addOption(nodeOption);

        return new CommandOptions(skipOptions, true);
    }
}
