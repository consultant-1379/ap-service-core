Feature: View project's nodes

  Scenario Outline: For unauthorized users, viewing a node should be blocked
    Given the system has one project with 1 node(s) of type <NodeType>
    And security is enabled
    And the user is <UserType>
    When the user requests a view of node 1 from project 1
    Then an error will occur
    And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType     | NodeType  |
      | ap_sec_admin | erbs      |
      | ap_sec_admin | RadioNode |
