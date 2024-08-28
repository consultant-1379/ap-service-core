/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.rest.profile.get;

import static java.nio.file.Files.write;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;

import cucumber.api.java.en.Given;

/**
 * Tests related to get node snapshot through REST.
 */
@RunWith(Cucumber.class)
public class GetSnapshotRestTest extends ServiceCoreTest {
    @Inject
    protected EnvironmentTestSteps environmentTestSteps;

    private static final String SNAPSHOT_DIRECTORY = "/ericsson/autoprovisioning/artifacts/generated/%s";

    @Given("^snapshot is generated for node '(.+)' with filter in project '(.+)' with profile '(.+)'$")
    public void snapshot_is_generated_for_a_profile(final String nodeName, final String projectName, final String profileName) {
        final String profileFdn = String.format("Project=%s,ConfigurationProfile=%s",projectName,profileName);
        environmentTestSteps.update_profile_mo_with_snapshot_status(profileFdn, "COMPLETED");
        createSnapshot(nodeName);
    }

    private Path createSnapshot(final String nodeName) {
        final byte[] snapshotFileContents = Resources.getResourceAsBytes("import/node-plugin/snapshot.xml");
        final String snapshotLocation = String.format(SNAPSHOT_DIRECTORY, nodeName);
        return writeSnapshot(nodeName, snapshotLocation, snapshotFileContents);
    }

    private Path writeSnapshot(final String nodeName, final String snapshotLocation, final byte[] snapshotFileContents) {
        final String snapshotFileName = String.format("%s_SNAPSHOT.xml", nodeName);
        final Path snapshotFilePath = Paths.get(snapshotLocation, snapshotFileName);
        try {
            snapshotFilePath.getParent().toFile().mkdirs();
            write(snapshotFilePath, snapshotFileContents);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return snapshotFilePath;
    }
}
