Feature: Order Replace project archive

    Scenario Outline: Service should successfully order a valid replace file
    Given the project <ProjectName> and node <NodeName> with node type <NodeType>
      And node pre-exists in the NRM database
      And the user has a replace file named '<FileName>'
    When the user orders replace file
    Then no error will occur
      And the system will have created only a siteInstallation file in raw directory

    Examples:
      | ProjectName     | NodeName              | NodeType  |  FileName                        |
      | replace_project | hardware_replace_node | RadioNode |  valid_replace_file_one_node.zip |

    Scenario Outline: If the user is ordering a replace file and the node does not exist in Nrm, the system should raise the appropriate error
    Given the user has a replace file named '<FileName>'
    When the user orders replace file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | FileName                        | ValidationMessage                         |
      | valid_replace_file_one_node.zip | Node hardware_replace_node does not exist |

    Scenario Outline: If the user is ordering a invalid replace file, the system should raise the appropriate error
    Given the project <ProjectName> and node <NodeName> with node type <NodeType>
      And node pre-exists in the NRM database
      And the user has a replace file named '<FileName>'
    When the user orders replace file
    Then an error will occur
     And the validation error message will contain '<ValidationMessage>'
    Examples: schema validation failure in nodeinfo file for replace
      | ProjectName             | NodeName              | NodeType  | FileName                            | ValidationMessage                              |
      | replace_project         | invalid_replace_node  | RadioNode | invalid_node_info_replace_file.zip  | nodeInfo.xml failed to validate against schema |
    Examples: duplicate node name in same project
      | ProjectName             | NodeName              | NodeType  | FileName                            | ValidationMessage                              |
      | replace_project         | hardware_replace_node | RadioNode | duplicate_replace_node.zip          | Duplicate node name                            |
    Examples: Unsupported node type for replace
      | ProjectName             | NodeName              | NodeType  | FileName                            | ValidationMessage                              |
      | replace_project         | hardware_replace_node | ERBS      | valid_replace_file_one_node.zip     | Unsupported node type                          |

    Scenario Outline: Service should not perform a hardware replace with a specific backup if the option is not supported for the node type
    Given the project <ProjectName> and node <NodeName> with node type <NodeType>
     And the user has a replace file named '<FileName>'
     And node pre-exists in the NRM database
    When the user orders replace file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples: RadioNode nodes
      | ProjectName     | NodeType | NodeName              | FileName                        | ValidationMessage                                      |
      | replace_project | MSRBS_V1 | hardware_replace_node | valid_replace_file_one_node.zip | nodeInfo.xml failed to validate against schema         |
