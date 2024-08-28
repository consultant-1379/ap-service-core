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
package com.ericsson.oss.services.ap.core.rest.reader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.CharEncoding;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.util.ParameterParser;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.core.rest.model.request.order.configurations.Configuration;
import com.ericsson.oss.services.ap.core.rest.model.request.order.configurations.OrderNodeConfigurationsRequest;

/**
 * Reader class for files uploaded in REST. Reads fileName and file content.
 */
public class FileDataReader {

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String FILENAME = "filename";

    /**
     * Extract file name and file contents as bytes from the form data input (containing one file)
     *
     * @param input
     *            MultipartFormDataInput which contains file in body
     * @return a Map with one entry (for one file), key is file name, value is file content in bytes
     */
    public Map<String, byte[]> extractFileDetails(final MultipartFormDataInput input) {
        final Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
        if (formDataMap != null && formDataMap.size() == 1) {
            final Map.Entry<String, List<InputPart>> pair = formDataMap.entrySet().iterator().next();

            final List<InputPart> inputPartList = pair.getValue();
            if (inputPartList != null && inputPartList.size() == 1) {
                final InputPart inputPart = inputPartList.get(0);

                final List<String> dispositions = inputPart.getHeaders().get(CONTENT_DISPOSITION);
                if (dispositions != null && dispositions.size() == 1) {
                    final String disposition = dispositions.get(0);

                    final String fileName = extractFileNameFromDisposition(disposition);
                    final byte[] fileContent = buildFileContentAsBytes(inputPart);

                    final Map<String, byte[]> fileData = new HashMap<>();
                    fileData.put(fileName, fileContent);
                    return fileData;
                }
            }
        }
        throw new ApApplicationException("File details could not be extracted from form data input");
    }

    /**
     * Retrieves a Map of nodes containing the node name and list of configurations associated with that node
     *
     * @param input
     *            Configurations received from node plugin.
     * @return a Map of nodes containing the node name and list of configurations associated with that node
     */
    public Map<String, List<File>> retrieveNodeList(final OrderNodeConfigurationsRequest input) {
        final Map<String, List<File>> nodes = new HashMap<>();
        for (final Configuration config : input.getConfigurations()) {
            final List<File> nodeFiles = new ArrayList<>();
            try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(config.getContent())))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    if (!zipEntry.isDirectory()) {
                        final File configFile = new File(zipEntry.getName(), getFileContent(zipInputStream));
                        nodeFiles.add(configFile);
                    }
                }
            } catch (final IOException e) {
                throw new ApApplicationException(e.getMessage(), e);
            }
            nodes.put(config.getName(), nodeFiles);
        }
        return nodes;
    }

    private byte[] buildFileContentAsBytes(final InputPart inputPart) {
        final InputStream inputStream;
        try {
            inputStream = inputPart.getBody(InputStream.class, null);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int nRead;
            final byte[] chunk = new byte[16_384];
            while ((nRead = inputStream.read(chunk, 0, chunk.length)) != -1) {
                byteArrayOutputStream.write(chunk, 0, nRead);
            }
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();

        } catch (final IOException e) {
            throw new ApApplicationException("Error reading file {} " + e.getMessage());
        }
    }

    private String extractFileNameFromDisposition(final String disposition) {
        final ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        final Map<String, String> params = parser.parse(disposition, ';');
        final String filename = params.get(FILENAME);
        if (filename == null) {
            throw new ApApplicationException("Cannot extract filename from disposition '{}'" + disposition);
        }
        final boolean iso88591 = Charset.forName(CharEncoding.ISO_8859_1).newEncoder().canEncode(filename);
        /*
         * 'filename' header field parameters cannot carry characters outside the ISO-8859-1 https://tools.ietf.org/html/rfc6266#section-4.3
         */
        if (!iso88591) {
            throw new ApApplicationException("'filename' header field parameters cannot carry characters outside the ISO-8859-1");
        }
        return filename;
    }

    private String getFileContent(final ZipInputStream zipInputStream) throws IOException {
        String fileContents;
        final byte[] buffer = new byte[1024];
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int len;
            while ((len = zipInputStream.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            fileContents = new String(os.toByteArray());
        }
        return fileContents;
    }
}
