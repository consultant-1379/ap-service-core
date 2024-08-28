/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link ExceptionMapperFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperFactoryTest {

    @Mock
    private ExceptionMapper<Throwable> defaultExceptionMapper;

    @Mock
    private Instance<ExceptionMapper<Throwable>> exceptionMappers;

    @InjectMocks
    private ExceptionMapperFactory exceptionMapperFactory;

    @Before
    public void setUp() {
        final List<ExceptionMapper<Throwable>> exceptionMappersList = new ArrayList<>();
        when(exceptionMappers.iterator()).thenReturn(exceptionMappersList.iterator());
    }

    @Test
    public void whenExceptionMapperFactoryCalled_andExceptionHasNoMatchingMapper_thenDefaultMapperIsReturned() {
        final ExceptionMapper<Throwable> result = exceptionMapperFactory.find(new IllegalStateException());
        assertEquals(defaultExceptionMapper, result);
    }
}
