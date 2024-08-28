/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

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

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Unit tests for {@link UseCaseFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UseCaseFactoryTest {

    @Mock
    private Instance<Object> instance;

    @InjectMocks
    private final UseCaseFactory usecaseFactory = new UseCaseFactory();

    private final OrderUseCase orderUseCase = new OrderUseCase();
    private final ViewUseCase viewUseCase = new ViewUseCase();
    private final List<Object> usecases = new ArrayList<>();

    @Before
    public void setUp() {
        usecases.add(orderUseCase);
        usecases.add(viewUseCase);
        when(instance.iterator()).thenReturn(usecases.iterator());
    }

    @Test
    public void test_factory_returns_instance_of_orderNode_usecase() {
        assertEquals(orderUseCase, usecaseFactory.getNamedUsecase(UseCaseName.ORDER_NODE));
    }
    @Test
    public void testFactoryReturnsInstanceOfViewNodeUsecase() {
        assertEquals(viewUseCase, usecaseFactory.getNamedUsecase(UseCaseName.VIEW_NODE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_factory_throws_IllegalArgumentException_when_no_usecase_found_with_specified_name() {
        usecaseFactory.getNamedUsecase(UseCaseName.IMPORT);
    }

    @UseCase(name = UseCaseName.ORDER_NODE)
    class OrderUseCase {

        public String execute() {
            return "Ordered";
        }

    }

    @UseCase(name = UseCaseName.VIEW_NODE)
    class ViewUseCase {

        public String execute() {
            return "Viewed";
        }
    }

}
