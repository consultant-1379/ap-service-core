/*
 ------------------------------------------------------------------------------
  *******************************************************************************
  * COPYRIGHT Ericsson 2019
  *
  * The copyright to the computer program(s) herein is the property of
  * Ericsson Inc. The programs may be used and/or copied only with written
  * permission from Ericsson Inc. or in accordance with the terms and
  * conditions stipulated in the agreement/contract under which the
  * program(s) have been supplied.
  *******************************************************************************
  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.ap.core.cli


import com.ericsson.cds.cdi.support.spock.CdiSpecification

class CliCommandSpec extends CdiSpecification {

    private CliCommand cliCommand

    def "CliCommand object contains correct operation and parameter values"() {

        when: "CliCommand object is created"
        cliCommand = new CliCommand(fullCommand, null)

        then: "CliCommand object contains correct operation and parameter values"
        cliCommand.getOperation() == operation
        cliCommand.getParameters() == parameters

        where:
        fullCommand                                               || operation  | parameters
        'ap replace -n nodeName -s serialNumber -b backupName'    || 'replace'  | ['-n', 'nodeName', '-s', 'serialNumber', '-b', 'backupName']
        'ap replace -n nodeName -s serialNumber'                  || 'replace'  | ['-n', 'nodeName', '-s', 'serialNumber']
        'ap replace -n nodeName -s serialNumber -b "backup Name"' || 'replace'  | ['-n', 'nodeName', '-s', 'serialNumber', '-b', 'backup Name']
        'ap order -nv file:"fileName"'                            || 'order'    | ['-nv', 'file:fileName']
        'ap download -o -n nodeName'                              || 'download' | [ '-o', '-n', 'nodeName']
    }
}
