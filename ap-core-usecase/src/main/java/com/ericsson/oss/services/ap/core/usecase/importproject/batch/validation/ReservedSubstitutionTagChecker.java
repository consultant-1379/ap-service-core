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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * Class which checks if a string is a substitution tag reserved by AP.
 * <p>
 * Reserved tags are used in the AP flow to generate node artifacts, and are not to be considered a validation failure for AP batch projects.
 */
final class ReservedSubstitutionTagChecker {

    private static final List<String> RESERVED_PREFIXES = new ArrayList<>(1);
    private static final Set<String> RESERVED_SUBSTITUTION_TAGS = new HashSet<>(1);

    private ReservedSubstitutionTagChecker() {

    }

    static {
        RESERVED_PREFIXES.add("INTERNAL_");
    }

    static {
        RESERVED_SUBSTITUTION_TAGS.add("logicalName");
    }

    /**
     * Checks if substitution tag is reserved for substitution by AP.
     *
     * @param substitutionTag
     *            the substitution tag to check
     * @return true if the substitution tag is reserved by AP
     */
    public static boolean isReserved(final String substitutionTag) {
        if (StringUtils.isBlank(substitutionTag)) {
            return false;
        }

        if (RESERVED_SUBSTITUTION_TAGS.contains(substitutionTag)) {
            return true;
        }

        for (final String reservedPrefix : RESERVED_PREFIXES) {
            if (substitutionTag.startsWith(reservedPrefix)) {
                return true;
            }
        }

        return false;
    }
}
