Feature: Bind Single Node via REST

  Scenario Outline: Service should bind single node and return successful status code
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT | NODE000001 |
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    And the bind node flow will be stubbed for node 1 from project 1
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    Examples:
      | URI                                                                      | StatusCode | NodeType  | UserType    | Body                         |
      | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 204        | RadioNode | ap_admin    | { "hardwareId":"ABC1234567"} |
      | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 204        | RadioNode | ap_operator | { "hardwareId":"ABC1234567"} |

  Scenario Outline: Service should not bind if the hardwareId is already bound
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT_1 | NODE000001 |
      | <NodeType> | 1         | BINDPROJECT_2 | NODE000002 |
    And the rest assured properties are set
    And security is enabled
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    And node 1 from project 2 is in state 'ORDER_COMPLETED'
    And node 1 from project 2 has hardwareSerialNumber 'ABC1234567'
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    And node 1 from project 1 will still be in state 'ORDER_COMPLETED'
    Examples: RadioNode nodes
      | NodeType  | URI                                                                        | StatusCode | UserType | Body                          |
      | RadioNode | /auto-provisioning/v1/projects/BINDPROJECT_1/nodes/NODE000001/actions/bind | 409        | ap_admin | { "hardwareId":"ABC1234567" } |
      | erbs      | /auto-provisioning/v1/projects/BINDPROJECT_1/nodes/NODE000001/actions/bind | 409        | ap_admin | { "hardwareId":"ABC1234567" } |

  Scenario Outline: Service should not bind if the serial number is invalid
    Given the system has the following project(s):
      | NodeType | NodeCount | ProjectName | NodeNames  |
      | erbs     | 1         | BINDPROJECT | NODE000001 |
    And the rest assured properties are set
    And security is enabled
    And the user is <UserType>
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    Examples: Invalid hardware serial numbers
      | URI                                                                      | StatusCode | UserType | Body                            |
      | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 400        | ap_admin | {"hardwareId":"ABCDABC1234567"} |

  Scenario Outline: Service should not bind if the node is in an invalid state
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT | NODE000001 |
    But node 1 from project 1 is in state '<NodeState>'
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    And node 1 from project 1 will still be in state '<NodeState>'
    Examples: Invalid erbs node states
      | NodeType  | NodeState             | UserType | URI                                                                      | StatusCode | Body                        |
      | erbs      | READY_FOR_ORDER       | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | ORDER_STARTED         | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | ORDER_FAILED          | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | ORDER_ROLLBACK_FAILED | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | DELETE_STARTED        | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | BIND_STARTED          | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | INTEGRATION_STARTED   | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | erbs      | INTEGRATION_COMPLETED | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | READY_FOR_ORDER       | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | ORDER_STARTED         | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | ORDER_FAILED          | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | ORDER_ROLLBACK_FAILED | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | DELETE_STARTED        | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | BIND_STARTED          | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | INTEGRATION_STARTED   | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |
      | RadioNode | INTEGRATION_COMPLETED | ap_admin | /auto-provisioning/v1/projects/BINDPROJECT/nodes/NODE000001/actions/bind | 403        | {"hardwareId":"ABC1234567"} |


  Scenario Outline: Service should not bind if the node type is not supported
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | VPPPROJECT  | NODE000001 |
    And the rest assured properties are set
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    Examples: Invalid nodeType
      | URI                                                                     | StatusCode | UserType | Body                        | NodeType |
      | /auto-provisioning/v1/projects/VPPPROJECT/nodes/NODE000001/actions/bind | 403        | ap_admin | {"hardwareId":"ABC1234567"} | vPP      |

  Scenario Outline: Service should fail correctly if the node does not exist
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT | NODE000001 |
    And the rest assured properties are set
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    When a user of type '<UserType>' makes a put rest call with the uri '<URI>' with body '<Body>' using node type '<NodeType>'
    Then the status code is <StatusCode>
    Examples: Invalid nodeType
      | URI                                                                         | StatusCode | UserType | Body                        | NodeType  |
      | /auto-provisioning/v1/projects/BINDPROJECT/nodes/InvalidNode/actions/bind   | 404        | ap_admin | {"hardwareId":"ABC1234567"} | RadioNode |
      | /auto-provisioning/v1/projects/INVALIDPROJECT/nodes/NODE000001/actions/bind | 404        | ap_admin | {"hardwareId":"ABC1234567"} | RadioNode |
