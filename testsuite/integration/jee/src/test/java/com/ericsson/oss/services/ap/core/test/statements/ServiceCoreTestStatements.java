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

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.test.steps.NodeStateTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.UserTestSteps;

public abstract class ServiceCoreTestStatements {

    @Inject
    protected UserTestSteps userTestSteps;

    @Inject
    protected NodeStateTestSteps nodeStateTestSteps;

    public void clear() {
    }
}
