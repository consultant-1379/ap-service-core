Feature: Create project via REST

  Scenario Outline: Service should return validation error when project data is invalid

    Given the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    And response body contains 'Validation error(s) occurred.'
    Examples:
      | URI                            | StatusCode | NodeType  | UserType | Body                                                                  |
      | /auto-provisioning/v1/projects | 400        | RadioNode | ap_admin | { "name":"Project 1","description":"Some description","creator":"jo"} |

  Scenario Outline: Service should create project when data is valid

    Given the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    And the project Project1 exists
    And response body contains 'Project1'
    Examples:
      | URI                            | StatusCode | NodeType  | UserType | Body                                                                   |
      | /auto-provisioning/v1/projects | 201        | RadioNode | ap_admin | { "name":"Project1","description":"Some description","creator":"john"} |

  Scenario Outline: Service should return validation error when project name is taken
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | PROJECT1    | NODE000001 |
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    And response body contains 'already exists'
    Examples:
      | URI                            | StatusCode | NodeType  | UserType | Body                                                                   |
      | /auto-provisioning/v1/projects | 409        | RadioNode | ap_admin | { "name":"PROJECT1","description":"Some description","creator":"john"} |
