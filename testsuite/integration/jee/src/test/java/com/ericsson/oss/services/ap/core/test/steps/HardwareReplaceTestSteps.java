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
package com.ericsson.oss.services.ap.core.test.steps;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.api.workflow.HardwareReplaceCapabilty;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;

import ru.yandex.qatools.allure.annotations.Step;

public class HardwareReplaceTestSteps extends ServiceCoreTestSteps {

    @Inject
    private Stubs stubs;

    @Step("Create Stub for AutoProvisoningWorkFlowService")
    public void create_ap_workflow_service_stub_hardware_replace() {
        stubs.injectIntoSystem(AutoProvisioningWorkflowService.class);
    }

    @Step("Create Stub for Hardware replace capability")
    public void create_ap_capability_service_stub_hardware_replace() {
        final HardwareReplaceCapabilty hardwareReplaceCapability = stubs.injectIntoSystem(HardwareReplaceCapabilty.class);
        doReturn(true).when(hardwareReplaceCapability).isSupported(anyString());
    }

    @Step("Create Stub for Hardware replace specify backup capability")
    public void create_ap_capability_service_stub_hardware_replace_specify_backup(final boolean condition) {
        final HardwareReplaceCapabilty hardwareReplaceCapability = stubs.injectIntoSystem(HardwareReplaceCapabilty.class);
        doReturn(true).when(hardwareReplaceCapability).isSupported(anyString());
        doReturn(condition).when(hardwareReplaceCapability).isBackupSelectionSupported(anyString());
    }
}
