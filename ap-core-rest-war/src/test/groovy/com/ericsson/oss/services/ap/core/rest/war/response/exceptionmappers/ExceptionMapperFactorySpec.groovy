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

package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers

import static org.junit.Assert.assertEquals

import javax.enterprise.inject.Instance
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.Response

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ValidationException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

/**
 * Unit tests for {@link ExceptionMapperFactory}.
 */
class ExceptionMapperFactorySpec extends CdiSpecification {

    @MockedImplementation
    private ExceptionMapper<? extends Throwable> defaultExceptionMapper

    @MockedImplementation
    private Instance<ExceptionMapper<Throwable>> exceptionMappers

    @ObjectUnderTest
    private ExceptionMapperFactory exceptionMapperFactory

    def testExceptionMapper

    def setup() {
        final TestThrowableMapper testMapper = new TestThrowableMapper()

        final List<ExceptionMapper<Throwable>> exceptionMappersList = new ArrayList<>()
        exceptionMappersList.add(testMapper)
        exceptionMappersList.add(new ValidationExceptionMapper())
        exceptionMappers.iterator() >> exceptionMappersList.iterator()

        // Needs to be called again because @PostConstruct runs before the test setup
        exceptionMapperFactory.cacheExceptionMappers()
    }

    def "When exception mapper factory is called and exception has no matching mapper then default mapper is returned"() {

        when: "Exception mapper tries to find exception"
        final ExceptionMapper<Throwable> result = exceptionMapperFactory.find(new IllegalStateException())

        then: "Default exception mapper is returned"
        assertEquals(defaultExceptionMapper, result)
    }

    def "When exception mapper factory is called then the mapper for the given exception must be returned"() {

        when: "Exception mapper tries to find TestException"
        testExceptionMapper = exceptionMapperFactory.find(new TestThrowable())

        then: "TestThrowableMapper must have been returned"
        testExceptionMapper instanceof TestThrowableMapper
        Response response = testExceptionMapper.toResponse(null)

        response.status == Response.Status.BAD_REQUEST.statusCode
        response.getEntity() instanceof TestErrorResponse
    }

    def "When exception mapper factory is called for ValidationException then the ValidationExceptionMapper must be returned"() {

        when: "Exception mapper tries to find TestException"
        ValidationException ex = new ValidationException("TestError")
        testExceptionMapper = exceptionMapperFactory.find(ex)

        then: "TestThrowableMapper must have been returned"
        testExceptionMapper instanceof ValidationExceptionMapper
        Response response = testExceptionMapper.toResponse(ex)

        response.status == HttpServletResponse.SC_EXPECTATION_FAILED
    }
}

class TestThrowable extends Throwable {
}

class TestErrorResponse extends ErrorResponse {
}

class TestThrowableMapper extends RestExceptionMapper
        implements ExceptionMapper<TestThrowable>, javax.ws.rs.ext.ExceptionMapper<TestThrowable> {

    @Override
    ErrorResponse toErrorResponse(TestThrowable exception, String additionalInformation) {
        return new TestErrorResponse()
    }

    @Override
    Response toResponse(TestThrowable testThrowable) {
        return entityWithStatus(Response.Status.BAD_REQUEST, new TestErrorResponse())
    }
}

