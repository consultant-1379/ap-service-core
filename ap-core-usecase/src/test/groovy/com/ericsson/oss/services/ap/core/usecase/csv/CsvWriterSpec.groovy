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

package com.ericsson.oss.services.ap.core.usecase.csv

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.core.usecase.csv.generator.CsvWriter

import spock.lang.Subject
import spock.lang.Unroll

class CsvWriterSpec extends CdiSpecification {

    @Subject
    CsvWriter csvWriter

    ResourceService resourceService

    def setup() {
        resourceService = Stub(ResourceService)
        csvWriter = new CsvWriter(resourceService : resourceService)
    }

    def "when list string is not empty then a csv line must be returned"() {
        given: "Populated list of string"
        def strings = ["test1", "test2", "test3", "test4", "test5"]

        when: "Convert this list of string to a csv line"
        def csvOutput = csvWriter.toCsv(strings.toSet())

        then: "The csv line is a concatenation of the list of string separated by comma"
        csvOutput.split(',').sort() == strings.sort()
    }

    @Unroll("Csv extension must always be validated in filenames like #fileUri")
    def "when csv generation is triggered then a filename pattern must be respected"() {
        given: "The name of the file"
        def fileName = "file"
        def csvUri = "."

        when: "The filename of the CSV is created"
        def outputFileName = csvWriter.write([].toSet(), csvUri, fileName)

        then: "Timestamp and extension is added in the filename"
        outputFileName ==~ /[0-9]+_file.csv/
    }
}
