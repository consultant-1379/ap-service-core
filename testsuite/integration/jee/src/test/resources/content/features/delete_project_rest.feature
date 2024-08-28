Feature: Delete project REST

  Scenario Outline: Delete an existent project
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | TestProject | Node000001 |
    And the rest assured properties are set
    And the rest workflow services are set
    When a user of type '<UserType>' makes a delete rest call with the uri '/auto-provisioning/v1/projects' and the request body <RequestBody>
    Then the status code is <StatusCode>
    And project 'TestProject' is deleted
    Examples:
      | NodeType  | StatusCode | UserType | RequestBody                                                 |
      | RadioNode | 204        | ap_admin | {"ignoreNetworkElement":false,"projectIds":["TestProject"]} |

  Scenario Outline: Delete an existent project using singular endpoint
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName  | NodeNames  |
      | <NodeType> | 1         | TestProject1 | Node000002 |
    And the rest assured properties are set
    And the rest workflow services are set
    When a user of type '<UserType>' makes a delete rest call with the uri '/auto-provisioning/v1/projects/TestProject1'
    Then the status code is <StatusCode>
    And project 'TestProject1' is deleted
    Examples:
      | NodeType  | StatusCode | UserType |
      | RadioNode | 204        | ap_admin |
