Feature: Download files from the system

  Scenario Outline: Service should download samples for all nodes when empty node type is specified
    Given the user wants to download the schemas and sample files for all node types
    And security is enabled
    And the user is <UserType>
    When the user requests the file containing the schemas and samples
    Then no error will occur
    And the downloaded file id will contain 'AP_SchemasAndSamples.zip'
    Examples: User is administrator
      | UserType |
      | ap_admin |
    Examples: User is operator
      | UserType    |
      | ap_operator |

  Scenario Outline: Service should raise an error if file is requested for a node that does exist, but has not been ordered
    Given the user wants to download the raw artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    But the node has not been ordered
    When the user requests the generated artifacts for the specified node
    Then an error will occur
    And the error message will contain 'No artifact found to download'
    Examples: erbs nodes
      | NodeType | ProjectName | NodeName |
      | erbs     | ERBSProject | ERBS001  |
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeName |
      | RadioNode | ECIMProject | ECIM001  |
