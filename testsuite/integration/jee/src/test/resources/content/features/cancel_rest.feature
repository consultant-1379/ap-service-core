Feature: Cancel of a node through REST

    Scenario Outline: Cancel should succeed when called through rest for valid node in correct state
    Given the system has the following project(s):
        | NodeType   | NodeCount | ProjectName   | NodeNames  |
        | <NodeType> | 1         | <ProjectName> | <NodeName> |
      And node 1 from project 1 is in state '<ApState>'
      And success workflow stubs set up for node 1 from project 1
      And the user is <UserType>
      And the rest assured properties are set
    When a user of type '<UserType>' requests a post rest call with the uri '<URI>'
      Then the status code is <StatusCode>
      And no error will occur
    Examples:
      | URI                                                                          | StatusCode | ProjectName | NodeName | NodeType  | UserType | ApState                 |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/actions/cancel |        204 | TestProject | Node01   | RadioNode | ap_admin | INTEGRATION_STARTED     |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/actions/cancel |        204 | TestProject | Node01   | RadioNode | ap_admin | EXPANSION_STARTED       |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/actions/cancel |        204 | TestProject | Node01   | RadioNode | ap_admin | RECONFIGURATION_STARTED |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/actions/cancel |        204 | TestProject | Node01   | vPP       | ap_admin | INTEGRATION_STARTED     |


  Scenario Outline: Cancel should fail when called through rest for invalid nodes
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | <NodeName> |
    And the user is <UserType>
    And error workflow stubs set up for node 1 from project 1
    And the rest assured properties are set
    And node 1 from project 1 is in state '<ApState>'
    When a user of type '<UserType>' requests a post rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Then response body contains '<ErrorMessage>'
    And no error will occur
    Examples:
      | URI                                                                               | StatusCode | ProjectName | NodeName | NodeType  | UserType | ApState               | ErrorMessage |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/actions/cancel      |        403 | TestProject | Node01   | ERBS      | ap_admin | INTEGRATION_COMPLETED | Unsupported action for this node type |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/InvalidNodeName/actions/cancel |        404 | TestProject | Node01   | RadioNode | ap_admin | INTEGRATION_STARTED   | Node does not exist |
      | /auto-provisioning/v1/projects/InvalidProjectName/nodes/<NodeName>/actions/cancel |        404 | TestProject | Node01   | RadioNode | ap_admin | INTEGRATION_STARTED   | Project does not exist |
