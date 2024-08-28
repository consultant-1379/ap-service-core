/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.testutils

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.model.HealthCheckAttribute;
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute

/**
 * Service to provide runtime dps capabilities without the need of boilerplate.
 *
 * This class requires RuntimeConfigurableDps and setup. If you are using this class,
 * please call the method setupPersistence within your test setup method, passing the
 * RuntimeConfigurableDps to it.
 *
 * E.g.: testPersistenceService.setupPersistence(cdiInjectorRule.getService(RuntimeConfigurableDps))
 */
class TestPersistenceService {

    public static final String NODE_STATUS_NAME = "1"

    @Inject
    private DpsOperations dpsOperations

    @Inject
    private DpsQueries dpsQueries

    @Inject
    private DataPersistenceService dataPersistenceService

    private modelData = new ModelData("ap", "2.0.0")

    void setupPersistence(RuntimeConfigurableDps dps, TransactionalExecutor executor) {
        dataPersistenceService = dps.build()
        Whitebox.setInternalState(dpsQueries, "dps", dataPersistenceService)
        Whitebox.setInternalState(dpsOperations, "dps", dataPersistenceService)
        Whitebox.setInternalState(dpsOperations, "executor", executor)
    }

    ManagedObject findByFdn(String moFdn) {
        return dataPersistenceService.getLiveBucket().findMoByFdn(moFdn)
    }

    ManagedObject persist(String type, String name, Map attributes, ManagedObject parent) {
        def mibRootBuilder = dataPersistenceService.getLiveBucket()
                .getMibRootBuilder()
                .namespace(modelData.getNameSpace())
                .version(modelData.getVersion())
                .type(type)
                .name(name)
                .addAttributes(attributes)

        if (parent != null) {
            mibRootBuilder.parent(parent)
        }

        return mibRootBuilder.create()
    }

    void createNodeStatus(ManagedObject nodeManagedObject, String state) {
        dataPersistenceService
                .getLiveBucket()
                .getManagedObjectBuilder()
                .type(MoType.NODE_STATUS.toString())
                .parent(nodeManagedObject)
                .name(NODE_STATUS_NAME)
                .addAttribute(NodeStatusAttribute.STATE.toString(), state)
                .create()
    }

    ManagedObject createHealthCheck(ManagedObject nodeManagedObject) {
        final Map<String, Object> healthCheckAttributes = new HashMap<String, Object>()
        final List<String> preHealthCheckAttributes = Arrays.asList("preReport1");
        final List<String> postHealthCheckAttributes = Arrays.asList("preReport2");
        healthCheckAttributes.put(HealthCheckAttribute.PRE_REPORT_IDS.toString(), preHealthCheckAttributes);
        healthCheckAttributes.put(HealthCheckAttribute.POST_REPORT_IDS.toString(), postHealthCheckAttributes);
        return dataPersistenceService.getLiveBucket()
                .getMibRootBuilder()
                .parent(nodeManagedObject)
                .namespace("AP")
                .version("1.0.0")
                .type(MoType.HEALTH_CHECK.toString())
                .name("1")
                .addAttributes(healthCheckAttributes)
                .create()
    }
}