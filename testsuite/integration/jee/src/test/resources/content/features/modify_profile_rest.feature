Feature: Update profile via REST

  Scenario Outline: Service should return profile not found error when profile does not exist

    Given the system has a project '<ProjectName>' with no profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                   | StatusCode | ProjectName | ProfileName  |UserType | FilePath                                    |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles | 404        | PROJECT_01  | PROFILEID_00 |ap_admin | json/profile/modify_profile_name_error.json |

  Scenario Outline: Service should modify profile when profile exists
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    And no error will occur
    And response body contains '<ProfileName>'
    And response body contains '<UpgradePackageName>'
    And response body contains '<OssModelIdentity>'
    Examples:
      | URI                                                   | StatusCode | ProjectName | ProfileName  |UserType | FilePath                                    | UpgradePackageName | OssModelIdentity |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles | 200        | PROJECT_01  | PROFILEID_01 |ap_admin | json/profile/modify_profile_valid.json      | upgrade_name       | some_identity    |
