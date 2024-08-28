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
package com.ericsson.oss.services.ap.core.test.rest.download;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.ericsson.oss.services.ap.core.test.common.DownloadCommonTest;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;
import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Artifact;
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor;
import com.ericsson.oss.services.ap.core.test.ErbsProjectOptions;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;

import cucumber.api.java.en.Given;

import javax.inject.Inject;

@RunWith(Cucumber.class)
public class DownloadRestTest extends DownloadCommonTest {

    @Inject
    private EnvironmentTestSteps environmentTestSteps;

    private static final Set<String> VNF_NODE_TYPES = new HashSet<>(Arrays.asList("vPP", "vSD"));

    private final TransactionalExecutor executor = new TransactionalExecutor();

    @Given("^the node has already been precreated$")
    public void add_node_to_system() {
        nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
        stubbedService.create_download_service_stub(nodeType);
    }

    @Given("^the node (has|has not) been ordered$")
    public void order_node(final String condition) {
        if ("has".equals(condition)) {
            order_node(nodeMo);
        }
    }

    public void order_node(final ManagedObject nodeMo) {
        String generatedArtifactFileType;
        Artifact generatedArtifact;
        if (VNF_NODE_TYPES.contains(nodeMo.getAttribute("nodeType"))) {
            return;
        } else if (nodeMo.getAttribute("nodeType").equals("MSRBS_V1")) {
            generatedArtifactFileType = "ccf";
            generatedArtifact = new Artifact(generatedArtifactFileType, "node-artifacts/msrbs_v1/ccf.xml");
        } else {
            generatedArtifactFileType = "siteInstallation";
            generatedArtifact = ErbsProjectOptions.getDefaultArtifact(generatedArtifactFileType);
        }
        final String artifactResourcePath = generatedArtifact.getArtifactLocation();
        final Path artifactFilePath = environmentTestSteps.create_generated_artifact_file(nodeMo, artifactResourcePath);

        final String artifactFdn = environmentTestSteps.getArtifactFdn(
            nodeMo.getFdn(),
            generatedArtifactFileType);

        environmentTestSteps.update_node_mo_with_generated_file_path(artifactFdn, artifactFilePath.toString());
    }

    @Given("'Project=(.+),Node=(.+)' is in state '(.+)'")
    public void update_state_in_node_status_mo(final String projectName, final String nodeName, final String nodeState) {
        nodeFdn = "Project="+projectName+",Node="+nodeName;
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
