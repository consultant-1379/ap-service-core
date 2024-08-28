Feature: View several projects' details rest

  Scenario Outline: Get Projects from Rest
    Given the system has one project with 1 node(s) of type <NodeType>
    And the system has another project with 1 node(s) of type <NodeType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                              | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects?filter=properties | 200        | ap_admin |
      | ERBS      | /auto-provisioning/v1/projects?filter=properties | 200        | ap_admin |

  Scenario Outline: Unable to get Projects from Rest using invalid filter
    Given the system has one project with 1 node(s) of type <NodeType>
    And the system has another project with 1 node(s) of type <NodeType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                                 | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects?filter=invalidfilter | 400        | ap_admin |
      | ERBS      | /auto-provisioning/v1/projects?filter=invalidfilter | 400        | ap_admin |

  Scenario Outline: Server unable to Get Projects from REST request due to service exception
    Given the system has one project with 1 node(s) of type <NodeType>
    And the rest assured properties are set
    When a service exception occurs on the server
    And a user of type '<UserType>' requests a rest call with the uri '<URI>'
    Then the status code is <StatusCode>
    Examples:
      | NodeType  | URI                                              | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects?filter=properties | 500        | ap_admin |
