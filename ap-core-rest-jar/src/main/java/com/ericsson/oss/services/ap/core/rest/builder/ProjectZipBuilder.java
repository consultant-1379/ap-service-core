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
package com.ericsson.oss.services.ap.core.rest.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.common.util.file.File;

/**
 * Builds a Project ZIP file from a given input
 */
public class ProjectZipBuilder {

    private static final String PROJECT_INFO_TEMPLATE = "/templates/projectInfoTemplate.xml";
    private static final String PROJECT_INFO_ARTIFACT = "projectInfo.xml";
    private static final String PROJECT_ID_TAG = "%projectId%";
    private static final String DELIMETER = "/";

    /**
     * Zip multiple files.
     *
     * @param projectId
     *            the id of the project to create nodes under
     * @param filesToZip
     *            map of filename to file content
     * @return a zip file with the supplied content
     * @throws IOException
     *             thrown if an error occurs
     */
    public byte[] createProjectZipFile(final String projectId, final Map<String, List<File>> filesToZip) throws IOException {
        final String projectInfoXml = createProjectInfo(projectId);
        final byte[] projectInfo = projectInfoXml.getBytes();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ZipOutputStream zipfile = new ZipOutputStream(bos);
        outputStream.write(projectInfo);
        addConfiguration(zipfile, PROJECT_INFO_ARTIFACT, projectInfoXml);

        for (final Entry<String, List<File>> entry : filesToZip.entrySet()) {
            final String nodeName = entry.getKey();
            addDirectoryToZip(nodeName, zipfile);
        }

        for (final Entry<String, List<File>> entry : filesToZip.entrySet()) {
            addConfigurationsToZip(outputStream, zipfile, entry);
        }
        zipfile.close();
        return bos.toByteArray();
    }

    private void addConfigurationsToZip(final ByteArrayOutputStream outputStream, final ZipOutputStream zipfile,
        final Entry<String, List<File>> entry) throws IOException {
        final String nodeName = entry.getKey();
        for (final File configurationFile : entry.getValue()) {
            final String fileContent = configurationFile.getContent();
            outputStream.write(fileContent.getBytes());
            final String fileName = nodeName + DELIMETER + configurationFile.getName();
            addConfiguration(zipfile, fileName, fileContent);
        }
    }

    private static void addDirectoryToZip(final String nodeName, final ZipOutputStream zipfile) throws IOException {
        final ZipEntry zipEntry = new ZipEntry(nodeName + "/");
        zipfile.putNextEntry(zipEntry);
        zipfile.closeEntry();
    }

    private static void addConfiguration(final ZipOutputStream zipfile, final String fileName, final String stringToZip)
        throws IOException {
        final byte[] byteArr = stringToZip.getBytes(StandardCharsets.UTF_8.toString());
        final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteArr);

        final ZipEntry zipentry = new ZipEntry(fileName);
        zipfile.putNextEntry(zipentry);
        while (byteInputStream.available() > 0) {
            int numberToRead = 0;
            if (byteInputStream.available() > 1024) {
                numberToRead = 1024;
            } else {
                numberToRead = byteInputStream.available();
            }
            final byte[] curBytes = new byte[numberToRead];
            byteInputStream.read(curBytes, 0, numberToRead);
            zipfile.write(curBytes);
        }
        byteInputStream.close();
    }

    private String createProjectInfo(final String projectId) throws IOException {
        final InputStream projectInfoTemplate = getClass().getResourceAsStream(PROJECT_INFO_TEMPLATE);
        final String projectInfo = convertInputStreamToString(projectInfoTemplate);
        return replaceProjectIdName(projectInfo, projectId);
    }

    private static String convertInputStreamToString(final InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

    private String replaceProjectIdName(final String projectInfoTemplate, final String projectId) {
        return StringUtils.replace(projectInfoTemplate, PROJECT_ID_TAG, projectId);
    }
}
