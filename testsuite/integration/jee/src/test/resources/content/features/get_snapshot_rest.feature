Feature: Get snapshot via REST

  Scenario Outline: Service should return profile not found error when profile does not exist when get snapshot

    Given the system has a project '<ProjectName>' for a node '<NodeNames>' with no profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' requests a get rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                                                   | StatusCode | ProjectName | NodeNames  | ProfileName  |UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/node/<NodeNames>/nodeDumpSnapshot | 404        | PROJECT_01  | Node000001 | PROFILEID_00 |ap_admin |

  Scenario Outline: Service can get node snapshot for a profile

    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And snapshot is generated for node '<NodeNames>' with filter in project '<ProjectName>' with profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a get rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And response body contains '<FileContent>'
    Examples:
      | URI                                                                                                   | StatusCode | ProjectName | NodeNames  | ProfileName | UserType | FileContent                   |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/node/<NodeNames>/nodeDumpSnapshot | 200        | PROJECT1    | Node000001 | Profile01   | ap_admin | NodeConfigurationSnapshot.xml |
