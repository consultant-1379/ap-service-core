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
package com.ericsson.oss.services.ap.common.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class ZipUtil {

    private ZipUtil() {

    }

    /**
     * Creates a list of zipped Bytes from an input String
     *
     * @param stringToZip
     *            A String which will represent XML taken from NodeInfo.xml
     * @return Returns a compressed list which has been converted from a String to a list of Bytes
     * @throws IOException
     */
    public static List<Byte> createZippedByteList(final String name, final String stringToZip) throws ZipException {
        if (stringToZip == null) {
            throw new IllegalArgumentException("Input String null for file: " + name);
        }

        byte[] byteArr = null;
        try {
            byteArr = stringToZip.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException ex) {
            throw new ZipException(
                "Error trying to create Zip File. Unsupported character encoding - cannot convert string to zip for file: " + name);
        }

        try (final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteArr);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            final ZipOutputStream zipfile = new ZipOutputStream(bos);

            final ZipEntry zipentry = new ZipEntry(name);
            zipfile.putNextEntry(zipentry);

            while (byteInputStream.available() > 0) {
                final int bufferSize = Math.min(byteInputStream.available(), 1024);
                final byte[] curBytes = new byte[bufferSize];
                byteInputStream.read(curBytes, 0, bufferSize);
                zipfile.write(curBytes);
            }
            zipfile.close();

            return conversion(bos);
        } catch (final IOException e) {
            throw new ZipException("Error trying to create zip File: " + name);
        }
    }

    /**
     * Unzips a compressed list of Bytes to return a StringL.
     *
     * @param zipped
     *            A compressed list of Bytes
     * @return Returns a String which is the uncompressed list of Bytes.
     * @throws IOException
     */
    public static String unzipToString(final List<Byte> zipped) throws IOException {

        final byte[] temp = new byte[zipped.size()];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = zipped.get(i);
        }

        try (final ByteArrayInputStream byteInputStream = new ByteArrayInputStream(temp);
            final ZipInputStream inputStream = new ZipInputStream(byteInputStream);
            final ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            inputStream.getNextEntry();

            final byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            return out.toString();
        }
    }

    /**
     * Converts a ByteArrayOutputStream to a list of Bytes, needed as you cannot convert from ByteArrayOutputStream to List<Byte> directly
     *
     * @param bos
     *            ByteArrayOutputStream
     * @return a list of Bytes
     * @throws IOException
     */
    private static List<Byte> conversion(final ByteArrayOutputStream bos) {
        final byte[] byteArray = bos.toByteArray();
        final Byte[] convertedByteArray = convertToByte(byteArray);
        return Arrays.asList(convertedByteArray);
    }

    /**
     * Converts an array of bytes to an array of Bytes, needed as you cannot convert from byte[] to List<Byte>
     *
     * @param byteArray
     *            an array of bytes
     * @return Returns a Byte array
     * @throws IllegalArgumentException
     *             if the byteArray is <code>null</code>
     * @throws IOException
     */
    private static Byte[] convertToByte(final byte[] byteArray) {
        final Byte[] converted = new Byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            converted[i] = Byte.valueOf(byteArray[i]);
        }
        return converted;
    }
}
