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

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.UnsupportedProjectSizeException;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.commons.ExtendedCommandParser;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Order handler for order request from CLI.
 */
@Handler(name = UseCaseName.ORDER)
public class OrderCommandHandler extends AbstractCommandHandler {

    private static final String ORDER_NODE_SUCCESS_MESSAGE = "node.command.successful";
    private static final String ORDER_PROJECT_SUCCESS_MESSAGE = "project.command.successful";
    private static final String FILE_PATH_IDENTIFIER = "filePath";
    private static final long MAXIMUM_PROJECT_FILE_SIZE_IN_BYTES = 5242880;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Override
    protected CommandLine validateCommand(final CliCommand command) throws ParseException {
        final ExtendedCommandParser parser = new ExtendedCommandParser();
        final String fullCommand = command.getFullCommand();
        final boolean isProjectArchiveCommand = isOrderProjectArchiveCommand(fullCommand);

        if (isProjectArchiveCommand && isValidationRequired(fullCommand)) {
            return parser.parse(new CommandOptions(new Options(), false), command.getParameters());
        } else if (isProjectArchiveCommand) {
            return parser.parse(getValidationOption(), command.getParameters());
        } else {
            return parser.parse(getProjectOrNodeOptions(), command.getParameters());
        }
    }

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandLine) {
        if (isOrderProjectArchiveCommand(fullCommand)) {
            return orderProjectArchive(fullCommand, commandProperties);
        } else if (isOrderProjectCommand(commandLine)) {
            return orderProject(fullCommand, commandLine);
        } else {
            return orderNode(fullCommand, commandLine);
        }
    }

    private CommandResponseDto orderNode(final String fullCommand, final CommandLine commandLine) {
        final String nodeFdn = argumentResolver.resolveFdn(commandLine, CommandLogName.ORDER_NODE);
        getAutoProvisioningService().orderNode(nodeFdn);
        final String nodeName = getMoName(commandLine);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(ORDER_NODE_SUCCESS_MESSAGE, nodeName))
                .build();
    }

    private CommandResponseDto orderProject(final String fullCommand, final CommandLine commandLine) {
        final String projectFdn = argumentResolver.resolveFdn(commandLine, CommandLogName.ORDER_PROJECT);
        getAutoProvisioningService().orderProject(projectFdn);
        final String projectName = getMoName(commandLine);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(ORDER_PROJECT_SUCCESS_MESSAGE, projectName))
                .build();
    }

    private CommandResponseDto orderProjectArchive(final String fullCommand, final Map<String, Object> commandProperties) {
        validateProjectSize(commandProperties);
        final String fileName = argumentResolver.getFileName(commandProperties);
        final byte[] fileContent = argumentResolver.getFileContent(commandProperties);
        final String projectFdn = getAutoProvisioningService().orderProject(fileName, fileContent, isValidationRequired(fullCommand));
        final String projectName = FDN.get(projectFdn).getRdnValue();

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(ORDER_PROJECT_SUCCESS_MESSAGE, projectName))
                .build();
    }

    private static boolean isOrderProjectArchiveCommand(final String fullCommand) {
        return fullCommand.toLowerCase().contains("file:");
    }

    private static boolean isValidationRequired(final String fullCommand) {
        return !fullCommand.toLowerCase().contains(" "+CliCommandOption.NO_VALIDATION.getShortFlag());
    }

    private static boolean isOrderProjectCommand(final CommandLine commandOptions) {
        return commandOptions.hasOption(CliCommandOption.PROJECT.getShortForm());
    }

    private void validateProjectSize(final Map<String, Object> commandProperties){
        final String filePath = (String) commandProperties.get(FILE_PATH_IDENTIFIER);
        final File file = new File(filePath);
        final long fileSize = file.length();

        if (fileSize > MAXIMUM_PROJECT_FILE_SIZE_IN_BYTES){
            logger.error("The project file {} which has {} bytes exceeds the maximum allowed size (5MB)", file.getName(), fileSize);
            throw new UnsupportedProjectSizeException(apMessages.get("validation.project.maximum.file.size.error"));
        }
    }

    private static CommandOptions getProjectOrNodeOptions() {
        final Options orderOptions = new Options();
        final OptionGroup nameOptionGroup = new OptionGroup();
        nameOptionGroup.setRequired(true);

        final Option projectOption = createOption(CliCommandOption.PROJECT, true);
        projectOption.setOptionalArg(false);
        nameOptionGroup.addOption(projectOption);

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setOptionalArg(false);
        nameOptionGroup.addOption(nodeOption);

        orderOptions.addOptionGroup(nameOptionGroup);
        return new CommandOptions(orderOptions, true);
    }

    private static CommandOptions getValidationOption() {
        final Options orderOptions = new Options();
        final OptionGroup nameOptionGroup = new OptionGroup();
        nameOptionGroup.setRequired(false);

        final Option nvOption = createOption(CliCommandOption.NO_VALIDATION, false);
        nvOption.setOptionalArg(true);
        nameOptionGroup.addOption(nvOption);

        orderOptions.addOptionGroup(nameOptionGroup);
        return new CommandOptions(orderOptions, false);
    }

    @Override
    protected CommandOptions getCommandOptions() {
        return null; // Not used as we directly overwrite the #validateCommand method instead
    }

    private static String getMoName(final CommandLine commandOptions) {
        return isOrderProjectCommand(commandOptions) ? commandOptions.getOptionValue(CliCommandOption.PROJECT.getShortForm())
                : commandOptions.getOptionValue(CliCommandOption.NODE.getShortForm());
    }
}
