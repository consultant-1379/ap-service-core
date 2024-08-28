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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;

@RunWith(MockitoJUnitRunner.class)
public class ValidateProjectNameMatchesInNrmTest {

    private static final String ZIPFILE_NAME = "test.zip";

    private static final String PROJECTINFO_FILENAME = "projectInfo.xml";
    private static final String PROJECTNAME = "PROJECT_NAME";
    private static final String DUMMYPROJECTNAME = "DUMMY_PROJECT_NAME";
    private static final String PROJECTINFO_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<projectInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ProjectInfo.xsd\">" + "<name>"
            + PROJECTNAME + "</name></projectInfo>";

    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String NODE_FOLDER = "Folder1";
    private static final String NODENAME = "NODE_NAME";

    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";

    private static final String NODEINFO_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ReconfigureNodeInfo.xsd\">"
            + "<name>" + NODENAME + "</name>" + "<nodeIdentifier>NODE_VERSION</nodeIdentifier>" + "<nodeType>ERBS</nodeType>" + "<artifacts>"
            + "<siteBasic>SiteBasic.xml</siteBasic>" + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
            + "<siteEquipment>SiteEquipment.xml</siteEquipment>" + "<transport>TN_Data.xml</transport>" + "</artifacts>" + "</nodeInfo>";

    @InjectMocks
    @Spy
    private ValidateProjectNameMatchesInNrm validateProjectMatches;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private ManagedObject mo;

    @Mock
    private ManagedObject projectMo;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private Iterator<ManagedObject> iteratorMo;

    private ValidationContext context;

    private Map<String, Object> target;

    @Before
    public void setUp() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);
        zcg.createFileInZip(NODE_FOLDER, NODEINFO_FILENAME, NODEINFO_CONTENT);

        target = zcg.getZipData(ZIPFILE_NAME);
        when(dpsQueries.findMoByName(NODENAME, MoType.NODE.toString(), Namespace.AP.toString())).thenReturn(dpsQueryExecutor);
        when(iteratorMo.hasNext()).thenReturn(true);
        when(mo.getParent()).thenReturn(projectMo);
        when(iteratorMo.next()).thenReturn(mo);
        when(dpsQueryExecutor.execute()).thenReturn(iteratorMo);
    }

    @Test
    public void testRuleReturnTrueWhenNoProjectMoExists() {
        final ManagedObject enmNodeMo = Mockito.mock(ManagedObject.class);
        when(enmNodeMo.getParent()).thenReturn(projectMo);
        when(projectMo.getName()).thenReturn(PROJECTNAME);
        final ArrayList<ManagedObject> moList = new ArrayList<>();
        moList.add(enmNodeMo);
        final Iterator<ManagedObject> mos = moList.iterator();
        when(dpsQueryExecutor.execute()).thenReturn(mos);

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final boolean result = validateProjectMatches.execute(context);

        assertTrue(result);
    }

    @Test
    public void testRuleReturnTruedReconfigProjectMatches() {
        when(projectMo.getName()).thenReturn(PROJECTNAME);

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final boolean result = validateProjectMatches.execute(context);

        assertTrue(result);

    }

    @Test
    public void testRuleReturnFalseWhenReconfigProjectDoesNotMatches() {
        when(projectMo.getName()).thenReturn(DUMMYPROJECTNAME);

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final boolean result = validateProjectMatches.execute(context);
        assertFalse(result);

        final String errorMessage = String.format("Imported AP project: node %s has been managed by existing ENM AP project %s, it can not be managed by another project %s", NODENAME,
                DUMMYPROJECTNAME, PROJECTNAME);
        assertEquals(String.format("%s - %s", NODE_FOLDER, errorMessage), context.getValidationErrors().get(0));
    }
}