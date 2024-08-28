Feature: View project's properties via REST

  Scenario Outline: GET project properties from REST successfully
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | NodeType  | URI                                                          | StatusCode | ProjectName | NodeName | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject?filter=properties | 200        | TestProject | Node01   | ap_admin |
      | ERBS      | /auto-provisioning/v1/projects/TestProject?filter=properties | 200        | TestProject | Node01   | ap_admin |

  Scenario Outline: Query project unsuccessfully with an invalid filter
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                             | StatusCode | ProjectName | NodeName | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject?filter=invalidfilter | 400        | TestProject | Node01   | ap_admin |
      | ERBS      | /auto-provisioning/v1/projects/TestProject?filter=invalidfilter | 400        | TestProject | Node01   | ap_admin |

  Scenario Outline: Get project properties from REST throws 404 project not found exception
    Given the system has one project with 1 node(s) of type <NodeType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                                   | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/unknown-project-name?filter=properties | 404        | ap_admin |

  Scenario Outline: Get project properties from REST throws 500 internal server error exception
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames |
      | <NodeType> | 1         | TestProject | Node00001 |
    And the rest assured properties are set
    When an internal service exception occurs on the server while querying a project
    And a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                          | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject?filter=properties | 500        | ap_admin |

  Scenario Outline: Get project status from REST successfully
    Given the system has the following project(s):
      | NodeType  | NodeCount | ProjectName   | NodeNames                   |
      | RadioNode | 4         | <ProjectName> | Node01,Node02,Node03,Node04 |
    And the rest assured properties are set
    And node with name 'Node01' from project 1 is in state 'READY_FOR_ORDER'
    And node with name 'Node02' from project 1 is in state 'ORDER_STARTED'
    And node with name 'Node03' from project 1 is in state 'ORDER_COMPLETED'
    And node with name 'Node04' from project 1 is in state 'ORDER_FAILED'
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    And response object attribute 'id' is equal to '<ProjectName>'
    And response object attribute 'nodeSummary[0].id' is equal to 'Node01'
    And response object attribute 'nodeSummary[0].state' is equal to 'Ready for Order'
    And response object attribute 'nodeSummary[1].id' is equal to 'Node02'
    And response object attribute 'nodeSummary[1].state' is equal to 'Order Started'
    And response object attribute 'nodeSummary[2].id' is equal to 'Node03'
    And response object attribute 'nodeSummary[2].state' is equal to 'Order Completed'
    And response object attribute 'nodeSummary[3].id' is equal to 'Node04'
    And response object attribute 'nodeSummary[3].state' is equal to 'Order Failed'
    Examples:
      | URI                                              | ProjectName       | StatusCode | UserType |
      | /auto-provisioning/v1/projects/TestStatusProject | TestStatusProject | 200        | ap_admin |

  Scenario Outline: Get project status from REST throws 404 project not found exception
    Given the system has one project with 1 node(s) of type <NodeType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                 | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/unknown-project-name | 404        | ap_admin |

  Scenario Outline: Get project status from REST throws 500 internal server error exception
    Given the system has the following project(s):
      | NodeType  | NodeCount | ProjectName | NodeNames |
      | RadioNode | 1         | TestProject | Node00001 |
    And the rest assured properties are set
    When an internal service exception occurs on the server while querying a project
    And a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                        | StatusCode | UserType |
      | /auto-provisioning/v1/projects/TestProject | 500        | ap_admin |
