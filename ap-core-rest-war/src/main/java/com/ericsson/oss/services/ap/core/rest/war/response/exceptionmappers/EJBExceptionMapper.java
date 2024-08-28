/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maps {@link EJBException} with uncaught EJB errors to appropriate http response.
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    @Context
    private Providers providers;

    @Inject
    @DefaultExceptionMapper
    private ExceptionMapper<Throwable> defaultExceptionMapper;

    private final Logger logger = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

    @Override
    public Response toResponse(EJBException ejbException) {
        final Throwable causeThrowable = ejbException.getCause();
        ExceptionMapper mapper = providers.getExceptionMapper(causeThrowable.getClass());

        if (mapper != null) {
            return mapper.toResponse(causeThrowable);
        }

        logger.info("Could not find a suitable mapper for {}, using default",
            causeThrowable.getClass().getName());

        return defaultExceptionMapper.toResponse(causeThrowable);
    }
}
