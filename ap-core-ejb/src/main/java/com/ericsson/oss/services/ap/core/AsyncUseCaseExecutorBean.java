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

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.EoiOrderProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.OrderProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;

/**
 * Asynchronously executes a given usecase.
 */
@Stateless
public class AsyncUseCaseExecutorBean {

    @Inject
    private UseCaseFactory usecaseFactory;

    @Inject
    private ContextService contextService;

    /**
     * Asynchronous execution of {@link OrderProjectUseCase} in order to propagate the user ID to the {@link ContextService}. Context is not
     * propagated to to methods annotated with {@link Asynchronous}, so it must be set explicitly.
     *
     * @param projectFdn
     *            the FDN of the project
     * @param userId
     *            the user ID which will be set in the context
     * @param validationRequired
     *            is validation required
     */
    @Asynchronous
    public void orderProject(final String projectFdn, final String userId, final boolean validationRequired) {
        this.orderProject(projectFdn, userId, validationRequired, null);
    }

    /**
     * Asynchronous execution of {@link OrderProjectUseCase} in order to propagate the user ID to the {@link ContextService}. Context is not
     * propagated to to methods annotated with {@link Asynchronous}, so it must be set explicitly.
     *
     * @param projectFdn
     *            the FDN of the project
     * @param userId
     *            the user ID which will be set in the context
     * @param validationRequired
     *            is validation required
     * @param projectInfo
     *            project info parsed from project.zip
     */
    @Asynchronous
    public void orderProject(final String projectFdn, final String userId, final boolean validationRequired, final ProjectInfo projectInfo) {
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);

        final OrderProjectUseCase usecase = usecaseFactory.getNamedUsecase(UseCaseName.ORDER_PROJECT);
        usecase.execute(projectFdn, validationRequired, projectInfo);
    }


    /* for EOI based nodes*/

    @Asynchronous
    public void eoiOrderProject(final String projectFdn, final String userId, final String baseUrl, final String sessionId){
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
        EoiOrderProjectUseCase usecase = usecaseFactory.getNamedUsecase(UseCaseName.EOI_ORDER_PROJECT);
        usecase.execute(projectFdn, baseUrl, sessionId);
    }
}
