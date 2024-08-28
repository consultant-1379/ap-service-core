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

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;

/**
 * Unit tests for {@link Template}.
 */
public class TemplateTest {

    @Test
    public void when_template_has_no_contents_then_placeholder_list_should_be_empty() {
        final Template template = new Template("test", "");
        final List<String> placeHolders = template.getPlaceHolders();
        assertThat(placeHolders).as("List of template placeholders").isEmpty();
    }

    @Test
    public void when_template_has_no_placeholders_then_placeholder_list_should_be_empty() {
        final Template template = new Template("test",
                "There are no placeholders in this string, even though there is a percentage sign here: %.");
        final List<String> placeHolders = template.getPlaceHolders();
        assertThat(placeHolders).as("List of template placeholders").isEmpty();
    }

    @Test
    public void when_template_has_placeholders_then_placeholder_list_should_return_all_values() {
        final Template template = new Template("test", "We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");
        final List<String> placeHolders = template.getPlaceHolders();
        assertThat(placeHolders)
                .as("List of template placeholders")
                .containsOnly("a", "placeholders", "file", "sometimes_with", "1_or_more_numbers");
    }

    @Test
    public void when_placeholder_refers_to_a_field_it_should_be_returned_properly() {
        final Template template = new Template("test", "<xml><name>%name_placeholder%</name></xml>");
        final String placeholderName = template.getPlaceHolderForPath("/xml/name");
        assertThat(placeholderName)
                .as("Name of the placeholder")
                .isEqualTo("name_placeholder");
    }

    @Test
    public void when_placeholder_refers_to_a_path_that_is_not_valid_then_path_search_should_throw_exception() {
        final Template template = new Template("test", "<xml><name>%name_placeholder%</name></xml>");
        verifyException(template).getPlaceHolderForPath("/xml/invalid");
        assertThat((Exception) caughtException())
                .as("Exception raised by placeholder fieldname search")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Path '/xml/invalid' did not return any result");
    }

    @Test
    public void when_placeholder_refers_to_a_path_that_does_not_contain_placeholder_then_path_search_should_throw_exception() {
        final Template template = new Template("test", "<xml><name>name_placeholder</name></xml>");
        verifyException(template).getPlaceHolderForPath("/xml/name");
        assertThat((Exception) caughtException())
                .as("Exception raised by placeholder fieldname search")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Value 'name_placeholder' for path does not contain a placeholder");
    }

    @Test
    public void when_placeholder_refers_to_a_path_that_is_syntatically_incorrect_then_path_search_should_throw_exception() {
        final Template template = new Template("test", "<xml><name>name_placeholder</name></xml>");
        verifyException(template).getPlaceHolderForPath("\\xml\\name");
        assertThat((Exception) caughtException())
                .as("Exception raised by placeholder fieldname search")
                .hasCauseInstanceOf(XPathExpressionException.class);
    }

    @Test
    public void when_placeholder_refers_to_a_text_that_is_not_xml_then_path_search_should_throw_exception() {
        final Template template = new Template("test", "this is a %name_placeholder% test.");
        verifyException(template).getPlaceHolderForPath("name");
        assertThat((Exception) caughtException())
                .as("Exception raised by placeholder fieldname search")
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Content is either not XML, or malformed XML");
    }

    @Test
    public void when_template_has_placeholders_then_apply_to_empty_map_should_maintain_placeholders() {
        final Template template = new Template("test", "We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");
        final String value = template.process(Collections.<String, String> emptyMap());
        assertThat(value)
                .as("Contents after processing")
                .isEqualTo("We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");
    }

    @Test
    public void when_template_has_placeholders_then_apply_should_replace_placeholders() {
        final Template template = new Template("test", "We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");
        final Map<String, String> data = new HashMap<>();
        data.put("a", "<a replaced>");
        data.put("placeholders", "<placeholders replaced>");
        data.put("file", "<file replaced>");
        data.put("sometimes_with", "<sometimes_with replaced>");
        data.put("1_or_more_numbers", "<1_or_more_numbers replaced>");

        final String value = template.process(data);
        assertThat(value)
                .as("Contents after processing")
                .isEqualTo(
                        "We have <a replaced> few <placeholders replaced> in this <file replaced>, <sometimes_with replaced> <1_or_more_numbers replaced>.");
    }

    @Test
    public void when_template_has_placeholders_then_apply_should_not_change_original_contents() {
        final Template template = new Template("test", "We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");
        final Map<String, String> data = new HashMap<>();
        data.put("a", "<a replaced>");
        data.put("placeholders", "<placeholders replaced>");
        data.put("file", "<file replaced>");
        data.put("sometimes_with", "<sometimes_with replaced>");
        data.put("1_or_more_numbers", "<1_or_more_numbers replaced>");

        final String value = template.process(data);
        final String originalValue = template.getContents();
        assertThat(originalValue)
                .as("Original contents")
                .isEqualTo("We have %a% few %placeholders% in this %file%, %sometimes_with% %1_or_more_numbers%.");

        assertThat(value)
                .as("Contents after processing")
                .isNotEqualTo(originalValue);
    }
}
