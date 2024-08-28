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
package com.ericsson.oss.services.ap.core.rest.war.interceptor;

import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;
import com.ericsson.oss.services.ap.core.rest.war.response.ApResponseBuilder;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Interceptor to resolve fdn
 */
@Interceptor
@ResolveFdn
public class ResolveFdnInterceptor {

    private static final String PROJECT_ID = "{projectId}";
    private static final String NODE_ID = "{nodeId}";
    private static final Class<Path> PATH_CLASS = Path.class;

    @Inject
    private Logger logger;

    @Inject
    private ApResponseBuilder responseBuilder;

    @Inject
    private ArgumentResolver argumentResolver;

    /**
     * Reads class and method {@link Path}, builds fdn and resolves fdn while handling exceptions
     *
     * @param invocationContext Contextual information about a method invocation
     * @return Object Either to proceed to method or {@link Response} if exception thrown from {@link ArgumentResolver}
     * @throws Exception Exception
     */
    @AroundInvoke
    public Object aroundInvoke(final InvocationContext invocationContext) throws Exception {
        final Method method = invocationContext.getMethod();
        logger.debug(method.getName(), "Intercepting REST endpoint: {0} to resolve fdn");
        final Class<?> declaringClass = method.getDeclaringClass();
        final String usecase = method.getAnnotation(ResolveFdn.class).usecase().toString();

        if (noPathAnnotationsExist(declaringClass, method)) {
            logger.warn(method.getName(), "No Path annotations exist for {0}, proceeding to method");
            return invocationContext.proceed();
        }
        final String path = getPath(declaringClass, method);
        if (path.isEmpty()) {
            logger.warn(method.getName(), "No projectId in path for {0}, incorrect usage, proceeding to method");
            return invocationContext.proceed();
        }
        final String fdn = extractFdn(invocationContext.getParameters(), path);
        try {
            argumentResolver.resolveFdn(fdn, usecase);
            logger.debug(fdn, method.getName(), "Fdn: {0} resolved for {1}");
        } catch (Exception e) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(usecase, e);
            return generateResponse(errorResponse);
        }
        injectFdnIfRequired(invocationContext, fdn);
        return invocationContext.proceed();
    }

    private void injectFdnIfRequired(final InvocationContext invocationContext, final String fdn) {
        final Method method = invocationContext.getMethod();
        final Annotation[][] annotations = method.getParameterAnnotations();
        final Object[] parameters = invocationContext.getParameters();
        for (int i = 0; i < annotations.length; i++) {
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof InjectFdn) {
                    parameters[i] = fdn;
                }
            }
        }
        invocationContext.setParameters(parameters);
    }

    private String extractFdn(final Object[] parameters, final String path) {
        String fdn = "";
        if (path.contains(PROJECT_ID)) {
            fdn = buildApProjectFdn((String) parameters[0]);
        }
        if (path.contains(NODE_ID)) {
            fdn = buildApNodeFdn(fdn, (String) parameters[1]);
        }
        return fdn;
    }

    private String getPath(final Class<?> declaringClass, final Method method) {
        String path = "";
        if (classPathAnnotation(declaringClass)) {
            path = declaringClass.getAnnotation(PATH_CLASS).value();
        }
        if (methodPathAnnotation(method)) {
            path += method.getAnnotation(PATH_CLASS).value();
        }
        return path;
    }

    private boolean methodPathAnnotation(final Method method) {
        return method.isAnnotationPresent(PATH_CLASS);
    }

    private boolean classPathAnnotation(final Class<?> declaringClass) {
        return declaringClass.isAnnotationPresent(PATH_CLASS);
    }

    private boolean noPathAnnotationsExist(final Class<?> declaringClass, final Method method) {
        return !declaringClass.isAnnotationPresent(PATH_CLASS) && !method.isAnnotationPresent(PATH_CLASS);
    }

    private Response generateResponse(final ErrorResponse errorResponse) {
        return Response.status(errorResponse.getHttpResponseStatus())
            .entity(errorResponse).build();
    }

    private String buildApProjectFdn(final String projectId) {
        return String.format("Project=%s", projectId);
    }

    private String buildApNodeFdn(final String fdn, final String nodeId) {
        return fdn + ",Node=" + nodeId;
    }


}
