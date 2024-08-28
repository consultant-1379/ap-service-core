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

import static com.ericsson.oss.services.ap.arquillian.util.data.project.ProjectDescriptor.ProjectDescriptorBuilder.usingNodeType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import ru.yandex.qatools.allure.annotations.Step;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.ProjectGenerator;
import com.ericsson.oss.services.ap.arquillian.util.data.project.ProjectDescriptor;
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;

/**
 * Includes all the steps needed for the order node test cases.
 */
public class BindTestSteps extends ServiceCoreTestSteps {

    @Inject
    private ProjectGenerator projectGenerator;

    @Inject
    private Dps dps;

    private TransactionalExecutor executor = new TransactionalExecutor(); // NOPMD

    private static final String ERBS = "ERBS";

    @Step("Bind node {0} with hwId {1}")
    public void bind(final String nodeFdn, final String harwareSerialNumber) {
        service.bind(nodeFdn, harwareSerialNumber);
    }

    @Step("Bulk Bind nodes and hwIds in {0}")
    public BatchBindResult batchBind(final String fileName, final byte[] bytes) {
        return service.batchBind(fileName, bytes);
    }

    @Step("Create a project with a single node.")
    public String create_project_with_one_node() {
        final ProjectDescriptor projectDescriptor = usingNodeType(ERBS).withNodeCount(1).build();
        final ManagedObject mo = projectGenerator.generate(projectDescriptor);
        return mo.getChildren()
                .iterator()
                .next()
                .getFdn();
    }

    @Step("Set harwareSerialNumber to {1} for node {0}")
    public void update_hardware_serial_number(final String nodeFdn, final String harwareSerialNumber) {
        final Map<String, Object> updatedAttributes = new HashMap<>();
        updatedAttributes.put("hardwareSerialNumber", harwareSerialNumber);
        dps.updateMo(nodeFdn, updatedAttributes);
    }

    @Step("Set node state to {1} for node {0}")
    public void update_state_in_node_status_mo(final String nodeFdn, final String nodeState) {
        final DataPersistenceService dpsHelper = new ServiceFinderBean().find(DataPersistenceService.class);
        final String nodeStatusFdn = new StringBuilder(nodeFdn).append(",NodeStatus=1").toString();
        final Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws WorkflowMessageCorrelationException {
                final Map<String, Object> nodeStatusAttributes = new HashMap<>();
                nodeStatusAttributes.put("state", nodeState);

                dpsHelper.getLiveBucket().findMoByFdn(nodeStatusFdn).setAttributes(nodeStatusAttributes);
                return null;
            }
        };

        try {
            executor.execute(callable);
        } catch (final Exception e) {
            throw new ApServiceException(String.format("Error update state of node status mo in transaction for Node %s", nodeFdn), e);
        }
    }
}
