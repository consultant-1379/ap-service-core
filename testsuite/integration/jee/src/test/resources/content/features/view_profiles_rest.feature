Feature: View profiles details

  Scenario Outline: View profiles when project has child profiles should return profiles
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>' and data type '<DatatypeName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    And response body contains '<ResponseContent>'
    Examples:
    | URI                                                                           | StatusCode | ProjectName | ProfileName | DatatypeName               | UserType | ResponseContent |
    | /auto-provisioning/v1/projects/<ProjectName>/profiles/dataType/<DatatypeName> | 200        | Project1    | Profile1    | node-plugin-request-action | ap_admin | Profile1        |

  Scenario Outline: View profiles when project has no child profiles should return empty profile list with no error
    Given the system has a project '<ProjectName>' with no profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    And response body contains '<ResponseContent>'
    Examples: empty array in JSON as string
    | URI                                                   | StatusCode | ProjectName | ProfileName | UserType | ResponseContent |
    | /auto-provisioning/v1/projects/<ProjectName>/profiles | 200        | Project2    | Profile1    | ap_admin | {"profiles":[]} |

  Scenario Outline: Viewing profiles when no project exists should return correct error
    Given the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                              | StatusCode | UserType |
      | /auto-provisioning/v1/projects/Project1/profiles | 404        | ap_admin |