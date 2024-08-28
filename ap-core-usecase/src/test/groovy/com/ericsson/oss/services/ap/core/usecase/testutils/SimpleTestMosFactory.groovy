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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*

import javax.inject.Inject

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.util.string.FDN

/**
 * Factory to create and provide simple test MOs.
 *
 * This class requires RuntimeConfigurableDps and setup. If you are using this class,
 * please call the method setupPersistence within your test setup method, passing the
 * RuntimeConfigurableDps to it.
 *
 * If you are using TestPersistenceService on your test and already setup persistence
 * on it, you don't need to call this class' setupPersistence method.
 */
class SimpleTestMosFactory {

    public static final String PROFILE_NAME = "MyTestProfile"
    public static final String PROFILE_FDN = PROJECT_FDN + ",ConfigurationProfile=" + PROFILE_NAME

    @Inject
    private TestPersistenceService testPersistenceService

    void setupPersistence(RuntimeConfigurableDps dps) {
        testPersistenceService.setupPersistence(dps)
    }

    ManagedObject newProject() {
        return testPersistenceService.persist(MoType.PROJECT.toString(), PROJECT_NAME, Collections.emptyMap(), null)
    }

    ManagedObject newTestNode(ManagedObject projectManagedObject, Map attributes) {
        return newTestNodeWithName(NODE_FDN, projectManagedObject, attributes)
    }

    ManagedObject newTestNodeWithName(String nodeFdn, ManagedObject projectManagedObject, Map attributes) {
        return testPersistenceService.persist(MoType.NODE.toString(), FDN.get(nodeFdn).getRdnValue(), attributes, projectManagedObject)
    }

    ManagedObject newEmptyProfile(ManagedObject projectManagedObject) {
        return testPersistenceService.persist(MoType.CONFIGURATION_PROFILE.toString(), PROFILE_FDN, Collections.emptyMap(), projectManagedObject)
    }
}
