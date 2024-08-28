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
package com.ericsson.oss.services.ap.core.cli.handlers

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

class UploadArtifactCommandHandlerSpec extends CdiSpecification {

    private static final String FILE_NAME = "fileName";

    @MockedImplementation
    private ArgumentResolver argumentResolver; // NOPMD

    @ObjectUnderTest
    private UploadArtifactCommandHandler uploadArtifactCommandHandler;

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "Running upload command with valid file should return valid response"() {

        given: "Upload command is valid format"
        final Map<String, Object> commandProperties = new HashMap<>();
        commandProperties.put("fileName", FILE_NAME);
        argumentResolver.getFileName(commandProperties) >> FILE_NAME;
        final CliCommand cliCommand = new CliCommand(String.format("ap upload -n %s file:%s", NODE_NAME, FILE_NAME), commandProperties);

        when: "Command is executed"
        final CommandResponseDto result = uploadArtifactCommandHandler.processCommand(cliCommand);

        then: "Response is successful"
        CommandResponseValidatorTest.verifySuccess(result);
    }

    def "Running upload command with invalid syntax should throw ParseException with invalid syntax response returned"() {

        given: "Upload command has invalid parameter syntax"
        final CliCommand cliCommand = new CliCommand("ap upload -y", Collections.<String, Object> emptyMap());

        when: "Command is executed"
        final CommandResponseDto result = uploadArtifactCommandHandler.processCommand(cliCommand);

        then: "Response returns invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(result, "upload");
    }

    def "Running upload command with missing file should throw ParseException with invalid syntax response returned" () {

        given: "Upload command created for missing file"
        final CliCommand cliCommand = new CliCommand("ap upload -n " + NODE_NAME + " fileName", Collections.<String, Object> emptyMap());

        when: "Command is executed"
        final CommandResponseDto result = uploadArtifactCommandHandler.processCommand(cliCommand);

        then: "Response returns invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(result, "upload");
    }

    def "Running upload command with no file specified, should throw ParseException with invalid syntax response returned" () {

        given: "Upload command does not have file specified"
        final CliCommand cliCommand = new CliCommand("ap upload -n " + NODE_NAME, null);

        when: "Command is executed"
        final CommandResponseDto result = uploadArtifactCommandHandler.processCommand(cliCommand);

        then: "Response returns invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(result, "upload");
    }
}
