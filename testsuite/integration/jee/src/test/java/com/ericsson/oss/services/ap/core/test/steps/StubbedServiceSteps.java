/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.mockito.Matchers;

import com.ericsson.oss.services.ap.api.download.DownloadArtifactService;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;
import com.ericsson.oss.services.ap.core.test.steps.dependencies.NodeIdentifierSoftwarePackageDto;
import com.ericsson.oss.services.shm.filestore.swpackage.api.SoftwarePackage;
import com.ericsson.oss.services.shm.swpackage.query.api.SoftwarePackageQueryService;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.api.instance.WorkflowInstance;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * Includes all the steps needed to deploy stubs for the order node test cases.
 */
public class StubbedServiceSteps {

    private final static Set<String> VNF_NODE_TYPES = new HashSet<>(Arrays.asList("vPP", "vSD"));

    @Inject
    private Stubs stubs;

    @Step("Create Generic Stub for AutoProvisoningWorkFlowService")
    public void createApWorkflowServiceStub(final boolean successFailure, final String message) throws WorkflowMessageCorrelationException {
        final AutoProvisioningWorkflowService apWorkflowService = stubs.injectIntoSystem(AutoProvisioningWorkflowService.class);
        doReturn(successFailure).when(apWorkflowService).isSupported(message);
        setupWorkflowStubs(apWorkflowService);
    }

    @Step("Create Stub for AutoProvisoningWorkFlowService")
    public void create_ap_workflow_service_stub() {
        final AutoProvisioningWorkflowService apWorkflowService = stubs.injectIntoSystem(AutoProvisioningWorkflowService.class);
        setupWorkflowStubs(apWorkflowService);
    }

    private void setupWorkflowStubs(final AutoProvisioningWorkflowService apWorkflowService) {
        doReturn("d.1.44_fake").when(apWorkflowService).getOrderWorkflowName();
        doReturn("reconfiguration_order_ecim_1").when(apWorkflowService).getReconfigurationOrderWorkflowName();
        doReturn("expansion_order_ecim_1").when(apWorkflowService).getExpansionOrderWorkflowName();
    }

    @Step("Create Stub for WorkflowInstanceServiceLocal")
    public void create_workflow_instance_service_stub() {
        final WorkflowInstanceServiceLocal workflowService = stubs.injectIntoSystem(WorkflowInstanceServiceLocal.class);
        when(workflowService.startWorkflowInstanceByDefinitionId(anyString(), anyString(),
            anyMapOf(String.class, Object.class))).thenReturn(workflowServiceId("any_name"));
    }

    @Step("Create Stub for WorkflowQueryServiceLocal")
    public void create_workflow_query_service_stub() {
        final WorkflowQueryServiceLocal workflowQueryService = stubs.injectIntoSystem(WorkflowQueryServiceLocal.class);
        final List<WorkflowObject> queryInstances = new ArrayList<>();
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(queryInstances);
    }

    @Step("Create Stub for DownloadService")
    public void create_download_service_stub(final String nodeType) {
        final DownloadArtifactService downloadArtifactService = stubs.injectIntoSystem(DownloadArtifactService.class);

        if (VNF_NODE_TYPES.contains(nodeType)) {
            when(downloadArtifactService.isOrderedArtifactSupported()).thenReturn(false);
        } else {
            when(downloadArtifactService.isOrderedArtifactSupported()).thenReturn(true);
        }
    }

    @Step("Create Stub for Software Query Package")
    public void create_software_query_package(final String upgradePackage) {
        final Map<String, SoftwarePackage> nodeIdentifierSoftwarePackages = new HashMap<>();
        final SoftwarePackage softwarePackage = getNodeIdentifierSoftwarePackage(upgradePackage);
        nodeIdentifierSoftwarePackages.put("nodeIdentifierSoftwarePackage", softwarePackage);
        final SoftwarePackageQueryService softwarePackageQueryService = stubs.injectIntoSystem(SoftwarePackageQueryService.class);
        when(softwarePackageQueryService.getSoftwarePackagesBasedOnPackageName(Matchers.<Map<String, List<String>>> any()))
            .thenReturn(nodeIdentifierSoftwarePackages);
    }

    private SoftwarePackage getNodeIdentifierSoftwarePackage(final String upgradePackage) {
        return new SoftwarePackage(NodeIdentifierSoftwarePackageDto.getInstance(upgradePackage));
    }

    private WorkflowInstance workflowServiceId(final String wfId) {
        return new WorkflowInstance("fake_def", wfId, "fake_key");
    }
}
