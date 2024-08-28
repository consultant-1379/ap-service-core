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
package com.ericsson.oss.services.ap.core.test.common;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import cucumber.api.java.en.Given;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Main Download test class, to be extended by all Download classes.
 */
public abstract class DownloadCommonTest extends ServiceCoreTest {

    @Inject
    private Dps dpsHelper;

    @Inject
    protected StubbedServiceSteps stubbedService;

    protected String nodeName;
    protected String nodeType;
    protected String nodeFdn;
    protected String projectName;
    protected ManagedObject nodeMo;

    @Given("^the user wants to download the (raw|generated) artifacts for the node 'Project=(.+),Node=(.+)' of type (.+)$")
    public void set_project_and_node_name_and_type(final String condition, final String projectName, final String nodeName, final String nodeType) {
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.projectName = projectName;
        nodeFdn = "Project=" + projectName + ",Node=" + nodeName;
    }

    @Given("^the node (does|does not) exist$")
    public void validate_node_existence(final String condition) {
        final ManagedObject mo = dpsHelper.findMoByFdn(nodeFdn);
        if ("does".equals(condition)) {
            assertThat(mo).as("Managed object for " + nodeFdn + " should exist").isNotNull();
        } else {
            assertThat(mo).as("Managed object for " + nodeFdn + " should NOT exist").isNull();
        }
    }
}
