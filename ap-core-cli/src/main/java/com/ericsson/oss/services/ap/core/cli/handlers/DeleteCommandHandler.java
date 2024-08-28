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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
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
 * Handles requests from the CLI to delete a project or a node.
 */
@Handler(name = UseCaseName.DELETE)
public class DeleteCommandHandler extends AbstractCommandHandler {

    private static final String DELETE_SUCCESS_MESSAGE = "command.success";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandLine) {
        delete(commandLine);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.get(DELETE_SUCCESS_MESSAGE))
                .build();
    }

    private void delete(final CommandLine commandOptions) {
        if (isProjectCommand(commandOptions)) {
            final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.DELETE_PROJECT);
            getAutoProvisioningService().deleteProject(fdn, isIgnoreOptionEnabled(commandOptions));
        } else {
            final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.DELETE_NODE);
            getAutoProvisioningService().deleteNode(fdn, isIgnoreOptionEnabled(commandOptions));
        }
    }

    private static boolean isProjectCommand(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.PROJECT.getShortForm());
    }

    private static boolean isIgnoreOptionEnabled(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.IGNORE_NETWORK_ELEMENT.getShortForm());
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options deleteOptions = new Options();

        final Option ignoreOption = createOption(CliCommandOption.IGNORE_NETWORK_ELEMENT, false);
        ignoreOption.setRequired(false);
        deleteOptions.addOption(ignoreOption);

        final OptionGroup deleteOptionGroup = new OptionGroup();
        deleteOptionGroup.setRequired(true);

        final Option projectOption = createOption(CliCommandOption.PROJECT, true);
        projectOption.setOptionalArg(false);
        deleteOptionGroup.addOption(projectOption);

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setOptionalArg(false);
        deleteOptionGroup.addOption(nodeOption);

        deleteOptions.addOptionGroup(deleteOptionGroup);

        return new CommandOptions(deleteOptions, true);
    }
}
