Feature: Dump snapshot via REST

  Scenario Outline: Service should return profile not found error when profile does not exist when dump snapshot

    Given the system has a project '<ProjectName>' with no profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                                  | StatusCode | ProjectName | ProfileName  |UserType | FilePath                              |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/nodeDumpSnapshot | 404        | PROJECT_01  | PROFILEID_00 |ap_admin | json/profile/dump_snapshot_valid.json |

  Scenario Outline: Service should trigger dumping snapshot for a profile

    Given the system already has a node '<NodeNames>'
    And the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                                  | StatusCode | NodeNames | ProjectName | ProfileName | UserType | FilePath                              |
      | /auto-provisioning/v1/projects/<ProjectName>/profiles/<ProfileName>/nodeDumpSnapshot | 200        | node01    | PROJECT_01  | Profile01   | ap_admin | json/profile/dump_snapshot_valid.json |
