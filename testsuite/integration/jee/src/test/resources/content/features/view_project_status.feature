Feature: View a project's status

    Scenario Outline: For unauthorized users, requesting the status of a project should throw an error
    Given the system has one project with 1 node(s) of type <NodeType>
      And security is enabled
      And the user is <UserType>
     When the user requests the status of project 1
     Then an error will occur
      And the error will have type 'SecurityViolationException'
    Examples: User is security administrator
      | UserType      | NodeType  |
      | ap_sec_admin  | erbs      |
      | ap_sec_admin  | RadioNode |
