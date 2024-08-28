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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.template;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.ericsson.oss.services.ap.common.util.xml.DocumentBuilder;
import com.ericsson.oss.services.ap.common.util.xml.XPaths;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;

/**
 * Read-only class that abstracts template data.
 */
public class Template {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    private final String name;
    private final String contents;

    Template(final String name, final String contents) {
        this.name = name;
        this.contents = contents;
    }

    public String getName() {
        return name;
    }

    public String getContents() {
        return contents;
    }

    /**
     * Get all placeholders for this template.
     *
     * @return all placeholders contained in this template
     */
    public List<String> getPlaceHolders() {
        final List<String> result = new ArrayList<>();
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(contents);

        while (matcher.find()) {
            result.add(matcher.group(1));
        }

        return result;
    }

    /**
     * Locates the placeholder for a specific path, to be replaced during processing.
     *
     * @param path
     *            the name of the path
     * @return the name of the placeholder that will be replaced by the processor
     */
    public String getPlaceHolderForPath(final String path) {
        try {
            final Document document = DocumentBuilder.getDocument(contents);
            final XPathExpression expression = XPaths.getXpathExpression(path);
            final String result = (String) expression.evaluate(document, XPathConstants.STRING);

            if (result.isEmpty()) {
                throw new IllegalArgumentException("Path '" + path + "' did not return any result");
            }

            final Matcher matcher = PLACEHOLDER_PATTERN.matcher(result);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Value '" + result + "' for path does not contain a placeholder");
            }

            return matcher.group(1);
        } catch (final XmlException e) {
            throw new IllegalArgumentException("Content is either not XML, or malformed XML", e);
        } catch (final XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Applies transformation to the template.
     *
     * @param data
     *            the key - value store that will be used when replacing placeholders in the template
     * @return the applied transformation
     */
    public String process(final Map<String, String> data) {
        final Queue<Entry<String, String>> replacements = new ArrayDeque<>(data.entrySet());
        return process(contents, replacements);
    }

    private static String process(final String contents, final Queue<Entry<String, String>> replacements) {
        if (replacements.isEmpty()) {
            return contents;
        }

        final Entry<String, String> replacement = replacements.poll();
        return process(replaceContent(contents, replacement), replacements);
    }

    private static String replaceContent(final String contents, final Entry<String, String> replacement) {
        return contents.replaceAll("%" + replacement.getKey() + "%", replacement.getValue());
    }
}
