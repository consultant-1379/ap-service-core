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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import javax.inject.Inject;

import com.ericsson.oss.itpf.smrs.SmrsAccount;
import com.ericsson.oss.itpf.smrs.SmrsService;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * Includes all the steps needed to deploy stubs for the delete node test cases.
 */
public class StubbedServiceStepsForDeleteNode {

    @Inject
    private Stubs stubs;

    @Step("Create Stub for SmrsService")
    public void create_smrs_service_stub() {
        final SmrsService smrsService = stubs.injectIntoSystem(SmrsService.class);
        final SmrsAccount smrsAccount = getSmrsAccount();

        doReturn(smrsAccount)
                .when(smrsService)
                .getNodeSpecificAccount(anyString(), anyString(), anyString());

        doReturn(true)
                .when(smrsService)
                .deleteSmrsAccount(smrsAccount);
    }

    @Step("Create Stub for SmrsService that returns false")
    public void create_smrs_service_stub_that_fails_to_delete_smrs_account() {
        final SmrsService smrsService = stubs.injectIntoSystem(SmrsService.class);
        final SmrsAccount smrsAccount = getSmrsAccount();

        doReturn(smrsAccount)
                .when(smrsService)
                .getNodeSpecificAccount(anyString(), anyString(), anyString());

        doReturn(false)
                .when(smrsService)
                .deleteSmrsAccount(smrsAccount);
    }

    private SmrsAccount getSmrsAccount() {
        return new SmrsAccount();
    }
}
