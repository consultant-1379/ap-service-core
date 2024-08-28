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
package com.ericsson.oss.services.ap.core.cli.response;

import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;

/**
 * Builder for {@link CommandResponseDto}.
 */
public class CommandResponseDtoBuilder {

    private final CommandResponseDto commandResponseDto;

    public CommandResponseDtoBuilder() {
        commandResponseDto = new CommandResponseDto();
    }

    /**
     * Specifies the property <code>fullCommand</code> of {@link CommandResponseDto} to be built.
     *
     * @param fullCommand
     *            the <code>fullCommand</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder fullCommand(final String fullCommand) {
        commandResponseDto.setCommand(fullCommand);
        return this;
    }

    /**
     * Specifies the property <code>statusCode</code> of {@link CommandResponseDto} to be built.
     *
     * @param statusCode
     *            the <code>statusCode</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder statusCode(final int statusCode) {
        commandResponseDto.setStatusCode(statusCode);
        return this;
    }

    /**
     * Specifies the property <code>statusMessage</code> of {@link CommandResponseDto} to be built.
     *
     * @param statusMessage
     *            the <code>statusMessage</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder statusMessage(final String statusMessage) {
        commandResponseDto.setStatusMessage(statusMessage);
        return this;
    }

    /**
     * Specifies the property <code>responseDto</code> of {@link CommandResponseDto} to be built.
     *
     * @param responseDto
     *            the <code>responseDto</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder responseDto(final ResponseDto responseDto) {
        commandResponseDto.setResponseDto(responseDto);
        return this;
    }

    /**
     * Specifies the property <code>errorCode</code> of {@link CommandResponseDto} to be built.
     *
     * @param errorCode
     *            the <code>errorCode</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder errorCode(final int errorCode) {
        commandResponseDto.setErrorCode(errorCode);
        return this;
    }

    /**
     * Specifies the property <code>solution</code> of {@link CommandResponseDto} to be built.
     *
     * @param solution
     *            the <code>solution</code> to be set
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder solution(final String solution) {
        commandResponseDto.setSolution(solution);
        return this;
    }

    /**
     * Adds a text line to the {@link CommandResponseDto} to be built.
     *
     * @param value
     *            the line to be added
     * @return a reference of this object
     */
    public CommandResponseDtoBuilder line(final String value) {
        commandResponseDto.addLine(value);
        return this;
    }

    public CommandResponseDtoBuilder setLogViewerCompatible() {
        commandResponseDto.setIsLogViewerCompatible(true);
        return this;
    }

    public CommandResponseDtoBuilder setLogReference(final String logReference) {
        commandResponseDto.setLogReference(logReference);
        return this;
    }

    /**
     * Builds a {@link CommandResponseDto} using the properties set previously.
     * <p>
     * Success or error lines are added based on <code>errorCode</code> value. An empty {@link ResponseDto} is also set if none has been provided.
     *
     * @return a new {@link CommandResponseDto} instance
     */
    public CommandResponseDto build() {
        addFormatting();
        return commandResponseDto;
    }

    public CommandResponseDto buildLineResponse() {
        return commandResponseDto;
    }

    private void addFormatting() {
        if (isValidErrorCode()) {
            addErrorFormatting();
        } else {
            addSuccessFormatting();
        }
    }

    private boolean isValidErrorCode() {
        return commandResponseDto.getErrorCode() > 0;
    }

    private void addErrorFormatting() {
        commandResponseDto.addErrorLines();
    }

    private void addSuccessFormatting() {
        commandResponseDto.addSuccessLines();
        commandResponseDto.addEmptyLine();
    }
}
