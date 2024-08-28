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
package com.ericsson.oss.services.ap.core.usecase.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * This class will process a project archive and return an {@link Archive}.
 */
public final class ArchiveReader {

    private static final String FOLDER_SEPARATOR = "/";
    private static final ArchiveStreamFactory STREAM_FACTORY = new ArchiveStreamFactory();

    private ArchiveReader() {

    }

    /**
     * Reads a ProjectArchive array of bytes, which may consist of any (supported) compression type.
     *
     * @param bytes
     *            the archive content byte array
     * @return the project archive
     * @throws IOException
     *             if the file can't be read or if there is a processing error
     */
    public static Archive read(final byte[] bytes) throws IOException {
        try (final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                final ArchiveInputStream ais = STREAM_FACTORY.createArchiveInputStream(bais)) {
            final Map<String, ArchiveArtifact> result = new LinkedHashMap<>();
            ArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                processEntry(ais, entry, result);
            }

            return new Archive(result);
        } catch (final ArchiveException e) {
            throw new IOException(e);
        }
    }

    private static void processEntry(final ArchiveInputStream ais, final ArchiveEntry entry, final Map<String, ArchiveArtifact> artifactMap)
            throws IOException {
        final String entryName = entry.getName().replaceAll("\\\\", FOLDER_SEPARATOR);
        if (!entry.isDirectory()) {
            processFileEntry(ais, artifactMap, entryName);
        }
    }

    private static void processFileEntry(final ArchiveInputStream ais, final Map<String, ArchiveArtifact> artifactMap, final String entryName)
            throws IOException {
        final byte[] entryContent = getStreamContent(ais);
        final ArchiveArtifact artifact = new ArchiveArtifact(entryName, entryContent);
        artifactMap.put(entryName, artifact);
    }

    private static byte[] getStreamContent(final ArchiveInputStream zipStream) throws IOException {
        return IOUtils.toByteArray(zipStream);
    }
}
