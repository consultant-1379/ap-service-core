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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.arquillian.util.Dps;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * All the steps needed to update AP Node Mo attributes
 */
public class NodeAttributeTestSteps {

    @Inject
    private Dps dpsHelper;

    @Step("Update the node deployment to {1} for node {0}")
    public void updateNodeDeployment(final String nodeFdn, final String state) {
        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put("deployment", state);

        dpsHelper.updateMo(nodeFdn, nodeAttributes);
    }
}
