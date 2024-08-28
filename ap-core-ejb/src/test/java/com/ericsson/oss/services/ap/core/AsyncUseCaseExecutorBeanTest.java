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
package com.ericsson.oss.services.ap.core;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ericsson.oss.services.ap.core.usecase.EoiOrderProjectUseCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.OrderProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory;

/**
 * Unit tests for {@link AsyncUseCaseExecutorBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AsyncUseCaseExecutorBeanTest {

    private static final String USER_ID = "userId";
    private static final String BASE_URL = "https://athtem.eei.ericsson.se";
    private static final String SESSION_ID = "hkdjlDKJ";
    private static final boolean VALIDATION_REQUIRED = true;

    @Mock
    private ContextService contextService;

    @Mock
    private UseCaseFactory usecaseFactory;

    @Mock
    private OrderProjectUseCase orderProjectUsecase;

    @Mock
    private EoiOrderProjectUseCase eoiOrderProjectUseCase;

    @Mock
    private SystemRecorder recorderMock; //NOPMD

    @InjectMocks
    private AsyncUseCaseExecutorBean sut;

    @Before
    public void setUp() {
        when(usecaseFactory.getNamedUsecase(UseCaseName.ORDER_PROJECT)).thenReturn(orderProjectUsecase);
        when(usecaseFactory.getNamedUsecase(UseCaseName.EOI_ORDER_PROJECT)).thenReturn(eoiOrderProjectUseCase);
    }

    @Test
    public void testOrderUsecaseIsSuccessfullyInvoked() {
        sut.orderProject(PROJECT_FDN, USER_ID, VALIDATION_REQUIRED);
        verify(orderProjectUsecase).execute(PROJECT_FDN, VALIDATION_REQUIRED, null);
    }

    @Test
    public void testUserIdContextIsSetBeforeExecutingTheOrderProjectUseCase() {
        sut.orderProject(PROJECT_FDN, USER_ID, VALIDATION_REQUIRED);
        verify(contextService).setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, USER_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void testExecuteMethodDoesntCatchOrWrapException() {
        doThrow(IllegalStateException.class).when(orderProjectUsecase).execute(PROJECT_FDN, VALIDATION_REQUIRED, null);
        sut.orderProject(PROJECT_FDN, USER_ID, VALIDATION_REQUIRED);
    }

    @Test
    public void testEoiOrderUsecaseIsSuccessfullyInvoked() {
        sut.eoiOrderProject(PROJECT_FDN, USER_ID, BASE_URL, SESSION_ID);
        verify(eoiOrderProjectUseCase).execute(PROJECT_FDN, BASE_URL, SESSION_ID);
    }

}
