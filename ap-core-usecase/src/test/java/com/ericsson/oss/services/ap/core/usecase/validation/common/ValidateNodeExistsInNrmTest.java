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

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeExistsInNrmTest {

    private static final String ZIPFILE_NAME = "test.zip";

    private static final String PROJECTINFO_FILENAME = "projectInfo.xml";
    private static final String PROJECTINFO_CONTENT = "testcontent";

    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String NODE_FOLDER = "Folder1";
    private static final String NODENAME = "NODE_NAME";

    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";

    private static final String NODEINFO_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
        + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"ReconfigureNodeInfo.xsd\">"
        + "<name>" + NODENAME + "</name>" + "<nodeIdentifier>NODE_VERSION</nodeIdentifier>" + "<nodeType>ERBS</nodeType>" + "<artifacts>"
        + "<siteBasic>SiteBasic.xml</siteBasic>" + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
        + "<siteEquipment>SiteEquipment.xml</siteEquipment>" + "<transport>TN_Data.xml</transport>" + "</artifacts>" + "</nodeInfo>";

    private final List<ManagedObject> emptyNodeMos = Collections.<ManagedObject> emptyList();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @InjectMocks
    @Spy
    private ValidateNodeExistsInNrm validateNodeExists;

    private ValidationContext context;

    private Map<String, Object> target;

    @Before
    public void setUp() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);
        zcg.createFileInZip(NODE_FOLDER, NODEINFO_FILENAME, NODEINFO_CONTENT);

        target = zcg.getZipData(ZIPFILE_NAME);
    }

    @Test
    public void testRuleReturnFalseWhenNodeDoesNotExistsInNrmDuringReconfiguration() {
        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(emptyNodeMos.iterator());

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final boolean result = validateNodeExists.execute(context);

        assertFalse(result);

        final String errorMessage = String.format("Node %s does not exist", NODENAME);
        assertEquals(String.format("%s - %s", NODE_FOLDER, errorMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void testRuleReturnTrueWhenNodeExistsInNrmDuringReconfiguration() {

        final ManagedObject mo = Mockito.mock(ManagedObject.class);
        final ArrayList<ManagedObject> moList = new ArrayList<>();
        moList.add(mo);
        final Iterator<ManagedObject> mos = moList.iterator();

        when(mo.getFdn()).thenReturn(NODE_FDN);
        when(dpsQueries.findMoByName(anyString(), eq(NETWORK_ELEMENT.toString()), eq(OSS_NE_DEF.toString()))).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(mos);

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final boolean result = validateNodeExists.execute(context);

        assertTrue(result);

    }
}
