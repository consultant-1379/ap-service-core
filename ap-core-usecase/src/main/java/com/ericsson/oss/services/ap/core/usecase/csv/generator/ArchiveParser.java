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
package com.ericsson.oss.services.ap.core.usecase.csv.generator;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Parses all the valid substitution variables from Archive.
 */
public class ArchiveParser {

    //Pattern for String starting and ending with '%'
    private static final String VALID_SUBSTITUTION_VARIABLE_PATTERN = "(%.{1,}?[^%]%)";
    private static final String RESERVED_TAG_PREFIX = "internal_";
    private static final char[] UNACCEPTED_CHARACTERS = { '%', '*', '&' };

    /**
     * Processes all the {@link ArchiveArtifact} and returns a list of matching values satisfying valid Substitution variables.
     *
     * @param archiveArtifacts
     *            the list of archiveArtifacts
     * @return the substitution variables from each archiveArtifact
     */
    public Set<String> parse(final List<ArchiveArtifact> archiveArtifacts) {
        final LinkedHashSet<String> substitutionVariables = new LinkedHashSet<>();
        final Pattern substitutionVariablePattern = Pattern.compile(VALID_SUBSTITUTION_VARIABLE_PATTERN, Pattern.CASE_INSENSITIVE);
        for (final ArchiveArtifact nodeInfoArtifact : archiveArtifacts) {
            if (nodeInfoArtifact.getName().equalsIgnoreCase(NODEINFO.toString())) {
                addSubstitutionVariables(substitutionVariables, substitutionVariablePattern, nodeInfoArtifact);
            }
        }
        for (final ArchiveArtifact artifact : archiveArtifacts) {
            if (!artifact.getName().equalsIgnoreCase(NODEINFO.toString())) {
                addSubstitutionVariables(substitutionVariables, substitutionVariablePattern, artifact);
            }
        }
        return substitutionVariables;
    }

    private void addSubstitutionVariables(final Set<String> substitutionVariables, final Pattern substitutionVariablePattern,
        final ArchiveArtifact artifact) {
        final Matcher substitutionVariableMatcher = substitutionVariablePattern.matcher(artifact.getContentsAsString());
        while (substitutionVariableMatcher.find()) {
            String substitutionVariable = substitutionVariableMatcher.group();
            //Removing first and last % character from match
            substitutionVariable = substitutionVariable.substring(1, substitutionVariable.length() - 1).trim();
            if (shouldParse(substitutionVariable)) {
                substitutionVariables.add(substitutionVariable);

            }
        }
    }

    private boolean shouldParse(final String variableName) {
        return !(variableName.toLowerCase(Locale.US).startsWith(RESERVED_TAG_PREFIX) || StringUtils.containsAny(variableName, UNACCEPTED_CHARACTERS));
    }
}
