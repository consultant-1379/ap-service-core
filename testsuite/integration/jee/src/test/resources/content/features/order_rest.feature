Feature: Order integration for archive through REST received from node-plugin

  Scenario Outline: Service should allow authorized users to import a valid project archive through REST
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples: Project and REST details
      | ProjectName       | ProfileName | UserType | URI                                                             | StatusCode | FilePath                      |
      | ArquillianProject | Profile1    | ap_admin | /auto-provisioning/v1/projects/ArquillianProject/actions/order  |        201 | json/profile/ect_configs.json |

  Scenario Outline: Service should throw ValidationException if the project is malformed
    Given the system has a project '<ProjectName>' with one profile '<ProfileName>'
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a post rest call with the uri '<URI>' with json body from file '<FilePath>'
    Then the status code is <StatusCode>
    Examples: Project and REST details
      | ProjectName      | ProfileName | UserType | URI                                                           | StatusCode | FilePath                              |
      | MalformedProject | Profile1    | ap_admin | /auto-provisioning/v1/projects/MalformedProject/actions/order |        417 | json/profile/ect_configs_invalid.json |

  Scenario Outline: Order node via REST
    Given the system has the following project(s):
      | NodeType | NodeCount | ProjectName | NodeNames |
      | ERBS     | 1         | TestProject | Node01    |
    And the user is <UserType>
    And the rest workflow services are set
    And security is enabled
    And the rest assured properties are set
    And node 1 from project 1 is in state 'ORDER_FAILED'
    When a user of type '<UserType>' makes a post rest call with the uri '/auto-provisioning/v1/projects/TestProject/nodes/actions/order' with body '<RequestBody>' using node type 'ERBS'
    Then the status code is <StatusCode>
    And response body is equal to <ResponseBody>
    Examples:
      | StatusCode | UserType | RequestBody                           | ResponseBody                                                              |
      | 202        | ap_admin | {"nodeIds": ["Node01"]}               |                                                                           |
      | 207        | ap_admin | {"nodeIds": ["ThisNodeDoesNotExist"]} | [{"nodeId":"ThisNodeDoesNotExist","errorMessage":"Node does not exist."}] |
