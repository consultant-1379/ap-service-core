Feature: Delete node REST

  Scenario Outline: Delete an existent node
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | TestProject | Node000001 |
    And the rest assured properties are set
    And the rest workflow services are set
    When a user of type '<UserType>' makes a delete rest call with the uri '/auto-provisioning/v1/projects/TestProject/nodes' and the request body <RequestBody>
    Then the status code is <StatusCode>
    And node 'Node000001' in project 'TestProject' is deleted
    Examples:
      | NodeType  | StatusCode | UserType     | RequestBody                                             |
      | RadioNode | 204        | ap_admin     | {"ignoreNetworkElement":false,"nodeIds":["Node000001"]} |

  Scenario Outline: Deleting a node should succeed if smrs account fails to be deleted
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | TestProject | Node000001 |
    And the rest assured properties are set
    And the rest workflow services are set
    And the system has an smrs account that will fail to delete
    When a user of type '<UserType>' makes a delete rest call with the uri '/auto-provisioning/v1/projects/TestProject/nodes' and the request body <RequestBody>
    Then the status code is <StatusCode>
    And node 'Node000001' in project 'TestProject' is deleted
    Examples:
      | NodeType  | StatusCode | UserType | RequestBody                                             |
      | RadioNode | 204        | ap_admin | {"ignoreNetworkElement":false,"nodeIds":["Node000001"]} |

    Scenario Outline: Delete an existent node using the singular endpoint
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | TestProject | Node000001 |
    And the rest assured properties are set
    And the rest workflow services are set
    When a user of type '<UserType>' makes a delete rest call with the uri '/auto-provisioning/v1/projects/TestProject/nodes/Node000001'
    Then the status code is <StatusCode>
    And node 'Node000001' in project 'TestProject' is deleted
    Examples:
      | NodeType  | StatusCode | UserType |
      | RadioNode | 204        | ap_admin |
