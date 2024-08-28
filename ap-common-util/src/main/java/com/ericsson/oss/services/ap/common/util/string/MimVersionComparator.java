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
package com.ericsson.oss.services.ap.common.util.string;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Alphanumeric compare of two MIM version strings.
 */
public class MimVersionComparator implements Comparator<String>, Serializable {

    private static final long serialVersionUID = -244586349174630033L;

    /**
     * Compare 2 strings of digits and strings separated by dots or underscores, like <code>a.4.1</code>.
     * <p>
     * If contains digits surrounded by '.' or '_' then the digits then uses integers compare, otherwise string compare.
     * <p>
     * If 1 string contains more parts (separated by dots), then if they are otherwise equals, the longer one is greater than the smaller one. (i.e.
     * "aaa.bbb.ccc" {@literal >} "aaa.bbb").
     * <p>
     * For example:
     * <ul>
     * <li>"a_9b" {@literal >} "a_33a" as string compare used (i.e. "9b" {@literal >} "33a")</li>
     * <li>"a_9" {@literal <} "a_33" as integer compare used (i.e. 9 {@literal <} 33)</li>
     * </ul>
     *
     * @return 0 if exact same, less than 0 if s1 {@literal <} s2, greater than 0 if s1 {@literal >} s2
     */
    @Override
    public int compare(final String s1, final String s2) {
        final String s1DotSeparated = s1.replaceAll("_", ".").toLowerCase();
        final String s2DotSeparated = s2.replaceAll("_", ".").toLowerCase();

        final String[] s1Parts = s1DotSeparated.split("\\.");
        final String[] s2Parts = s2DotSeparated.split("\\.");

        for (int i = 0; i < s1Parts.length && i < s2Parts.length; ++i) {
            final String s1Part = s1Parts[i];
            final String s2Part = s2Parts[i];

            final int result = compareParts(s1Part, s2Part);
            if (result != 0) {
                return result;
            }
        }

        return compareStringPartSizes(s1Parts, s2Parts);
    }

    private static int compareParts(final String s1Part, final String s2Part) {
        if (isInteger(s1Part) && isInteger(s2Part)) {
            return compareIntegers(s1Part, s2Part);
        }
        return compareStrings(s1Part, s2Part);
    }

    /**
     * Convert input to Letter MIM Version format if required If MIM version is in number format, for example 6.1.100, then return letter format, for
     * example f.1.100
     *
     * @param possibleNumberMimVersion
     *            MIM version in number format, for example 6.1.100
     * @return If MIM version is in number format, for example 6.1.100, then return letter format; f.1.100
     */
    public static String convert(final String possibleNumberMimVersion) {
        final String firstPart = possibleNumberMimVersion.split("\\.")[0];

        if (!isInteger(firstPart)) {
            return possibleNumberMimVersion;
        }

        final String firstChar = getCharForNumber(Integer.parseInt(firstPart));
        return firstChar + possibleNumberMimVersion.substring(firstPart.length());
    }

    private static int compareStrings(final String s1Part, final String s2Part) {
        return s1Part.compareTo(s2Part);
    }

    private static int compareIntegers(final String s1Part, final String s2Part) {
        final int s1PartInteger = Integer.parseInt(s1Part);
        final int s2PartInteger = Integer.parseInt(s2Part);
        return s1PartInteger - s2PartInteger;
    }

    private static int compareStringPartSizes(final String[] s1Parts, final String[] s2Parts) {
        return s1Parts.length - s2Parts.length;
    }

    private static boolean isInteger(final String input) {
        return input.matches("[\\d]+");
    }

    private static String getCharForNumber(final int i) {
        final char c = (char) (64 + i);
        return Character.toString(c);
    }
}
