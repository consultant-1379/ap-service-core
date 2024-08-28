/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.MoType.CONFIGURATION_PROFILE;
import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.ProjectAttribute.CREATION_DATE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.node.Node;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.core.usecase.view.ProjectData;

/**
 * Unit tests for {@link ViewAllProjectsUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewAllProjectsUseCaseTest {

    private static final String VERSION_NUMBER = "1.0.0";
    private static final String ATTR_1 = "attr1";
    private static final String ATTR_2 = "attr2";
    private static final String VALUE_1 = "value1";
    private static final String VALUE_2 = "value2";
    private static final String PROJECT_1_FDN = "Project=Project1";
    private static final String PROJECT_2_FDN = "Project=Project2";
    private static final String PROJECT_3_FDN = "Project=Project3";

    @InjectMocks
    private DpsQueries dpsQueries;

    @InjectMocks
    private ViewAllProjectsUseCase viewUseCase;

    private RuntimeConfigurableDps configurableDps;

    @InjectMocks
    private ProjectData projectData;

    @Before
    public void setUp() {
        configurableDps = new RuntimeConfigurableDps();
        final DataPersistenceService dps = configurableDps.build();
        Whitebox.setInternalState(viewUseCase, "dpsQueries", dpsQueries);
        Whitebox.setInternalState(dpsQueries, "dps", dps);
    }

    @Test
    public void when_no_projects_exist_then_return_empty_list() {
        final List<MoData> foundProjectMos = viewUseCase.execute();
        assertTrue("No projects should be found", foundProjectMos.isEmpty());
    }

    @Test
    public void whenMultipleProjectsExistThenTheReturnedProjectsAreSortedByCreationDate() {
        addProjectMo(PROJECT_1_FDN, "2018-08-30 00:00:00");
        addProjectMo(PROJECT_2_FDN, "2018-08-29 00:00:00");
        addProjectMo(PROJECT_3_FDN, "2018-08-30 00:20:00");

        final List<MoData> foundProjectMos = viewUseCase.execute();
        assertEquals(PROJECT_2_FDN, foundProjectMos.get(0).getFdn());
        assertEquals(PROJECT_1_FDN, foundProjectMos.get(1).getFdn());
        assertEquals(PROJECT_3_FDN, foundProjectMos.get(2).getFdn());
    }

    @Test
    public void when_project_exists_then_the_returned_project_contains_all_project_attributes() {
        addProjectMo(PROJECT_1_FDN);

        final MoData foundProjectMo = viewUseCase.execute().iterator().next();
        assertEquals(VALUE_1, foundProjectMo.getAttribute(ATTR_1));
        assertEquals(VALUE_2, foundProjectMo.getAttribute(ATTR_2));
    }

    @Test
    public void when_three_nodes_in_project_then_the_returned_project_contains_the_node_quantity() {
        final String projFdn = PROJECT_1_FDN;
        final ManagedObject projectMo = addProjectMo(projFdn);

        addNodeMo(projFdn + ",Node=Node1", projectMo);
        addNodeMo(projFdn + ",Node=Node2", projectMo);
        addNodeMo(projFdn + ",Node=Node3", projectMo);

        final MoData foundProjectMo = viewUseCase.execute().iterator().next();
        assertEquals("3", foundProjectMo.getAttribute("nodeQuantity"));
    }

    @Test
    public void whenProjectContainsNodesThenTheNodeDataIsValid() {

        final ManagedObject projectMo = addProjectMo(PROJECT_1_FDN);

        addNodeMo(PROJECT_1_FDN + ",Node=Node1", projectMo);
        addNodeMo(PROJECT_1_FDN + ",Node=Node2", projectMo);

        final MoData foundProjectMo = viewUseCase.execute().iterator().next();
        @SuppressWarnings("unchecked")
        final List<Node> nodeMos = (List<Node>) foundProjectMo.getAttribute("nodes");
        assertEquals("Node1", nodeMos.get(0).getId());
        assertEquals("Project1", nodeMos.get(1).getParent());
    }

    @Test
    public void whenViewProjectContainsIntegrationProfileDatatypeThenReturnNodeDataList() {

        ManagedObject projectMo = addProjectMo(PROJECT_1_FDN);

        addIntegrationProfileMO(PROJECT_FDN + ",ConfigurationProfile=Profile1",projectMo);

       viewUseCase.execute().iterator().next();

    }

    @Test
    public void whenViewProjectContainsExpansionProfileDatatypeThenReturnNodeDataList() {

        ManagedObject projectMo = addProjectMo(PROJECT_1_FDN);
        addExpansionProfileMO(PROJECT_FDN + ",ConfigurationProfile=Profile1",projectMo);

        viewUseCase.execute().iterator().next();

    }

    private void addIntegrationProfileMO(final String fdn, final ManagedObject parentMo) {

        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put(ProfileAttribute.DATATYPE.toString(), "INTEGRATION");
        nodeAttributes.put(ProfileAttribute.PROFILE_ID.toString(), "Prof1");
        configurableDps.addManagedObject()
            .withFdn(fdn)
            .type(CONFIGURATION_PROFILE.toString())
            .namespace(AP.toString())
            .version("1.0.0")
            .parent(parentMo)
            .addAttributes(nodeAttributes)
            .build();
    }

    private void addExpansionProfileMO(final String fdn, final ManagedObject parentMo) {

        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put(ProfileAttribute.DATATYPE.toString(), "EXPANSION");
        nodeAttributes.put(ProfileAttribute.PROFILE_ID.toString(), "Prof1");
        configurableDps.addManagedObject()
           .withFdn(fdn)
           .type(CONFIGURATION_PROFILE.toString())
           .namespace(AP.toString())
           .version("1.0.0")
           .parent(parentMo)
           .addAttributes(nodeAttributes)
           .build();
   }

    private void addNodeMo(final String fdn, final ManagedObject parentMo) {
        configurableDps.addManagedObject()
                .withFdn(fdn)
                .type(NODE.toString())
                .namespace(AP.toString())
                .version(VERSION_NUMBER)
                .parent(parentMo)
                .build();
    }

    private ManagedObject addProjectMo(final String fdn) {
        final Map<String, Object> projectAttributes = new HashMap<>();
        projectAttributes.put(ATTR_1, VALUE_1);
        projectAttributes.put(ATTR_2, VALUE_2);

        return configurableDps.addManagedObject()
                .withFdn(fdn)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version(VERSION_NUMBER)
                .addAttributes(projectAttributes)
                .build();
    }

    private ManagedObject addProjectMo(final String fdn, final String creationDate) {
        final Map<String, Object> projectAttributes = new HashMap<>();
        projectAttributes.put(ATTR_1, VALUE_1);
        projectAttributes.put(ATTR_2, VALUE_2);
        projectAttributes.put(CREATION_DATE.toString(), creationDate);

        return configurableDps.addManagedObject()
            .withFdn(fdn)
            .type(PROJECT.toString())
            .namespace(AP.toString())
            .version(VERSION_NUMBER)
            .addAttributes(projectAttributes)
            .build();
    }

}