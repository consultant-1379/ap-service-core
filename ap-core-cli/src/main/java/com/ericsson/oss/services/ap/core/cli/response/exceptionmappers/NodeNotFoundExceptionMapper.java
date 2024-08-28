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

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>NodeNotFoundException</code> to <code>CommandResponseDto</code>.
 */
public class NodeNotFoundExceptionMapper implements ExceptionMapper<NodeNotFoundException> {

    private static final String NODE_NOT_EXIST_ERROR_MESSAGE_KEY = "node.not.found";
    private static final String NODE_NOT_EXIST_SOLUTION_MESSAGE_KEY = "node.not.found.solution";

    private static final String NOT_AP_NODE_ERROR_MESSAGE_KEY = "ap.node.not.found";
    private static final String NOT_AP_NODE_SOLUTION_MESSAGE_KEY = "ap.node.not.found.solution";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private DpsQueries dpsQueries;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final NodeNotFoundException e) {
        final String nodeName = CommandArgs.getProjectOrNodeName(command);
        final long networkElementCount = dpsQueries.findMoByName(nodeName, MoType.NETWORK_ELEMENT.toString(), Namespace.OSS_NE_DEF.toString()).executeCount();

        if (networkElementCount == 0) {
            return new CommandResponseDtoBuilder()
                    .fullCommand(command)
                    .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                    .errorCode(CliErrorCodes.ENTITY_NOT_FOUND_ERROR_CODE)
                    .statusMessage(apMessages.get(NODE_NOT_EXIST_ERROR_MESSAGE_KEY))
                    .solution(apMessages.get(NODE_NOT_EXIST_SOLUTION_MESSAGE_KEY))
                    .build();
        }

        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.ENTITY_NOT_FOUND_ERROR_CODE)
                .statusMessage(apMessages.get(NOT_AP_NODE_ERROR_MESSAGE_KEY))
                .solution(apMessages.get(NOT_AP_NODE_SOLUTION_MESSAGE_KEY))
                .build();
    }
}
