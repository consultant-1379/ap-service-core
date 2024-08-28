/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.builder

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.core.rest.model.Project

class ProjectDataBuilderSpec extends CdiSpecification {

    @ObjectUnderTest
    private ProjectDataBuilder projectDataBuilderSpec

    private static final String PROJECT_NAME = "project1"
    private static final String CREATOR = "creator"
    private static final String DESC = "project desc"
    private static final String INTEGRATION_PROFILE = "integrationProfile"
    private static String fdn

    private static MoData moData
    private static Project project
    private Project data

    def setupSpec() {
        fdn = String.format("Project=%s", PROJECT_NAME)
        Map<String, Object> attributes = [
                "projectName"       : PROJECT_NAME,
                "creationDate"      : new Date(2014, 02, 11).toString(),
                "creator"           : CREATOR,
                "description"       : DESC,
                "generatedby"       : null,
                "integrationProfile": INTEGRATION_PROFILE,
                "expansionProfile"  : null,
                "nodes"             : null,
                "children"           : 2


        ]
        final Map<String, Object> projectAttributes = new LinkedHashMap<>()
        projectAttributes.putAll(new TreeMap<>(attributes))
        moData = new MoData(fdn, projectAttributes, MoType.PROJECT.toString(), null)
    }

    def "Build project data successfully"() {
        given: "The build project service will return mock MoData"
        Map<String,Object> map = new HashMap<>();
        map.put("id",PROJECT_NAME);
        map.put("creationDate",new Date(2014, 02, 11).toString());
        map.put("creator", CREATOR);
        map.put("projectNodeQuantity", 0);
        map.put("integrationProfile", INTEGRATION_PROFILE);
        map.put("nodes", null);
        map.put("children",2)
        project = new Project(map)

        when: "the build profile data is called"
        data = projectDataBuilderSpec.buildProject(moData)

        then: "Profile data returned"
        map.get("creator") == CREATOR
    }
}
