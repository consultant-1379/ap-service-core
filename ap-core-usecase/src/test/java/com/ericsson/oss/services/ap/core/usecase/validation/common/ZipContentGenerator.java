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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader;

/**
 * Generates project files with contents required by test.
 */
public class ZipContentGenerator {

    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final ZipOutputStream zos = new ZipOutputStream(bos);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Creates a file in the root of the zip.
     *
     * @param fileName
     *            name of file to create
     * @param fileContents
     *            contents of the file
     */
    public void createFileInZip(final String fileName, final String fileContents) {
        createFile(new ZipEntry(fileName), fileContents.getBytes());
    }

    /**
     * Creates a file (with path) in the zip.
     *
     * @param path
     *            path to the file within the zip
     * @param fileName
     *            name of the file
     * @param fileContents
     *            contents of the file (as String)
     */
    public void createFileInZip(final String path, final String fileName, final String fileContents) {
        createFile(new ZipEntry(path + "\\" + fileName), fileContents.getBytes());
    }

    private void createFile(final ZipEntry entry, final byte[] fileContentBytes) {
        try (final InputStream myInputStream = new ByteArrayInputStream(fileContentBytes);
                final BufferedInputStream entryStream = new BufferedInputStream(myInputStream, 2048)) {
            int count;
            zos.putNextEntry(entry);

            while ((count = entryStream.read(fileContentBytes)) > 0) {
                zos.write(fileContentBytes, 0, count);
            }

            zos.closeEntry();
        } catch (final IOException e) {
            logger.warn("Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Get the zip file as a byte[].
     *
     * @return the zip file
     */
    public byte[] getZip() {
        try {
            zos.close();
        } catch (final IOException e) {
            logger.warn("Error: {}", e.getMessage(), e);
        }
        return bos.toByteArray();
    }

    /**
     * Get zip file data.
     *
     * @param validZipFileName
     *            the zip file name
     * @return the contents of the zip file, where fileContent is keyed by fileName.
     */
    public Map<String, Object> getZipData(final String validZipFileName) {
        final Map<String, Object> zipData = new LinkedHashMap<>();

        try {
            zipData.put("fileName", validZipFileName);
            zipData.put("fileContent", ArchiveReader.read(getZip()));
            zipData.put("directoryList", ArchiveReader.read(getZip()).getAllDirectoryNames());
        } catch (final Exception e) {
            logger.warn("Error: {}", e.getMessage(), e);
        }
        return zipData;
    }
}
