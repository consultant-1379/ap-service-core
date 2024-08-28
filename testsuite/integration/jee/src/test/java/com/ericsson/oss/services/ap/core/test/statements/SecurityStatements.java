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
package com.ericsson.oss.services.ap.core.test.statements;

import static com.ericsson.oss.services.ap.stubs.ThreadSecurityControl.setSecurityEnabledForThread;

import cucumber.api.java.en.Given;

public class SecurityStatements extends ServiceCoreTestStatements {

    @Given("^security is (enabled|disabled)$")
    public void set_security_status(final String enabled) {
        setSecurityEnabledForThread("enabled".equals(enabled));
    }

    @Given("^the user is (.+)$")
    public void set_security_user(final String userType) {
        userTestSteps.set_user_as(userType);
    }

    @Override
    public void clear() {
        setSecurityEnabledForThread(false);
    }
}
