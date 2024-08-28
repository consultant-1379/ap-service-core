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
package com.ericsson.oss.services.ap.core.test.steps;

import javax.inject.Inject;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doReturn;

import ru.yandex.qatools.allure.annotations.Step;

import com.ericsson.oss.services.ap.api.workflow.ProfileManagementCapability;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;

/**
 * Test Steps related to get capabilities.
 */
public class GetCapabilityTestStep {

    @Inject
    private Stubs stubs;

    private final List<String> nodeTypes = Collections.singletonList("RadioNode");

    @Step("Create Stub for Profile Management capability")
    public void createCapabilityServiceStubProfileManagement () {
        final ProfileManagementCapability profileManagementCapability = stubs.injectIntoSystem(ProfileManagementCapability.class);
        doReturn(nodeTypes).when(profileManagementCapability).getSupportedNodeTypes();
    }
}
