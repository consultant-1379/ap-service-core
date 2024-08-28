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
package com.ericsson.oss.services.ap.common.util.string;

import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link FDN}.
 */
public class FDNTest {

    private static final String MECONTEXT_1 = "MeContext=1";
    private static final String MANAGEDELEMENT_2 = MECONTEXT_1 + ",ManagedElement=2";
    private static final String ENODEBFUNCTION_3 = MANAGEDELEMENT_2 + ",ENodeBFunction=3";
    private static final String RBSCONFIGURATION_4 = ENODEBFUNCTION_3 + ",RbsConfiguration=4";
    private static final String NODE_FDN = "Project=project1,Node=Node1";
    private static final String NODE_FDN1 = "MeContext=LTE001";
    private static final String NODE_FDN2 = "ManagedElement=LTE0012";
    private static final String NODE_FDN3 = "NetworkElement=LTE00123";

    @Test
    public void whenInvalidFdnThenGetThrowsException() {
        int caughtExceptionCount = 0;
        final ArrayList<String> invalidFDNs = new ArrayList<>();
        invalidFDNs.add("MeContext=1,ManagedElement");
        invalidFDNs.add("MeContext");
        invalidFDNs.add("MeContext,");
        invalidFDNs.add("MeContext=1,");
        invalidFDNs.add("MeContext=1,ManagedElement=1,");
        invalidFDNs.add(",");
        invalidFDNs.add("");
        invalidFDNs.add(null);

        for (final String inputString : invalidFDNs) {
            try {
                FDN.get(inputString);
            } catch (final IllegalArgumentException e) {
                caughtExceptionCount++;
            }
        }
        assertEquals(invalidFDNs.size(), caughtExceptionCount);
    }

    @Test
    public void whenValidFdnThenGetRdnReturnsTheRdn() {
        assertEquals("MeContext=1", FDN.get(MECONTEXT_1).getRdn());
        assertEquals("ManagedElement=2", FDN.get(MANAGEDELEMENT_2).getRdn());
    }

    @Test
    public void whenFdnContainsChildrenThenGetParentReturnsTheParentFdn() {
        assertEquals(ENODEBFUNCTION_3, FDN.get(RBSCONFIGURATION_4).getParent());
        assertEquals(MANAGEDELEMENT_2, FDN.get(ENODEBFUNCTION_3).getParent());
        assertEquals(MECONTEXT_1, FDN.get(MANAGEDELEMENT_2).getParent());
    }

    @Test
    public void whenRootFdnThenGetParentReturnsNull() {
        assertEquals(null, FDN.get(MECONTEXT_1).getParent());
    }

    @Test
    public void whenValidFdnThenGetTypeReturnsTheType() {
        assertEquals("MeContext", FDN.get(MECONTEXT_1).getType());
        assertEquals("ManagedElement", FDN.get(MANAGEDELEMENT_2).getType());
        assertEquals("ENodeBFunction", FDN.get(ENODEBFUNCTION_3).getType());
        assertEquals("RbsConfiguration", FDN.get(RBSCONFIGURATION_4).getType());
    }

    @Test
    public void whenValidFdnThenGetRdnValueReturnsTheRdnName() {
        assertEquals("1", FDN.get(MECONTEXT_1).getRdnValue());
        assertEquals("2", FDN.get(MANAGEDELEMENT_2).getRdnValue());
        assertEquals("3", FDN.get(ENODEBFUNCTION_3).getRdnValue());
        assertEquals("4", FDN.get(RBSCONFIGURATION_4).getRdnValue());
    }

    @Test
    public void whenFdnContainsChildrenThenGetRootReturnsTheRoot() {
        assertEquals(MECONTEXT_1, FDN.get(MECONTEXT_1).getRoot());
        assertEquals(MECONTEXT_1, FDN.get(MANAGEDELEMENT_2).getRoot());
        assertEquals(MECONTEXT_1, FDN.get(ENODEBFUNCTION_3).getRoot());
        assertEquals(MECONTEXT_1, FDN.get(RBSCONFIGURATION_4).getRoot());
    }

    @Test
    public void whenFdnContainsTypeThenGetRdnValueOfTypeReturnsTheRdnValue() {
        assertEquals("1", FDN.get(MECONTEXT_1).getRdnValueOfType("MeContext"));
        assertEquals("1", FDN.get(RBSCONFIGURATION_4).getRdnValueOfType("MeContext"));
        assertEquals("2", FDN.get(RBSCONFIGURATION_4).getRdnValueOfType("ManagedElement"));
        assertEquals("3", FDN.get(RBSCONFIGURATION_4).getRdnValueOfType("ENodeBFunction"));
        assertEquals("4", FDN.get(RBSCONFIGURATION_4).getRdnValueOfType("RbsConfiguration"));
    }

    @Test
    public void whenFdnDoesNotContainTypeThenGetRdnValueOfTypeReturnsNull() {
        assertEquals(null, FDN.get(MECONTEXT_1).getRdnValueOfType("ManagedElement"));
    }

    @Test
    public void whenValidFdnThenGetNodeNameReturnsTheNodeName(){
        assertEquals("LTE001", FDN.get(NODE_FDN1).getNodeName());
        assertEquals("LTE0012", FDN.get(NODE_FDN2).getNodeName());
        assertEquals("LTE00123", FDN.get(NODE_FDN3).getNodeName());
        assertNull(FDN.get(NODE_FDN).getNodeName());
    }

    @Test
    public void whenValidFdnThenGetProjectNameReturnsTheProjectName(){
        assertEquals("project1", FDN.get(NODE_FDN).getProjectName());
    }
}
