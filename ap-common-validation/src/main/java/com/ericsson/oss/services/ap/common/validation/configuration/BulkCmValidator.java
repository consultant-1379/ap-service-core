/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.configuration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cm.bulkimport.api.ImportService;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification;

/**
 * Validate the bulkCm configuration files supplied for each node in a project using {@link ImportService}.
 */
@Stateless
public class BulkCmValidator {

    private static final long MAXIMUM_VALIDATION_TIME_IN_SECONDS = 300;

    @Inject
    private Logger logger;

    @EServiceRef
    private ImportService importService;

    private ExecutorService executor;

    /**
     * Validates a supplied {@link ArchiveArtifact} of a bulk import configuration file, using
     * {@link ImportService#validate(ImportValidationSpecification)}.
     * <p>
     * A max time period of {@link #MAXIMUM_VALIDATION_TIME_IN_SECONDS} seconds will be given for each bulkCmFile to complete its validation by
     * {@link ImportService}. If the task is not complete within {@link #MAXIMUM_VALIDATION_TIME_IN_SECONDS} seconds then the file will fail
     * validation.
     *
     * @param importSpecification
     *            Defines a specification for validation request.
     * @param configurationFile
     *            Name of the file for validation.
     * @return importServiceValidationResponse: Comprises of a List of violations (if any)
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object validateBulkCmFile(final ImportValidationSpecification importSpecification, final ArchiveArtifact configurationFile) {

        executor = getExecutorService();
        final Callable<Object> task = () -> importService.validate(importSpecification);

        final Future<Object> future = executor.submit(task);
        try {
            return future.get(MAXIMUM_VALIDATION_TIME_IN_SECONDS, TimeUnit.SECONDS);

        } catch (final TimeoutException e) {
            logger.warn("Validation timed out while attempting to validate BulkCmFile {} with exception {}", configurationFile.getName(),
                    e.getClass().getSimpleName(), e);
            return String.format("Validation of BulkCmFile %s has timed out, ImportService has not completed validation within %d seconds",
                    configurationFile.getName(), MAXIMUM_VALIDATION_TIME_IN_SECONDS);
        } catch (final Exception e) {
            logger.warn("Validation failed while attempting to validate BulkCmFile {} with exception {}", configurationFile.getName(),
                    e.getClass().getSimpleName(), e);
            return String.format("Validation failed while attempting to validate BulkCmFile %s with exception %s",
                    configurationFile.getName(), e.getMessage());
        } finally {
            future.cancel(true);
        }
    }

    /**
     * @return the maximumValidationTimeInSeconds
     */
    public static long getMaximumValidationTimeInSeconds() {
        return MAXIMUM_VALIDATION_TIME_IN_SECONDS;
    }

    private ExecutorService getExecutorService() {
        if (executor == null) {
            return Executors.newCachedThreadPool();
        } else {
            return executor;
        }
    }
}
