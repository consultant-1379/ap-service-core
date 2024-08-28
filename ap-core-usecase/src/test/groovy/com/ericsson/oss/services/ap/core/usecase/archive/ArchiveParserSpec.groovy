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
package com.ericsson.oss.services.ap.core.usecase.archive

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.csv.generator.ArchiveParser

import spock.lang.Unroll

class ArchiveParserSpec extends CdiSpecification {

    @ObjectUnderTest
    ArchiveParser archiveParser

    private ArchiveArtifact archiveArtifact

    Integer randomTextLength = 10

    def List<ArchiveArtifact> archiveArtifacts = new ArrayList<ArchiveArtifact>()

    def setup() {
        archiveParser = new ArchiveParser()
    }

    def "when no substitution variable in archive then the parser gives no result"() {
        given: "No substitution variable in archive"
        def variables = ["test1", "test2", "test3", "test4"]

        and: "Some artifacts"
        archiveArtifact.getContentsAsString() >> variables.join(generateRandomText(randomTextLength))
        archiveArtifact.getAbsoluteName() >> "./path/to/file.xml"

        when: "Parsing is executed over the archive"
        def substitutionVariables = archiveParser.parse(archiveArtifacts)

        then: "No substitution variable is found"
        substitutionVariables.empty
    }

    @Unroll("Parser ignored these substitution variables = #invalidSubstitutionVariables")
    def "when there are invalid substitution variables in archive then the parser ignores them"() {
        given: "Some valid and invalid substitution variables in archive"
        def substitutionVariables = validSubstitutionVariables + invalidSubstitutionVariables

        and: "An artifact where there are up to one variable per line"
        ArchiveArtifact firstArchiveArtifact = Stub(ArchiveArtifact)
        firstArchiveArtifact.getContentsAsString() >> substitutionVariables.join(generateRandomText(randomTextLength))
        firstArchiveArtifact.getAbsoluteName() >> "./path/to/file.xml"

        and: "Another similar artifact"
        ArchiveArtifact secondArchiveArtifact = Stub(ArchiveArtifact)
        secondArchiveArtifact.getContentsAsString() >> substitutionVariables.join(generateRandomText(randomTextLength))
        secondArchiveArtifact.getAbsoluteName() >> "./path/to/file.xml"

        and: "Artifacts are added to list"
        archiveArtifacts.add(firstArchiveArtifact)
        archiveArtifacts.add(secondArchiveArtifact)

        when: "Parsing is executed over the archive"
        def parsedSubstitutionVariableFromArchive = archiveParser.parse(archiveArtifacts)

        then: "The result contains only the valid substitution variables"
        validSubstitutionVariables.collect { variable -> variable.replace('%', '') }.sort() == parsedSubstitutionVariableFromArchive.sort()

        where: "Sample substitution variables"
        validSubstitutionVariables                   || invalidSubstitutionVariables
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| []
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["%INTERNAL_test1%"]
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["%*test1%", "%t*est2%", "%test3*%", "%test*4%"]
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["%&test1%", "%t&est2%", "%tes&t3%", "%t&est4%"]
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["%&test1%", "%t&est2%", "%tes&t3%", "%t&est4%"]
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["<timeZone>GB-E%ire</timeZone>"]
        ["%test1%", "%test2%", "%test3%", "%test4%"]|| ["<timeZone>%%</timeZone>"]
    }

    @Unroll("Parsed successfully with #numberOfVariablesPerLine substitution variables per line")
    def "when there are more variable in the line then all of them are parsed"() {
        given: "A line with more than one substitution variable"

        and: "An artifact where there are more than one variable per line"
        ArchiveArtifact artifact = Stub(ArchiveArtifact)
        artifact.getContentsAsString() >> line
        artifact.getAbsoluteName() >> "./path/to/file.xml"
        archiveArtifacts.add(artifact)

        when: "Parsing is executed over the archive"
        def parsedSubstitutionVariableFromArchive = archiveParser.parse(archiveArtifacts)

        then: "The result contains only the valid substitution variables"
        expectedValues == parsedSubstitutionVariableFromArchive.sort()

        where: "Artifacts content"
        line                                                                                                || expectedValues
        "--%variable1%---%variable2%---%variable3%---"                                                      || ["variable1", "variable2", "variable3"]
        "--%variable1%---%variable2%---%variable3%---%variable4%---%variable5%---"                          || ["variable1", "variable2", "variable3", "variable4", "variable5"]
        "--%variable1%---%variable2%---%variable3%---%variable4%---%variable5%---%variable6%---"            || ["variable1", "variable2", "variable3", "variable4", "variable5", "variable6"]
    }

    def generateRandomText(int length) {
        def alphabet = (('a'..'z') + ('A'..'Z') + ('0'..'9') + ['<', '>', '_', '.', ':', System.lineSeparator()]).join()
        return stringGenerator(alphabet, length)
    }

    def stringGenerator(String alphabet, int maxOccurrences) {
        if (maxOccurrences == 0) {
            return ""
        }
        new Random().with {
            (1..maxOccurrences).collect { alphabet[nextInt(alphabet.length())] }.join()
        }
    }
}
