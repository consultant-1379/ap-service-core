Feature: Create profile via REST

  Scenario Outline: Service should return validation error when profile data is invalid

    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | PROJECT1    | NODE000001 |
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                              | StatusCode | NodeType  | UserType | FilePath                                    |
      | /auto-provisioning/v1/projects/PROJECT1/profiles | 400        | RadioNode | ap_admin | json/profile/create_profile_name_error.json |

  Scenario Outline: Service should create profile when data is valid

    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | PROJECT1    | NODE000001 |
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                              | StatusCode | NodeType  | UserType | FilePath                               |
      | /auto-provisioning/v1/projects/PROJECT1/profiles | 201        | RadioNode | ap_admin | json/profile/create_profile_valid.json |

  Scenario Outline: Service should return list of model ids for supported node type

      Given the rest assured properties are set
      And security is enabled
      And the user is <UserType>
      When a user of type '<UserType>' requests a get rest call with the uri '<URI>'
      Then the status code is <StatusCode>
      Then response body contains '<Response>'
      Examples:
      | URI                                                          | StatusCode | UserType | Response    |
      | /auto-provisioning/v1/models/RadioNode?ossModelIdentity=true | 200        | ap_admin | ossModelIds |