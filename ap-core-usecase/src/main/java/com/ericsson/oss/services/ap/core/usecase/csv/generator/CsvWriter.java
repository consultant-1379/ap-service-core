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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * Write the csv file into the file system.
 */
public class CsvWriter {
    private static final String SEPARATOR = ",";

    private static final String FILE_EXTENSION = ".csv";

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }


    /**
     * Write the {@code csvContent} into comma separated value format on the file system with specified {@code fileName}
     *
     * @param csvContent content to write in csv format on file system
     * @param fileName csv file name
     * @param csvUri the csv uri
     * @return {@code fileUri} with appended .csv if it is not there already
     */
    public String write(final Set<String> csvContent, final String csvUri, final String fileName) {
        final String csvHeader = toCsv(csvContent);

        final String csvFileName = buildFileName(fileName);

        final String csvFileUri = csvUri + File.separator + csvFileName;

        resourceService.write(csvFileUri, csvHeader.getBytes(StandardCharsets.UTF_8), false);

        return csvFileName;
    }

    private String toCsv(final Set<String> listToConvert) {
        return StringUtils.join(listToConvert, SEPARATOR);
    }

    private String buildFileName(final String fileName) {
        return new StringBuilder()
                .append(Calendar.getInstance().getTimeInMillis())
                .append("_")
                .append(fileName)
                .append(FILE_EXTENSION)
                .toString();
    }
}
