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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ConfigurationFile;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Class that is responsible to build the multi-part request that is sent to the Node Plugin REST interface.
 * The request contains two parts: JSON content and plain text content.
 */
public class NodePluginRequestBuilder {

    private static final String SEMI_COLON = ";";
    private static ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private static final String NAME = "json";
    private static final String FILE_NAME = "request.json";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Builds the RequestBody to call the Node Plugin service
     *
     * @param validationData
     *     the data to be validated
     * @return a builder response
     *
     * @throws JsonProcessingException
     * @throws UnsupportedEncodingException
     */
    public MultipartEntityBuilder buildNodePluginRequest(final ValidationData validationData) throws JsonProcessingException, UnsupportedEncodingException {

        final String jsonString = convertObjectToJSON(validationData);
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        final String boundary = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
        multipartEntityBuilder.setBoundary(boundary);
        final FormBodyPart jsonPart = FormBodyPartBuilder.create()
            .setName(NAME)
            .addField("name", FILE_NAME + SEMI_COLON)
            .addField("filename", FILE_NAME + SEMI_COLON)
            .setBody(new StringBody(jsonString, ContentType.create(ContentType.APPLICATION_JSON.getMimeType())))
            .build();
        multipartEntityBuilder.addPart(jsonPart);
        for (ConfigurationFile configurationFile : validationData.getConfigurationFiles()) {
            multipartEntityBuilder.addBinaryBody(configurationFile.getFileName(), configurationFile.getFileContent().getBytes(),
                ContentType.TEXT_PLAIN, configurationFile.getFileName());
        }
        if (validationData.isValidateDelta()) {
            final ConfigurationFile preconfigurationFile = validationData.getPreconfigurationFile();
            multipartEntityBuilder.addBinaryBody(preconfigurationFile.getFileName(), preconfigurationFile.getFileContent().getBytes("UTF-8"),
                ContentType.TEXT_PLAIN, preconfigurationFile.getFileName());
        }

        if (logger.isTraceEnabled()) {
            logRequest(multipartEntityBuilder);
        }
        return multipartEntityBuilder;
    }

    private String convertObjectToJSON(final ValidationData validationData) throws JsonProcessingException {
        return objectWriter.writeValueAsString(validationData);
    }

    private void logRequest(final MultipartEntityBuilder multipartEntityBuilder) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            multipartEntityBuilder.build().writeTo(bytes);
            final String content = bytes.toString();
            if (logger.isTraceEnabled()) {
                logger.trace("Node Plugin Request Content: {}", content);
            }
        }
        catch(Exception e){
            logger.error("Node Plugin Request error: {}", e);
        }
    }

}
