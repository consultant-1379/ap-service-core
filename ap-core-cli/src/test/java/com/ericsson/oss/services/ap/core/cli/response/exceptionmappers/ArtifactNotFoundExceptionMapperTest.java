/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;

import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.core.cli.handlers.CommandResponseValidatorTest;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link ArtifactNotFoundExceptionMapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactNotFoundExceptionMapperTest {

    @InjectMocks
    private ArtifactNotFoundExceptionMapper artifactNotFoundExceptionMapper;

    @Mock
    private ArtifactNotFoundException artifactNotFoundException;

    @Test
    public void whenArtifactForDownloadNotFound_ThenDownloadErrorMessagesCalled() {
        final String downloadCommand = "ap download -n " + NODE_NAME;
        final CommandResponseDto commandResponse = artifactNotFoundExceptionMapper.toCommandResponse(downloadCommand, artifactNotFoundException);
        CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(commandResponse,
                 "Node is not in correct state to execute the requested operation. See AP Online help for more information.", "Not applicable");
    }

    @Test
    public void whenArtifactForUploadNotFound_ThenUploadErrorMessagesCalled() {
        final String uploadCommand = "ap upload -n " + NODE_NAME + " file:fileName";
        final CommandResponseDto commandResponse = artifactNotFoundExceptionMapper.toCommandResponse(uploadCommand, artifactNotFoundException);
        CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(commandResponse,
                "Node does not contain any matching configuration file fileName", "Rename the configuration file to match the name of an existing configuration file");
    }
}
