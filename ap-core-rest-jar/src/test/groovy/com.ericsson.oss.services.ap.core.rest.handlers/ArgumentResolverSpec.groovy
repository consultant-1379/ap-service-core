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
package com.ericsson.oss.services.ap.core.rest.handlers

import static com.ericsson.oss.services.ap.common.model.MoType.NODE
import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException
import com.ericsson.oss.services.ap.common.cm.DpsQueries

import javax.inject.Inject

class ArgumentResolverSpec extends CdiSpecification {

    @ObjectUnderTest
    ArgumentResolver resolver

    @Inject
    private DpsQueries dpsQueries

    @Inject
    DataPersistenceService dataPersistenceService

    private RuntimeConfigurableDps dps

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = "Project=Project1,Node=Node1"
    private static final String USECASE_NAME = "AP Usecase"

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsQueries.dps = dataPersistenceService
    }

    def "Resolving fdn that exists should return no exception"() {
        given: "Project with child node created"
            final ManagedObject projectMO = addProjectMo(PROJECT_FDN)
            final ManagedObject nodeMO = addNodeMo(NODE_FDN, projectMO)

        when: "Resolver is called for created node fdn"
            resolver.resolveFdn(nodeMO.getFdn(), "RESUME")

        then: "no exception should be thrown"
            notThrown(Exception)
    }

    def "Resolving node fdn that does not exist should return NodeNotFoundException"() {
        given: "Create project MO (but not node MO)"
            addProjectMo(PROJECT_FDN)

        when: "Resolver is called for nodeFdn which has not been created"
            resolver.resolveFdn(NODE_FDN, "BIND")

        then: "NodeNotFoundException should be thrown"
            thrown(NodeNotFoundException)
    }

    def "Resolving project fdn that does not exist should return ProjectNotFoundException"() {
        when: "Resolver is called for non existent FDN - project does not exist"
            resolver.resolveFdn(PROJECT_FDN, USECASE_NAME)

        then: "validate if the corresponding service method was called once"
            thrown(ProjectNotFoundException)
    }

    private ManagedObject addProjectMo(final String fdn) {
        return dps.addManagedObject()
                .withFdn(fdn)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version("1.0.0")
                .build()
    }

    private ManagedObject addNodeMo(
            final String fdn, final ManagedObject parentMo) {
        ManagedObject apNodeMO = dps.addManagedObject()
                .withFdn(fdn)
                .type(NODE.toString())
                .namespace(AP.toString())
                .version("2.0.0")
                .parent(parentMo)
                .build()

        return apNodeMO
    }

}