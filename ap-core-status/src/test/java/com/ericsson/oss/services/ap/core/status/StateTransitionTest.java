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
package com.ericsson.oss.services.ap.core.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Unit tests for {@link StateTransition}.
 */
public class StateTransitionTest {

    @Test
    public void whenEqualsOnTwoStateTransitions_andTransitionsAreEqual_thenTrueIsReturned() {
        final StateTransition first = new StateTransition("a", "aTob", "b");
        final StateTransition second = new StateTransition("a", "aTob", "b");
        assertEquals(first, second);
    }

    @Test
    public void whenEqualsOnTwoStateTransitions_andTransitionsAreNotEqual_thenFalseIsReturned() {
        final StateTransition first = new StateTransition("a", "aTob", "b");
        final StateTransition second = new StateTransition("a", "aToc", "c");
        assertNotEquals(first, second);
    }

    @Test
    public void whenEqualsOnTwoStateTransitions_andOneTransitionsIsNull_thenFalseIsReturned() {
        final StateTransition first = new StateTransition("a", "aTob", "b");
        assertNotEquals(first, null);
    }
}
