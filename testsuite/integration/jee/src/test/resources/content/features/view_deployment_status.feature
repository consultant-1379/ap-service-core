Feature: View a deployments's status

    Scenario Outline: Viewing the status of a deployment with 4 nodes should give the correct outcome for authorized users
    Given the system has one project with 4 node(s) of type <NodeType>
      And node 1 from project 1 is in state 'READY_FOR_ORDER'
      And node 2 from project 1 is in state 'ORDER_STARTED'
      And node 3 from project 1 is in state 'ORDER_COMPLETED'
      And node 4 from project 1 is in state 'ORDER_FAILED'
      And node 1 from project 1 will have deployment set to 'defaultDeployment'
      And node 2 from project 1 will have deployment set to 'defaultDeployment'
      And node 3 from project 1 will have deployment set to 'diffDeployment'
      And node 4 from project 1 will have deployment set to 'defaultDeployment'
      And security is enabled
      And the user is <UserType>
     When the user requests the status of deployment 'defaultDeployment'
     Then no error will occur
      And the status will inform the deployment has 3 nodes
      And the status will inform that node 1 is in state 'READY_FOR_ORDER'
      And the status will inform that node 2 is in state 'ORDER_STARTED'
      And the status will inform that node 3 is in state 'ORDER_COMPLETED'
      And the status summary will say there are 2 nodes in phase 'IN_PROGRESS'
      And the status summary will say there is 1 node in phase 'FAILED'
    Examples: User is administrator
      | UserType    | NodeType  |
      | ap_admin    | erbs      |
      | ap_admin    | RadioNode |
    Examples: User is operator
      | UserType    | NodeType  |
      | ap_operator | erbs      |
      | ap_operator | RadioNode |

    Scenario Outline: Viewing the status of an invalid deployment returns error message
    Given the system has one project with 4 node(s) of type <NodeType>
      And node 1 from project 1 is in state 'READY_FOR_ORDER'
      And node 2 from project 1 is in state 'ORDER_STARTED'
      And node 3 from project 1 is in state 'ORDER_COMPLETED'
      And node 4 from project 1 is in state 'ORDER_FAILED'
      And security is enabled
      And the user is <UserType>
     When the user requests the status of deployment 'defaultDeployment'
     Then no error will occur
      And the status will inform the deployment has 0 nodes
    Examples: User is administrator
      | UserType    | NodeType  |
      | ap_admin    | erbs      |
      | ap_admin    | RadioNode |
    Examples: User is operator
      | UserType    | NodeType  |
      | ap_operator | erbs      |
      | ap_operator | RadioNode |