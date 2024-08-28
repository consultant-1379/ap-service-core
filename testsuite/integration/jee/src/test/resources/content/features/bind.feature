Feature: Binding

  Scenario Outline: Service should validate correctly the csv before doing a bind, failing with the correct message
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT | NODE000001 |
    And node 1 from project 1 is in state 'ORDER_STARTED'
    And the user has a csv file with '<FileContent>'
    When the user requests a batch bind
    Then no error will occur
    And there will be 0 successful bind(s) and 1 failed bind(s)
    And the system list of errors will contain '<ErrorMessage>'
    Examples: erbs nodes
      | NodeType | FileContent           | ErrorMessage                                                                       |
      | erbs     | NotExist,ABC1234567   | Line 1 - Node does not exist                                                       |
      | erbs     | ,ABC1234567           | Line 1 - Missing mandatory value for node name                                     |
      | erbs     | NODE000001,ABC1234567 | Line 1 - Node is not in the correct state to perform the operation [Order Started] |
      | erbs     | NODE000001,           | Line 1 - Missing mandatory value for hardware serial number                        |
    Examples: RadioNode nodes
      | NodeType  | FileContent           | ErrorMessage                                                                       |
      | RadioNode | NotExist,ABC1234567   | Line 1 - Node does not exist                                                       |
      | RadioNode | ,ABC1234567           | Line 1 - Missing mandatory value for node name                                     |
      | RadioNode | NODE000001,ABC1234567 | Line 1 - Node is not in the correct state to perform the operation [Order Started] |
      | RadioNode | NODE000001,           | Line 1 - Missing mandatory value for hardware serial number                        |

  Scenario Outline: For authorized users, for batch bind, service should handle problems and bind correctly the lines without problems
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT | NODE000001 |
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    And the bind node flow will be stubbed for node 1 from project 1
    And the '<UserType>' has a csv file with
        """
        NODE000001,ABC1234567
        NODE000002,ABC1234568
        ,ABC1234569
        NODE000003,
        """
    When the user requests a batch bind
    And security is enabled
    And the user is <UserType>
    Then no error will occur
    And there will be 1 successful bind(s) and 3 failed bind(s)
    Examples: User is limited remote access operator
      | UserType                          | NodeType |
      | ap_limited_remote_access_operator | erbs     |
    Examples: User is administrator
      | UserType | NodeType  |
      | ap_admin | RadioNode |

  Scenario Outline: For unauthorized users, bind should be blocked
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | BINDPROJECT_1 | NODE000001 |
    And node 1 from project 1 is in state 'ORDER_COMPLETED'
    And security is enabled
    And the user is <UserType>
    When the user requests a bind on node 1 from project 1 with hardwareSerialNumber 'ABC1234567'
    Then an error will occur
    And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType  | NodeType |
      | sec_admin | erbs     |

