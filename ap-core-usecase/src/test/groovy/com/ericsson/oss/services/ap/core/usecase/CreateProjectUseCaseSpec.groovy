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
package com.ericsson.oss.services.ap.core.usecase

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ProjectExistsException
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.api.model.node.Node
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.Namespace
import com.ericsson.oss.services.ap.common.model.access.ModelReader

class CreateProjectUseCaseSpec extends CdiSpecification {

    private static final String NAME = "Project1"
    private static final String USERNAME = "user"
    private static final String DESCRIPTION = "some description"

    @ObjectUnderTest
    private CreateProjectUseCase createProjectUseCase

    @MockedImplementation
    private ModelReader modelReader


    def setup() {
        modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.PROJECT.toString()) >> new ModelData("ap", "2.0.0")
    }

    def "when create project usecase is executed then project mo is created"() {
        when: "create project usecase is executed"
        def moData = createProjectUseCase.execute(NAME, USERNAME, DESCRIPTION)

        then: "project mo created is returned"
        moData.getFdn() == "Project=" + NAME
        moData.getAttribute("projectName") == NAME
        moData.attributes.get("description") == DESCRIPTION
        moData.attributes.get("creator") == USERNAME

        and: "project is empty containing no nodes"
        moData.getAttribute("nodeQuantity") == "0"
        ((List<Node>) moData.getAttribute("nodes")).size() == 0

    }

    def "when project already exists then exception is thrown"() {
        given: "project has already being created with the same name"
        createProjectUseCase.execute(NAME, USERNAME, DESCRIPTION)

        when: "create project usecase is executed"
        createProjectUseCase.execute(NAME, USERNAME, DESCRIPTION)

        then: "ProjectExistsException is thrown"
        thrown(ProjectExistsException)

    }

}
