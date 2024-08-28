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
package com.ericsson.oss.services.ap.core.usecase.csv.generator;

import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.exception.GenerateCsvFailedException;

/**
 * CSV Generator generates the CSV containing the substitution variables from the supplied {@code Archive}.
 *
 * @see Archive
 */
public interface CsvGenerator {

    /**
     * Generates the CSV file from the Archive.
     *
     * @param archive
     *            Contains one or more artifacts to process for generating the CSV.
     * @return CSV file name generated on the file system.
     *
     * @throws GenerateCsvFailedException failed to generate csv
     */
    String generateCsv(final Archive archive) throws GenerateCsvFailedException;

    /**
     * Generates the CSV file from the Archive in custom location and custom name specified in method arguments.
     *
     * @param archive
     *            Contains one or more artifacts to process for generating the CSV.
     * @param path
     *            Custom path to csv file.
     * @param fileName
     *            Custom file name.
     * @return CSV file name generated on the file system.
     *
     * @throws GenerateCsvFailedException failed to generate csv
     */
    String generateCsv(final Archive archive, final String path, final String fileName) throws GenerateCsvFailedException;

}
