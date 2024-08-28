
Feature: Delete profile via REST

  Scenario Outline: Service should return profile not found error when profile does not exist

    Given the system has a project '<ProjectName>' with no profile '<ProfileName>'
    And the rest assured properties are set
    And the rest workflow services are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a delete rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                 | StatusCode | ProjectName | ProfileName  |UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName> | 404        | PROJECT_01  | PROFILEID_00 |ap_admin |

  Scenario Outline: Service should delete profile when profile exists
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    And the rest workflow services are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a delete rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                 | StatusCode | ProjectName | ProfileName  |UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName> | 204        | PROJECT_01  | PROFILEID_01 |ap_admin |
