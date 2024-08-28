Feature: Export CIQ from the system

  Scenario Outline: Export CIQ file for a Profile
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And ciq is generated for project '<ProjectName>' and profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/octet-stream' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    And response header 'Content-Disposition' contains '<ProfileName>'
    And response body contains '<FileContent>'
    Examples:
      | URI                                                                     | StatusCode | ProjectName       | ProfileName |  UserType |  FileContent |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/ciq | 200        | ExportTestProject | Profile01   |  ap_admin |  Node1       |
      
  Scenario Outline: Export CIQ using Rest call when file does not exist
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And response body contains '<ErrorMessage>'
    And no error will occur
    Examples:
      | URI                                                                     | StatusCode | ProjectName           | ProfileName | UserType | ErrorMessage                                 |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/ciq | 400        | TestProjectWithoutCIQ | Profile01   | ap_admin | No CIQ found to export for Profile Profile01 |

  Scenario Outline: Export CIQ using Rest call when profile does not exist
    Given the system has a project '<ProjectName>' with no profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                     | StatusCode | ProjectName           | ProfileName | UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/ciq | 404        | TestProjectWithoutCIQ | Profile01   | ap_admin |