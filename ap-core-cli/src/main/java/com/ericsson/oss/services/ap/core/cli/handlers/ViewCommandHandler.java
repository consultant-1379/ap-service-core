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
package com.ericsson.oss.services.ap.core.cli.handlers;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.ViewResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * This class is responsible to handle the all the requests for "view" operation. It handles the following commands:
 * <ul>
 * <li>ap view
 * <li>ap view -p {@literal <}projectName{@literal >}
 * <li>ap view -n {@literal <}nodeName{@literal >}
 * </ul>
 */
@Handler(name = UseCaseName.VIEW)
public class ViewCommandHandler extends AbstractCommandHandler {

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Inject
    private ViewResponseDtoBuilder viewResponseDtoBuilder;

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandOptions) {
        if (isViewAllProjects(commandOptions)) {
            return viewAllProjects(fullCommand);
        } else if (isViewSingleProject(commandOptions)) {
            return viewSingleProject(fullCommand, commandOptions);
        } else {
            return viewNode(fullCommand, commandOptions);
        }
    }

    private CommandResponseDto viewAllProjects(final String fullCommand) {
        final List<MoData> projectsData = getAutoProvisioningService().viewAllProjects();
        final ResponseDto responseDto = viewResponseDtoBuilder.buildViewForAllProjects(projectsData);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format("projects.found", projectsData.size()))
                .responseDto(responseDto)
                .build();
    }

    private CommandResponseDto viewSingleProject(final String fullCommand, final CommandLine commandOptions) {
        final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW_PROJECT);
        final List<MoData> projectData = getAutoProvisioningService().viewProject(fdn);
        final ResponseDto responseDto = viewResponseDtoBuilder.buildViewForProject(projectData);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage("")
                .responseDto(responseDto)
                .build();
    }

    private CommandResponseDto viewNode(final String fullCommand, final CommandLine commandOptions) {
        final String fdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.VIEW_NODE);
        final List<MoData> nodeData = getAutoProvisioningService().viewNode(fdn);
        final ResponseDto responseDto = viewResponseDtoBuilder.buildViewForNode(nodeData);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage("")
                .responseDto(responseDto)
                .build();
    }

    private static boolean isViewAllProjects(final CommandLine commandOptions) {
        return commandOptions.getOptions().length == 0;
    }

    private static boolean isViewSingleProject(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.PROJECT.getShortForm());
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options viewOptions = new Options();
        final OptionGroup viewOptionGroup = new OptionGroup();
        viewOptionGroup.setRequired(false);

        final Option projectOption = createOption(CliCommandOption.PROJECT, true);
        projectOption.setOptionalArg(false);
        projectOption.setValueSeparator(' ');
        viewOptionGroup.addOption(projectOption);

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setOptionalArg(false);
        nodeOption.setValueSeparator(' ');
        viewOptionGroup.addOption(nodeOption);

        viewOptions.addOptionGroup(viewOptionGroup);
        return new CommandOptions(viewOptions, true);
    }
}
