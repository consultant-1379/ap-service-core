Feature: Order for Expansion project archive

    Scenario Outline: Service should successfully order a valid expansion file even the node was not managed in AP
    Given the project and node <ProjectName> <NodeName> <NodeType>
      And node pre-exists in the NRM database
      And the user has an expansion file named '<FileName>'
    When the user orders expansion file
    Then no error will occur
      And the Node artifact MOs are created in order <ConfigurationFilesOrder> as defined in nodeInfo
    Examples:
      | ProjectName             | NodeName                   | NodeType       |  FileName                                       |     ConfigurationFilesOrder                          |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017          | RadioNode      |  valid_expansion_file_one_node.zip              |  1=radio,2=transport,3=unlockCell,4=optionalFeature  |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00018          | RadioNode      |  valid_expansion_file_full_attributes.zip       |  1=radio,2=transport,3=unlockCell,4=optionalFeature  |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017          | RadioNode      |  valid_expansion_file_one_node_strict_true.zip  |  1=config_1,2=amos_1,3=config_2                      |

    Scenario Outline: If the user is ordering an expansion file with node not exist in Nrm, the system should raise the appropriate error
    Given the user has an expansion file named '<FileName>'
    When the user orders expansion file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | FileName                          | ValidationMessage                     |
      | valid_expansion_file_one_node.zip | Node LTE01dg2ERBS00017 does not exist |

    Scenario Outline: If the user is ordering an expansion file with node not in sync, the system should raise the appropriate error
    Given the project and node <ProjectName> <NodeName> <NodeType>
      And node pre-exists in the NRM and AP database but not in sync
      And the user has an expansion file named '<FileName>'
    When the user orders expansion file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | ProjectName             | NodeName          | NodeType  | FileName                          | ValidationMessage |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | valid_expansion_file_one_node.zip | is Unsynchronized |

#    Scenario Outline: Service should successfully order a valid expansion file when node in valid state
#    Given the project and node <ProjectName> <NodeName> <NodeType>
#      And node pre-exists in the NRM and AP database in state <NodeStatus>
#      And the user has an expansion file named '<FileName>'
#    When the user orders expansion file
#    Then no error will occur
#    Examples:
#      | ProjectName             | NodeName          | NodeType  |  NodeStatus                | FileName                          |
#      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  EXPANSION_CANCELLED       | valid_expansion_file_one_node.zip |
#      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  EXPANSION_COMPLETED       | valid_expansion_file_one_node.zip |
#      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  EXPANSION_FAILED          | valid_expansion_file_one_node.zip |
#      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  INTEGRATION_COMPLETED     | valid_expansion_file_one_node.zip |
#      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  RECONFIGURATION_COMPLETED | valid_expansion_file_one_node.zip |

    Scenario Outline: If the user is ordering a valid expansion file but node in incorrect state, the system should raise the appropriate error
    Given the project and node <ProjectName> <NodeName> <NodeType>
      And node pre-exists in the NRM and AP database in state <NodeStatus>
      And the user has an expansion file named '<FileName>'
    When the user orders expansion file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | ProjectName             | NodeName          | NodeType  |  NodeStatus              | FileName                          |  ValidationMessage         |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  EXPANSION_STARTED       | valid_expansion_file_one_node.zip |  is in incorrect state for |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  EXPANSION_SUSPENDED     | valid_expansion_file_one_node.zip |  is in incorrect state for |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  READY_FOR_EXPANSION     | valid_expansion_file_one_node.zip |  is in incorrect state for |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode |  READY_FOR_ORDER         | valid_expansion_file_one_node.zip |  is in incorrect state for |

    Scenario Outline: If the user is ordering an invalid expansion file, the system should raise the appropriate error
    Given the project and node <ProjectName> <NodeName> <NodeType>
      And node pre-exists in the NRM and AP database
      And the user has an expansion file named '<FileName>'
    When the user orders expansion file
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples: Invalid artifacts in nodeinfo file for expansion
      | ProjectName             | NodeName          | NodeType  | FileName                                               | ValidationMessage                                 |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | invalid_expansion_file_one_node_artifacts_missing.zip  | referenced in nodeInfo.xml is not found           |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | invalid_expansion_file_one_node_artifacts_needless.zip | Invalid files present in project file             |
    Examples: schema validation failure in nodeinfo file for expansion
      | ProjectName             | NodeName          | NodeType  | FileName                                               | ValidationMessage                                 |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | invalid_expansion_file_node_info_schema.zip            | nodeInfo.xml failed to validate against schema    |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | invalid_expansion_file_artifacts_schema.zip            | licenseKeys.xml failed to validate against schema |
    Examples: Invalid project name for expansion
      | ProjectName             | NodeName          | NodeType  | FileName                                               | ValidationMessage                                 |
      | aBcdEFGhi_apProjectName | LTE01dg2ERBS00017 | RadioNode | valid_expansion_file_one_node.zip                      | it can not be managed by another project          |
    Examples: duplidate node name in same project
      | ProjectName             | NodeName          | NodeType  | FileName                                               | ValidationMessage                                 |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | RadioNode | duplicate_expansion_reconfig_node.zip                  | Duplicate node name                               |
    Examples: Unsupported node type for expansion
      | ProjectName             | NodeName          | NodeType  | FileName                                               | ValidationMessage                                 |
      | lciadm100_apTafProject3 | LTE01dg2ERBS00017 | ERBS      | valid_expansion_file_one_node.zip                      | Unsupported node type                             |

