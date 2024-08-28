/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.UploadArtifactService;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;
import com.ericsson.oss.services.ap.core.test.statements.EnvironmentStatements;

import ru.yandex.qatools.allure.annotations.Step;

public class UploadArtifactTestSteps extends ServiceCoreTestSteps {

    @Inject
    private Stubs stubs;

    @Inject
    protected EnvironmentStatements environmentStatements;

    private static final Map<String, Set<String>> supportedUploadTypes = new HashMap<>();
    private final static List<String> nodeFileArtifacts = new ArrayList<>();
    private final static Set<String> validStates = new HashSet<>();

    static {
        validStates.add(State.ORDER_COMPLETED.toString());
        validStates.add(State.ORDER_FAILED.toString());
        validStates.add(State.READY_FOR_ORDER.toString());
        supportedUploadTypes.put("siteBasic", validStates);
        supportedUploadTypes.put("configuration", validStates);
        supportedUploadTypes.put("nodeConfiguration", validStates);
        supportedUploadTypes.put("unlockCell", validStates);
        nodeFileArtifacts.add("siteBasic");
    }

    @Step("Create Stub for SmrsService that returns false")
    public void create_upload_service_stub() {
        final UploadArtifactService uploadArtifactService = stubs.injectIntoSystem(UploadArtifactService.class, "erbs");
        when(uploadArtifactService.getSupportedUploadTypes()).thenReturn(new HashSet<>(supportedUploadTypes.keySet()));
        when(uploadArtifactService.isNodeArtifactFile("siteBasic")).thenReturn(true);
        when(uploadArtifactService.getValidStatesForUpload(anyString())).thenReturn(validStates);

        final UploadArtifactService uploadArtifactServiceEcim = stubs.injectIntoSystem(UploadArtifactService.class, "ecim");
        when(uploadArtifactServiceEcim.getSupportedUploadTypes()).thenReturn(new HashSet<>(supportedUploadTypes.keySet()));
        when(uploadArtifactServiceEcim.isNodeArtifactFile("siteBasic")).thenReturn(true);
        when(uploadArtifactServiceEcim.getValidStatesForUpload(anyString())).thenReturn(validStates);
    }
}
