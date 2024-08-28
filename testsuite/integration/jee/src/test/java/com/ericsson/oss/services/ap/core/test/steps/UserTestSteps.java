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

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.services.ap.annotations.SecurityEnabled;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * This class contains all the steps needed for set up users within a test case.
 * <p>
 * In the future this implementation for the modification/deletion of user within a test case should be implemented within this class.
 */
public class UserTestSteps {

    @Inject
    private ContextService contextService;

    /**
     * Sets the User Name in the Context Service.
     * <p>
     * This test step should be used when executing a test case with the annotation {@link SecurityEnabled}.
     *
     * @param userType
     *            the user to set
     */
    @Step("Setting header X-Tor-UserID on context service to {0}")
    public void set_user_as(final String userType) {
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType);
    }
}
