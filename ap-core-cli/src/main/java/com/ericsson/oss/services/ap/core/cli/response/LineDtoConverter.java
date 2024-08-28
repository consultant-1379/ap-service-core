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
package com.ericsson.oss.services.ap.core.cli.response;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;

/**
 * Converts text to instances of {@link LineDto}.
 */
final class LineDtoConverter {

    private static final LineDto BLANK_LINE_DTO = new LineDto("");
    private static final String CM_CLI_SPACE = " ";
    private static final int PADDING_SPACE = 40;

    private LineDtoConverter() {

    }

    /**
     * Converts a {@link List} of <code>List{@literal <}String{@literal >}</code>, where each internal list must contain two elements: the attribute
     * name and the attribute value.
     * <p>
     * A list of {@link LineDto}s are returned with each LineDto representing a row of information.
     *
     * @param listOfNameValuePairs
     *            the input row data
     * @return the input information as {@link LineDto} objects
     */
    public static List<LineDto> convertRowsOfNameValuePairs(final List<List<String>> listOfNameValuePairs) {
        final List<LineDto> lineDtos = new ArrayList<>(listOfNameValuePairs.size());

        for (final List<String> row : listOfNameValuePairs) {
            final String lineData = mergeNameValuePair(row.get(0), row.get(1));
            lineDtos.add(new LineDto(lineData));
        }

        lineDtos.add(BLANK_LINE_DTO);
        return lineDtos;
    }

    /**
     * Converts a {@link List} of <code>List{@literal <}String{@literal >}</code>, where each internal list contains a single element: the attribute
     * value.
     * <p>
     * A list of {@link LineDto}s are returned with each LineDto representing a row of information.
     *
     * @param listOfValues
     *            the input row data
     * @return the input information as {@link LineDto} objects
     */
    public static List<LineDto> convertRowsOfValues(final List<List<String>> listOfValues) {
        final List<LineDto> lineDtos = new ArrayList<>(listOfValues.size());

        for (final List<String> row : listOfValues) {
            lineDtos.add(new LineDto(row.get(0)));
        }

        lineDtos.add(BLANK_LINE_DTO);
        return lineDtos;
    }

    /**
     * Combines a name-value pair of strings into a single string. Pads the name and value with a special space character (ALT+255) that can be picked
     * up by CM CLI.
     *
     * @param name
     *            the attribute name
     * @param value
     *            the attribute value
     * @return the merged name-value pair
     */
    public static String mergeNameValuePair(final String name, final String value) {
        return String.format("%s%s", StringUtils.rightPad(name, PADDING_SPACE, CM_CLI_SPACE), value);
    }
}