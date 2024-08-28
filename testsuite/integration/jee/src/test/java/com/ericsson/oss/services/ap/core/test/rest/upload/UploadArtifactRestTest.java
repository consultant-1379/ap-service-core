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
package com.ericsson.oss.services.ap.core.test.rest.upload;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.UploadArtifactTestSteps;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

@RunWith(Cucumber.class)
public class UploadArtifactRestTest extends ServiceCoreTest {

    @Inject
    private UploadArtifactTestSteps uploadArtifactTestSteps;

    private final TransactionalExecutor executor = new TransactionalExecutor();

    @When("^a user of type '(.+)' uploads a file '(.+)' to the uri '(.+)' for node '(.+)'$")
    public void user_requests_to_upload_file_to_uri(final String userType, final String filePath, final String uri, final String nodeName)
        throws IOException {

        uploadArtifactTestSteps.create_upload_service_stub();

        final String fileContent = new String(Resources.getResourceAsBytes(filePath), StandardCharsets.UTF_8);
        final String updatedFileContent = fileContent.replaceAll("%NODE_NAME%", nodeName);
        final InputStream inputStream = new ByteArrayInputStream(updatedFileContent.getBytes(Charset.forName("UTF-8")));

        final File targetFile = new File(filePath);
        FileUtils.copyInputStreamToFile(inputStream, targetFile);

        restStatements.user_requests_put_rest_call_with_file_to_uri(userType, targetFile, uri);
    }

    @Given("'Project=(.+),Node=(.+)' is in state '(.+)'")
    public void update_state_in_node_status_mo(final String projectName, final String nodeName, final String nodeState) {
        final String nodeFdn = "Project="+projectName+",Node="+nodeName;
        final DataPersistenceService dpsHelper = new ServiceFinderBean().find(DataPersistenceService.class); // NOSONAR
        final String nodeStatusFdn = new StringBuilder(nodeFdn).append(",NodeStatus=1").toString();
        final Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws WorkflowMessageCorrelationException {
                final Map<String, Object> nodeStatusAttributes = new HashMap<>();
                nodeStatusAttributes.put("state", nodeState);

                dpsHelper.getLiveBucket().findMoByFdn(nodeStatusFdn).setAttributes(nodeStatusAttributes);
                return null;
            }
        };
        try {
            executor.execute(callable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error update state of node status mo in transaction for Node %s", nodeFdn), e);
        }
    }

}
