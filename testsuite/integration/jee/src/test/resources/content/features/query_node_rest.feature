Feature: View node properties rest

  Scenario Outline: Get node status from REST successfully
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | RadioNode  | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is 200
    And no error will occur
    Examples:
      | URI                                                                          | ProjectName       | NodeName | UserType |
      | /auto-provisioning/v1/projects/TestStatusProject/nodes/Node01?filter=status  | TestStatusProject | Node01   | ap_admin |
      | /auto-provisioning/v1/projects/TestStatusProject/nodes/Node01                | TestStatusProject | Node01   | ap_admin |

  Scenario Outline: Get project status from REST throws 500 internal server error exception
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | RadioNode  | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When an internal server error occurs for viewing node status
    And a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                         | StatusCode | ProjectName       | NodeName | UserType |
      | /auto-provisioning/v1/projects/TestStatusProject/nodes/Node01?filter=status | 500        | TestStatusProject | Node01   | ap_admin |

  Scenario Outline: Get Nodes from Rest
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | RadioNode  | 1         | TestProject | Node000001 |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                              | StatusCode | UserType |
      | /auto-provisioning/v1/projects/TestProject/nodes | 200        | ap_admin |

  Scenario Outline: Get Node Properties from Rest
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                       | StatusCode | ProjectName | NodeName | NodeType  | UserType |
      | /auto-provisioning/v1/projects/TestProject/nodes/Node01?filter=properties | 200        | TestProject | Node01   | RadioNode | ap_admin |
      | /auto-provisioning/v1/projects/TestProject/nodes/Node01?filter=properties | 200        | TestProject | Node01   | ERBS      | ap_admin |

  Scenario Outline: Query node unsuccessfully with an invalid filter
    Given the system has the following project(s):
      | NodeType  | NodeCount | ProjectName   | NodeNames  |
      | RadioNode | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                                           | StatusCode | ProjectName       | NodeName | UserType |
      | /auto-provisioning/v1/projects/TestStatusProject/nodes/invalid-node-name?filter=invalidfilter | 400        | TestStatusProject | Node01   | ap_admin |

  Scenario Outline: Get node properties from REST throws 500 internal server error exception
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames |
      | <NodeType> | 1         | TestProject | Node00001 |
    And the rest assured properties are set
    When an internal server error occurs for viewing node properties
    And a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                                          | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node00001?filter=properties | 500        | ap_admin |

  Scenario Outline: Query node fails correctly when MO does not exist
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | RadioNode  | 1         | TestProject | Node01     |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And response body contains '<ErrorMessage>'
    Examples:
      | URI                                                                              | StatusCode | UserType | ErrorMessage           |
      | /auto-provisioning/v1/projects/TestProject/nodes/InvalidNode?filter=properties   | 404        | ap_admin | Node does not exist    |
      | /auto-provisioning/v1/projects/InvalidProject/nodes/Node01?filter=properties     | 404        | ap_admin | Project does not exist |
      | /auto-provisioning/v1/projects/TestStatusProject/nodes/InvalidNode?filter=status | 404        | ap_admin | Node does not exist    |
      | /auto-provisioning/v1/projects/InvalidProject/nodes/Node01?filter=status         | 404        | ap_admin | Project does not exist |
