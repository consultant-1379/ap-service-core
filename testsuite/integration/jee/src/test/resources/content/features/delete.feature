Feature: Delete a project or node

  Scenario Outline: For unauthorized users, deleting a project should be blocked
    Given the system has one project with 1 node(s) of type <NodeType>
    And security is enabled
    And the user is <UserType>
    When the user requests project 1 to be deleted
    Then an error will occur
    And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType     | NodeType  |
      | ap_sec_admin | erbs      |
      | ap_sec_admin | RadioNode |

  Scenario Outline: For unauthorized users, deleting a node should be blocked
    Given the system has one project with 1 node(s) of type <NodeType>
    And security is enabled
    And the user is <UserType>
    When the user requests node 1 from project 1 to be deleted
    Then an error will occur
    And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType     | NodeType  |
      | ap_sec_admin | erbs      |
      | ap_sec_admin | RadioNode |
