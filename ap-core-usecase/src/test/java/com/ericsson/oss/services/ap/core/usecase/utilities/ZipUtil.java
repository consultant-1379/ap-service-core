/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Helper class for test classes.
 */
public final class ZipUtil {

    private ZipUtil() {

    }

    /**
     * Zip single file.
     *
     * @param fileName
     *            the name of the file
     * @param fileContent
     *            the file content
     * @return a zip file with the supplied content
     * @throws IOException
     *             thrown if an error occurs
     */
    public static byte[] createProjectZipFile(final String fileName, final String fileContent) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ZipOutputStream zipfile = new ZipOutputStream(bos);

        addToZip(zipfile, fileName, fileContent);
        zipfile.close();

        return bos.toByteArray();
    }

    /**
     * Zip multiple files.
     *
     * @param filesToZip
     *            map of filename to file content
     * @return a zip file with the supplied content
     * @throws IOException
     *             thrown if an error occurs
     */
    public static byte[] createProjectZipFile(final Map<String, String> filesToZip) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final ZipOutputStream zipfile = new ZipOutputStream(bos);

        for (final String filename : filesToZip.keySet()) {
            addToZip(zipfile, filename, filesToZip.get(filename));
        }
        zipfile.close();
        return bos.toByteArray();
    }

    private static void addToZip(final ZipOutputStream zipfile, String fileName, final String stringToZip) throws IOException {
        final byte[] byteArr = stringToZip.getBytes(StandardCharsets.UTF_8.toString());
        final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteArr);
        final ZipEntry zipentry = new ZipEntry(fileName);
        zipfile.putNextEntry(zipentry);

        while (byteInputStream.available() > 0) {
            int numberToRead;

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
}
