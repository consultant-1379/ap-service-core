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
package com.ericsson.oss.services.ap.core.test.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.statements.NodeStatements.NodeStatementListener;
import com.ericsson.oss.services.ap.core.test.steps.ViewStatusTestSteps;

public abstract class ViewStatusTest extends ServiceCoreTest implements NodeStatementListener {

    @Inject
    protected ViewStatusTestSteps viewStatusSteps;

    private final Map<Integer, List<String>> nodeNames;

    public ViewStatusTest() {
        this.nodeNames = new HashMap<>();
    }

    @Before
    public void setup() {
        nodeStatements.addListener(this);
    }

    private List<String> getNamesForProject(final int projectIndex) {
        if (this.nodeNames.containsKey(projectIndex)) {
            return nodeNames.get(projectIndex);
        } else {
            final List<String> names = new ArrayList<>();
            this.nodeNames.put(projectIndex, names);
            return names;
        }
    }

    protected String getNodeName(final int nodeIndex) {
        return getNodeName(1, nodeIndex);
    }

    protected String getNodeName(final int projectIndex, final int nodeIndex) {
        return this.nodeNames.get(projectIndex).get(nodeIndex - 1);
    }

    @Override
    public void stateChanged(final int projectIndex, final int nodeIndex, final ManagedObject node, final String state) {
        final List<String> names = getNamesForProject(projectIndex);
        names.add(node.getName());
    }
}
