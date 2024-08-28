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
package com.ericsson.oss.services.ap.common.artifacts.util;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.smrs.SmrsAccount;
import com.ericsson.oss.itpf.smrs.SmrsService;

import javax.inject.Inject;

/**
 * Unit tests for {@link SmrsAccountOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SmrsAccountOperationsTests {

    private static final String SMRS_ACCOUNT_TYPE = "AI";

    @Mock
    private SmrsService smrsService;

    @Mock
    private SmrsAccount smrsAccount;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private SmrsAccountOperations smrsAccountOperations;

    @Test
    public void whenGetSmrsAccountThenTheAccountIsReturned() {
        when(smrsService.getNodeSpecificAccount(SMRS_ACCOUNT_TYPE, VALID_NODE_TYPE, NODE_NAME)).thenReturn(smrsAccount);
        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn("ERBS");
        final SmrsAccount resultSmrsAccount = smrsAccountOperations.getSmrsAccount(NODE_NAME, VALID_NODE_TYPE);
        assertEquals(smrsAccount, resultSmrsAccount);
    }

    @Test
    public void whenDeleteSmrsAccountThenTheAccountIsDeleted() {
        when(smrsService.getNodeSpecificAccount(SMRS_ACCOUNT_TYPE, VALID_NODE_TYPE, NODE_NAME)).thenReturn(smrsAccount);
        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn("ERBS");
        smrsAccountOperations.deleteSmrsAccount(NODE_NAME, VALID_NODE_TYPE);
        verify(smrsService).deleteSmrsAccount(smrsAccount);
    }
}
