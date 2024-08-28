/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.rest.profile.export;

import static java.nio.file.Files.write;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;

import cucumber.api.java.en.Given;

@RunWith(Cucumber.class)
public class ExportCiqRestTest extends ServiceCoreTest {

    private static final String PROFILE_CIQ_DIRECTORY = "/ericsson/autoprovisioning/projects/%s/profiles/%s/ciq";

    @Given("^ciq (is|is not) generated for project '(.+)' and profile '(.+)'$")
    public void ciq_is_generated_for_a_profile(final String condition, final String projectName, final String profileName) {
        if (condition.equals("is")) {
            createGeneratedCIQ(projectName, profileName);
        }
    }

    private Path createGeneratedCIQ(final String projectName, final String profileName) {
        final byte[] ciqFileContents = Resources.getResourceAsBytes("import/node-plugin/generatedCIQ.csv");
        final String profileLocation = String.format(PROFILE_CIQ_DIRECTORY, projectName, profileName);
        return writeArtifact(profileLocation, ciqFileContents);
    }

    private Path writeArtifact(final String ciqLocation, final byte[] ciqFileContent) {
        final Path ciqFilePath = Paths.get(ciqLocation, "generatedCIQ.csv");
        try {
            ciqFilePath.getParent().toFile().mkdirs();
            write(ciqFilePath, ciqFileContent);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return ciqFilePath;
    }
}
