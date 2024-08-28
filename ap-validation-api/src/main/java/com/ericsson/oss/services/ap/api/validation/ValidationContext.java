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
package com.ericsson.oss.services.ap.api.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * Identifies which {@link ValidationRule} are required to execute on the target.
 */
public class ValidationContext {

    private final String group;
    private final Object target;

    private final List<String> validationErrors = new ArrayList<>();
    private final Set<String> nodeValidationErrors = new TreeSet<>();

    /**
     * Constructs an instance of {@link ValidationContext}.
     *
     * @param group
     *            the validation rule group
     * @param target
     *            the target of the validation
     */
    public ValidationContext(final String group, final Object target) {
        this.group = group;
        this.target = target;
    }

    /**
     * The validation rule group.
     *
     * @return the validation group
     */
    public String getGroup() {
        return group;
    }

    /**
     * The target of the validation.
     *
     * @return the validation target
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Adds a validation error to the context.
     *
     * @param errorMessage
     *            the error message to add
     */
    public void addValidationError(final String errorMessage) {
        validationErrors.add(errorMessage);
    }

    /**
     * Adds a validation error which occured on in a node directory (either the <code>nodeInfo</code> file, or one of the node artifact files) to the
     * context.
     * <p>
     * Prefixes the error message with the directory name.
     *
     * @param error
     *            the error message to add
     * @param directory
     *            the node directory in which this error occurred
     */
    public void addNodeValidationError(final String error, final String directory) {
        nodeValidationErrors.add(String.format("%s - %s", directory, error));
    }

    public void addValidationErrorMessage(final String error){
        nodeValidationErrors.add(String.format("%s",error));
    }
    

    /**
     * Returns a list of all validation errors in the context.
     *
     * @return a list of all validation errors.
     */
    public List<String> getValidationErrors() {
        final List<String> allErrors = new ArrayList<>();
        allErrors.addAll(validationErrors);
        allErrors.addAll(nodeValidationErrors);
        return Collections.unmodifiableList(allErrors);
    }
}
