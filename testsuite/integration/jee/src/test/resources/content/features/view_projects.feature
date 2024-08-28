Feature: View several projects' details

    Scenario Outline: For unauthorized users, viewing projects should be blocked
    Given the system has one project with 1 node(s) of type <NodeType>
      And the system has another project with 1 node(s) of type <NodeType>
      And security is enabled
      And the user is <UserType>
     When the user requests to view all projects
     Then an error will occur
      And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType      | NodeType  |
      | ap_sec_admin  | erbs      |
      | ap_sec_admin  | RadioNode |
