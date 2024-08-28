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

import com.ericsson.oss.services.ap.api.exception.UnsupportedNodeTypeException;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Maps <code>UnsupportedNodeTypeException</code> to <code>CommandResponseDto</code>.
 */
public class UnsupportedNodeTypeExceptionMapper implements ExceptionMapper<UnsupportedNodeTypeException> {

    private static final String SOLUTION_MESSAGE_KEY = "unsupported.node.type.solution";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ModelReader modelReader;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final UnsupportedNodeTypeException e) {
        return new CommandResponseDtoBuilder()
            .fullCommand(command)
            .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
            .errorCode(CliErrorCodes.UNSUPPORTED_NODE_TYPE_ERROR_CODE)
            .statusMessage(e.getMessage())
            .solution(apMessages.format(SOLUTION_MESSAGE_KEY, getValidNodeTypesString()))
            .build();
    }

    private String getValidNodeTypesString() {
        final Collection<String> nodeTypes = modelReader.getSupportedNodeTypes();
        return StringUtils.join(nodeTypes, ", ");
    }
}
