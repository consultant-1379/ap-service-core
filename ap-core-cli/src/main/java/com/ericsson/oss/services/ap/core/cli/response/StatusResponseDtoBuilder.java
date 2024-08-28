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
package com.ericsson.oss.services.ap.core.cli.response;

import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.CANCELLED;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.FAILED;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.IN_PROGRESS;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.SUCCESSFUL;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.getIntegrationPhase;
import static com.ericsson.oss.services.ap.api.status.IntegrationPhase.values;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Builds a {@link ResponseDto} for the AP status command.
 */
public class StatusResponseDtoBuilder {

    private static final String PROJECT_NAME = "Project Name";
    private static final String DEPLOYMENT_NAME = "Deployment Name";
    private static final String NODE_QUANTITY = "Node Quantity";
    private static final String[] HEADERS = { "Task", "Progress", "Timestamp", "Additional Information" };
    private static final String[] PROJECTS_SUMMARY_TABLE_HEADERS = { PROJECT_NAME, NODE_QUANTITY, IN_PROGRESS.getName(), SUSPENDED.getName(),
        SUCCESSFUL.getName(), FAILED.getName(), CANCELLED.getName() };
    private static final String[] PROJECT_NODE_SUMMARY_TABLE_HEADERS = { PROJECT_NAME, NODE_QUANTITY, IN_PROGRESS.getName(),  SUSPENDED.getName(),
        SUCCESSFUL.getName(), FAILED.getName(), CANCELLED.getName() };
    private static final String[] DEPLOYMENT_NODE_SUMMARY_TABLE_HEADERS = { DEPLOYMENT_NAME, NODE_QUANTITY, IN_PROGRESS.getName(),  SUSPENDED.getName(),
        SUCCESSFUL.getName(), FAILED.getName(), CANCELLED.getName() };
    private static final String[] NODE_SUMMARY_TABLE_HEADERS = { "Node Name", "Status", "State" };
    private static final String DEPLOYMENT_NOT_FOUND_ERROR_MESSAGE_KEY = "deployment.not.found";
    private static final String DEPLOYMENT_NOT_FOUND_SOLUTION_MESSAGE_KEY = "deployment.not.found.solution";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ResponseDtoBuilder baseBuilder;

    /**
     * Builds a {@link ResponseDto} used as response of node status command.
     *
     * @param nodeStatus
     *            the status of the node
     * @return a new instance of {@link ResponseDto} well formatted with the node status
     */
    public ResponseDto buildViewNodeStatusCommandResponseDto(final NodeStatus nodeStatus) {
        final Map<String, Object> nodeAttrs = new LinkedHashMap<>();
        nodeAttrs.put("Node Name", nodeStatus.getNodeName());
        nodeAttrs.put("Project Name", nodeStatus.getProjectName());
        nodeAttrs.put("State", getStateDisplayName(nodeStatus.getState()));

        final List<AbstractDto> commandResult = baseBuilder.buildLineDtosOfNameValuePairs(nodeAttrs);
        commandResult.add(new LineDto()); // empty line

        final List<StatusEntry> statusEntries = nodeStatus.getStatusEntries();

        if (statusEntries.isEmpty()) {
            statusEntries.add(new StatusEntry("", "", "", "")); // always display table headings
        }

        final List<List<String>> nodeStatusTableRows = new ArrayList<>(statusEntries.size());

        for (final StatusEntry statusEntry : statusEntries) {
            final List<String> row = new ArrayList<>(4);
            row.add(statusEntry.getTaskName());
            row.add(statusEntry.getTaskProgress());
            row.add(statusEntry.getTimeStamp());
            row.add(statusEntry.getAdditionalInfo());
            nodeStatusTableRows.add(row);
        }

        final List<AbstractDto> nodeStatusTableDto = baseBuilder.buildHorizontalTableDto(nodeStatusTableRows, HEADERS);
        commandResult.addAll(nodeStatusTableDto);
        return new ResponseDto(commandResult);
    }

    /**
     * Builds a {@link ResponseDto} used as response of project status command.
     *
     * @param projectStatus
     *            the status of the project
     * @return a new instance of {@link ResponseDto} well formatted with the project status
     */
    public ResponseDto buildViewProjectStatusCommandResponseDto(final ApNodeGroupStatus projectStatus) {
        final List<AbstractDto> commandResult = new ArrayList<>();
        commandResult.addAll(createApNodeGroupSummaryTable(projectStatus));
        commandResult.addAll(createNodeSummaryTable(projectStatus.getNodesStatus()));

        return new ResponseDto(commandResult);
    }

    /**
     * Builds a {@link CommandResponseDto} used as response of deployment status command.
     *
     * @param deploymentStatus
     *            the status of the deployment
     * @param fullCommand
     *            the full command
     * @return a new instance of {@link CommandResponseDto} well formatted with the project status
     */
    public CommandResponseDto buildViewDeploymentStatusCommandResponseDto(final ApNodeGroupStatus deploymentStatus, final String fullCommand) {
       if (deploymentStatus.getNumberOfNodes() == 0) {
           return buildFailureViewDeploymentStatusCommandResponseDto(fullCommand);
       } else {
           return buildSuccessfulViewDeploymentStatusCommandResponseDto(deploymentStatus, fullCommand);
       }
    }

    private CommandResponseDto buildSuccessfulViewDeploymentStatusCommandResponseDto(final ApNodeGroupStatus deploymentStatus, final String fullCommand) {
        final List<AbstractDto> commandResult = new ArrayList<>();
        commandResult.addAll(createApNodeGroupSummaryTable(deploymentStatus));
        commandResult.addAll(createNodeSummaryTable(deploymentStatus.getNodesStatus()));

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .responseDto(new ResponseDto(commandResult))
                .build();
    }

    private CommandResponseDto buildFailureViewDeploymentStatusCommandResponseDto(final String fullCommand) {
        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.ENTITY_NOT_FOUND_ERROR_CODE)
                .statusMessage(apMessages.get(DEPLOYMENT_NOT_FOUND_ERROR_MESSAGE_KEY))
                .solution(apMessages.get(DEPLOYMENT_NOT_FOUND_SOLUTION_MESSAGE_KEY))
                .build();
    }

    /**
     * Builds a {@link ResponseDto} used as response of project statuses command.
     *
     * @param projectStatuses
     *            the status of the project
     * @return a new instance of {@link ResponseDto} formatted with the project status
     */
    public ResponseDto buildViewAllProjectStatusesCommandResponseDto(final List<ApNodeGroupStatus> projectStatuses) {
        final List<AbstractDto> commandResult = new ArrayList<>();
        if (!projectStatuses.isEmpty()) {
            commandResult.addAll(createProjectsSummaryTable(projectStatuses));
        }
        return new ResponseDto(commandResult);
    }

    private List<AbstractDto> createProjectsSummaryTable(final List<ApNodeGroupStatus> projectStatuses) {
        final List<List<String>> rowEntries = new ArrayList<>(projectStatuses.size());
        for (final ApNodeGroupStatus projectStatus : projectStatuses) {
            final List<String> rowEntry = convertApNodeGroupStatusToRow(projectStatus);
            rowEntries.add(rowEntry);
        }

        return baseBuilder.buildHorizontalTableDto(rowEntries, PROJECTS_SUMMARY_TABLE_HEADERS);
    }

    private static List<String> convertApNodeGroupStatusToRow(final ApNodeGroupStatus apNodeGroupStatus) {
        final List<String> rowEntry = new ArrayList<>();
        rowEntry.add(apNodeGroupStatus.getApNodeGroupName());
        rowEntry.add(String.valueOf(apNodeGroupStatus.getNumberOfNodes()));

        for (final IntegrationPhase integrationPhase : values()) {
            rowEntry.add(String.valueOf(apNodeGroupStatus.getIntegrationPhaseSummary().get(integrationPhase)));
        }
        return rowEntry;
    }

    private List<AbstractDto> createApNodeGroupSummaryTable(final ApNodeGroupStatus apNodeGroupStatus) {
        final List<List<String>> rowEntries = new ArrayList<>(1);
        rowEntries.add(convertApNodeGroupStatusToRow(apNodeGroupStatus));

        return baseBuilder.buildHorizontalTableDto(rowEntries, getApNodeGroupHeaderRow(apNodeGroupStatus));
    }

    private String[] getApNodeGroupHeaderRow(final ApNodeGroupStatus collectionStatus) {
        return collectionStatus.isProjectGroup() ? PROJECT_NODE_SUMMARY_TABLE_HEADERS : DEPLOYMENT_NODE_SUMMARY_TABLE_HEADERS;
    }

    private List<AbstractDto> createNodeSummaryTable(final List<NodeStatus> nodeStatuses) {
        final List<List<String>> rowEntries = new ArrayList<>(nodeStatuses.size());

        for (final NodeStatus nodeStatus : nodeStatuses) {
            final List<String> rowEntry = new ArrayList<>(3);
            rowEntry.add(nodeStatus.getNodeName());
            rowEntry.add(convertNodeStatusToIntegrationPhaseName(nodeStatus));
            rowEntry.add(getStateDisplayName(nodeStatus.getState()));
            rowEntries.add(rowEntry);
        }

        return baseBuilder.buildHorizontalTableDto(rowEntries, NODE_SUMMARY_TABLE_HEADERS);
    }

    private static String convertNodeStatusToIntegrationPhaseName(final NodeStatus nodeStatus) {
        final String nodeStateValue = nodeStatus.getState();
        final IntegrationPhase phase = getIntegrationPhase(nodeStateValue);
        return phase.getName();
    }

    private static String getStateDisplayName(final String nodeState) {
        final State state = State.getState(nodeState);
        return state.getDisplayName();
    }
}
