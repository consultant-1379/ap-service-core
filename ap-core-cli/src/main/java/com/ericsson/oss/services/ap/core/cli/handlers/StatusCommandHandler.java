/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.StatusResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Handler for status command.
 */
@Handler(name = UseCaseName.STATUS)
public class StatusCommandHandler extends AbstractCommandHandler {

    private static final String PROJECTS_FOUND_MESSAGE = "projects.found";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Inject
    private StatusResponseDtoBuilder responseDtoBuilder;

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandLine) {
        if (isViewStatusAllProjectsCommand(commandLine)) {
            return viewStatusForAllProjects(fullCommand);
        } else if (isViewStatusProjectCommand(commandLine)) {
            return viewStatusForSingleProject(fullCommand, commandLine);
        } else if (isViewStatusDeploymentCommand(commandLine)) {
            return viewStatusForSingleDeployment(fullCommand, commandLine);
        } else {
            return viewStatusForNode(fullCommand, commandLine);
        }
    }

    private CommandResponseDto viewStatusForNode(final String fullCommand, final CommandLine commandLine) {
        final String fdn = argumentResolver.resolveFdn(commandLine, CommandLogName.STATUS_NODE);
        final NodeStatus nodeStatus = getAutoProvisioningService().statusNode(fdn);
        final ResponseDto responseDto = responseDtoBuilder.buildViewNodeStatusCommandResponseDto(nodeStatus);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage("")
                .responseDto(responseDto)
                .build();
    }

    private CommandResponseDto viewStatusForSingleProject(final String fullCommand, final CommandLine commandLine) {
        final String fdn = argumentResolver.resolveFdn(commandLine, CommandLogName.STATUS_PROJECT);
        final ApNodeGroupStatus projectStatus = getAutoProvisioningService().statusProject(fdn);
        final ResponseDto responseDto = responseDtoBuilder.buildViewProjectStatusCommandResponseDto(projectStatus);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .responseDto(responseDto)
                .build();
    }

    private CommandResponseDto viewStatusForSingleDeployment(final String fullCommand, final CommandLine commandLine) {
        final String deployment = getDeployment(commandLine);
        final ApNodeGroupStatus deploymentStatus = getAutoProvisioningService().statusDeployment(deployment);

        return responseDtoBuilder.buildViewDeploymentStatusCommandResponseDto(deploymentStatus, fullCommand);
    }

    private CommandResponseDto viewStatusForAllProjects(final String fullCommand) {
        final List<ApNodeGroupStatus> projectStatuses = getAutoProvisioningService().statusAllProjects();
        final ResponseDto responseDto = responseDtoBuilder.buildViewAllProjectStatusesCommandResponseDto(projectStatuses);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(PROJECTS_FOUND_MESSAGE, projectStatuses.size()))
                .responseDto(responseDto)
                .build();
    }

    private static String getDeployment(final CommandLine commandOptions) {
        return commandOptions.getOptionValue(CliCommandOption.DEPLOYMENT.getShortForm());
    }

    private static boolean isViewStatusAllProjectsCommand(final CommandLine commandOptions) {
        return commandOptions.getOptions().length == 0;
    }

    private static boolean isViewStatusProjectCommand(final CommandLine commandOptions) {
        return isCommandOption(commandOptions, CliCommandOption.PROJECT.getShortForm());
    }

    private static boolean isViewStatusDeploymentCommand(final CommandLine commandOptions) {
        return isCommandOption(commandOptions, CliCommandOption.DEPLOYMENT.getShortForm());
    }

    private static boolean isCommandOption(final CommandLine commandOptions, final String commandOptionShortForm) {
        return commandOptions.hasOption(commandOptionShortForm);
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options viewStatusOptions = new Options();
        final OptionGroup projectOrNodeOptionGroup = new OptionGroup();
        projectOrNodeOptionGroup.setRequired(false);

        final Option projectOption = createOption(CliCommandOption.PROJECT, true);
        projectOption.setOptionalArg(false);
        projectOrNodeOptionGroup.addOption(projectOption);

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setOptionalArg(false);
        projectOrNodeOptionGroup.addOption(nodeOption);

        final Option deploymentOption = createOption(CliCommandOption.DEPLOYMENT, true);
        deploymentOption.setOptionalArg(false);
        projectOrNodeOptionGroup.addOption(deploymentOption);

        viewStatusOptions.addOptionGroup(projectOrNodeOptionGroup);
        return new CommandOptions(viewStatusOptions, true);
    }
}
