Feature: Order integration for project archive

    Scenario Outline: Service should allow authorized users to order a valid project archive
    Given the user has a zip file named 'ORDER_001.ZIP' for a project of type <NodeType>
      And project 1 has 2 nodes with default configuration
      And security is enabled
      And the user is <UserType>
    When the user orders project archive 1
    Then no error will occur
      And the node state for each node in project 1 is 'ORDER_STARTED' (wait timeout: 8000ms)
      And the wfsInstanceId for each node in project 1 is not empty
    Examples: erbs nodes and ap_admin
      | NodeType  | UserType     |
      | erbs      | ap_operator  |
    Examples: RadioNode nodes and ap_operator
      | NodeType  | UserType     |
      | RadioNode | ap_admin     |

    Scenario Outline: Service should block unauthorized users from ordering a project archive
    Given the user has a zip file named 'ORDER_002.ZIP' for a project of type <NodeType>
      And project 1 has 2 nodes with default configuration
      And security is enabled
      And the user is <UserType>
     When the user orders project archive 1
     Then an error will occur
    Examples: RadioNode nodes and ap_invalid
      | NodeType  | UserType     |
      | RadioNode | ap_invalid   |
    Examples: erbs nodes and limited remote access operator
      | NodeType | UserType                          |
      | erbs     | ap_limited_remote_access_operator |


    Scenario Outline: Service should initiate order for all nodes when workflow service is unavailable
    Given the user has a zip file named 'ORDER_003.ZIP' for a project of type <NodeType>
      And project 1 has 2 nodes with default configuration
      And workflow service is unavailable
     When the user orders project archive 1
     Then no error will occur
      And the node state for each node in project 1 is 'ORDER_FAILED' (wait timeout: 8000ms)
      And the wfsInstanceId for each node in project 1 is empty
    Examples: erbs nodes
      | NodeType  |
      | erbs      |

    Scenario Outline: Service should successfully order a valid batch file
    Given the user has a batch file named '<FileName>'
     When the user orders batch file
     Then no error will occur
    Examples:
      | FileName                         |
      | valid_batch_file_one_node.zip    |
      | valid_batch_file_three_nodes.zip |
      | valid_radio_batch_file.zip       |

    Scenario Outline: Service should successfully order a valid standard greenfield file
     Given the project and node <ProjectName> <NodeName> <NodeType> pre-exists user has a project zip file named '<FileName>'
     When the user orders standard greenfield file
     Then no error will occur
      And the Node artifact MOs are created in <ConfigurationFilesOrder> as defined in nodeInfo
    Examples:
      | ProjectName             | NodeName                   | NodeType       |  FileName                                             |     ConfigurationFilesOrder                          |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00016          | RadioNode      |  valid_standard_greenfield_file_one_node.zip          |  4=radio,5=transport,6=unlockCell,7=optionalFeature  |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00016          | RadioNode      |  valid_standard_greenfield_file_one_node_netconf.zip  |  4=radio,5=transport,6=unlockCell,7=optionalFeature  |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00016          | RadioNode      |  integration_artifact_files_in_order_strict_false.zip |  4=config_1,5=amos_1,6=config_2                      |
      | lciadm100_projectC6610  | Transport121Controller00001| Controller6610 |  Transport121Controller00001_project_strict_true.zip  |  4=config_1,5=amos_1,6=config_2                      |

    Scenario Outline: Service should successfully order a valid mixed project file without referenced project existing
    Given the project and node <ProjectName> <NodeName> <NodeType> pre-exists user has a project zip file named '<FileName>'
     When the user orders mixed project file without referenced project existing
     Then no error will occur
    Examples:
      | ProjectName             | NodeName           | NodeType    |  FileName             |
      | lciadm100_apTafProject5 | LTE01dg2ERBS00017  | RadioNode   |  mixed_project.zip    |

    Scenario Outline: Service should successfully order a valid mixed project file with referenced project existing
    Given the project and node <ProjectName> <NodeName> <NodeType> pre-exists user has a project zip file named '<FileName>'
     When the user orders mixed project file with referenced project existing
     Then no error will occur
    Examples:
      | ProjectName             | NodeName           | NodeType    |  FileName             |
      | lciadm100_apTafProject5 | LTE01dg2ERBS00017  | RadioNode   |  mixed_project.zip    |

    Scenario Outline: Service should not order any nodes if project archive is not valid.
    Given the user has a zip file named 'ORDER_04.zip' for a project of type <NodeType>
     And project 1 is named '<ProjectName>'
     And project 1 has its nodes configured like this:
      | NodeName | IpAddress   | NodeIdentifier   | NodeArtifacts   | HardwareSerialNumber   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
      | N001     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | B123456789             | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
      | N002     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | B123456788             | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
     When the user orders project archive 1 with workflow service
     Then an error will occur
      And the validation error message will be 'N002 - Duplicate IP address <IpAddress>'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | IpAddress   | NodeIdentifier  | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 10.10.10.1  | 6607-651-025    | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should order invalid node if -nv option is used
    Given the user has a zip file named 'ORDER_05.zip' for a project of type <NodeType>
     And project 1 is named '<ProjectName>'
     And project 1 has its nodes configured like this:
      | NodeName | IpAddress   | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
      | N010     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
      | N011     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
    When the user orders project archive no validation 1 with workflow service
    Then no error will occur
     And the node state for each node in project 1 is 'ORDER_STARTED' (wait timeout: 8000ms)
     And the wfsInstanceId for each node in project 1 is not empty
    Examples: RadioNode nodes
      | NodeType  | ProjectName | IpAddress   | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                   |
      | RadioNode | ECIMProject | 10.10.10.2  | 18.Q4-R57A02      | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |

    Scenario Outline: If the user is ordering an invalid batch file, the system should raise the appropriate error
    Given the user has a batch file named '<FileName>'
    When the user orders batch file
    Then an error will occur
     And the validation error message will be '<ValidationMessage>'
    Examples:
      | FileName                                                | ValidationMessage                                                                                                                 |
      | invalid_batch_csv_empty.zip                             | csv substitution file NodeValues.csv is empty (both header and data row required)                                                 |
      | invalid_batch_file_csv_duplicate_headers.zip            | Duplicate header(s) [NodeIpAddress] found in csv substitution file                                                                |
      | invalid_batch_file_csv_header_mismatch.zip              | The number of data values do not match the number of headers in NodeValues.csv                                                    |
      | invalid_batch_file_csv_multiple_duplicate_node_name.zip | Duplicate node name(s) [Tullamore, Athlone] found in csv substitution file                                                        |
      | invalid_batch_file_csv_node_name_empty.zip              | Empty node name found in csv substitution file at row(s) [1]                                                                      |
      | invalid_batch_file_csv_single_duplicate_node_name.zip   | Duplicate node name(s) [Tullamore] found in csv substitution file                                                                 |
      | invalid_batch_file_no_data_row.zip                      | csv substitution file NodeValues.csv contains no data, only presumed header row                                                   |
      | invalid_batch_file_no_matching_header.zip               | Cannot substitute element tag(s) [NodeIpAddress] from file nodeInfo.xml. Expected matching substitution header(s) not in csv file |
      | invalid_batch_file_node_info_without_name_tag.zip       | Mandatory variable substitution of element <name> in nodeInfo.xml is not present                                                  |
      | invalid_batch_file_nodeinfo_without_substitution.zip    | Mandatory variable substitution of element <name> in nodeInfo.xml is not present                                                  |
      | invalid_batch_file_with_multiple_csvs.zip               | Multiple csv substitution files found in invalid_batch_file_with_multiple_csvs.zip                                                |
      | invalid_batch_file_without_nodeinfo.zip                 | nodeInfo.xml is not found in invalid_batch_file_without_nodeinfo.zip                                                              |
      | invalid_batch_file_without_projectinfo.zip              | No projectInfo.xml file found in project file                                                                                    |

    Scenario Outline: If the user is ordering an invalid batch file, the system should raise the appropriate error
    Given the user has a batch file named '<FileName>'
    When the user orders batch file
    Then an error will occur
     And the validation error message will contain '<ValidationMessage>'
    Examples:
      | FileName                                | ValidationMessage                                                                                      |
      | invalid_batch_duplicate_ip_address.zip  | Mullingar - Duplicate IP address 1.2.30.41                                                             |
      | invalid_batch_invalid_node.zip          | nodeInfo.xml failed to validate against schema. Value 'x.y' is not facet-valid with respect to pattern |

    Scenario Outline: If the user is ordering invalid batch file using -nv order will still be kicked off
    Given the user has a batch file named '<FileName>'
    When the user orders a batch file no validation
    Then no error will occur
    Examples:
      | FileName                               |
      | invalid_batch_duplicate_ip_address.zip |

  Scenario Outline: If the user is ordering valid batch file using -nv order will still be kicked off
    Given the user has a batch file named '<FileName>'
    When the user orders a batch file no validation
    Then no error will occur
    And node 'Project=<ProjectName>,Node=<NodeName>' will exist
    Examples:
      | FileName                   | ProjectName | NodeName |
      | valid_radio_batch_file.zip | SouthWest   | Athlone  |

    Scenario Outline: If the user is ordering an invalid batch file, the system should raise the appropriate error
    Given the user has a batch file named '<FileName>'
    When the user orders batch file
    Then an error will occur
     And validate the CSV for the filename '<FileName>', validation message '<ValidationMessage>'
    Examples:
      | FileName                                       | ValidationMessage                                                                                                                |
      | invalid_batch_no_csv_no_subs_substitution.zip  | No valid substitution variables found.                                                                                           |
      | invalid_batch_file_without_csv.zip             | Please include the node specific values in the csv file returned from this command and reorder the project with the csv included |
      | invalid_batch_no_csv_12_subvar.zip             | All the substitution variables are not in the CSV                                                                                |
      | invalid_batch_no_csv[SouthWest].zip            | CSV not matched with project name                                                                                                |
      | batch-project_9_subvar.zip                     | All the substitution variables are not in the generated CSV                                                                      |

  Scenario Outline: Order successfully if nodename is provided for the remote node configuration file in Netconf format and it is sync
    Given the project <ProjectName> and node <NodeName> <NodeType> and remote node <RemoteNodeName> <RemoteNodeType> pre-exists user has a project zip file named '<FileName>'
    When the user orders greenfield file with remote node configuration in Netconf format
    Then no error will occur
    Examples:
      | ProjectName             | NodeName           | NodeType    | RemoteNodeName     | RemoteNodeType  | FileName                                            |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00030  | RadioNode   | LTE01dg2ERBS00031  | RadioNode       | valid_greenfield_netconf_file_one_remote_node.zip   |

  Scenario Outline: If the user is ordering a project file with incorrect remoteNodeConfiguration, the system should raise the appropriate error
    Given the project <ProjectName> and node <NodeName> <NodeType> and remote node <RemoteNodeName> <RemoteNodeType> pre-exists user has a project zip file named '<FileName>'
    When the user orders greenfield file with remote node configuration in Netconf format
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | ProjectName             | NodeName           | NodeType    | RemoteNodeName     | RemoteNodeType  | FileName                                              | ValidationMessage        |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00032  | RadioNode   | LTE01dg2ERBS00033  | RadioNode       | invalid_greenfield_netconf_file_one_remote_node.zip   | is not set               |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00030  | RadioNode   | LTE01dg2ERBS00031  | RadioTNode      | valid_greenfield_netconf_file_one_remote_node.zip     | Unsupported file format  |

  Scenario Outline: If the user is ordering a project file with netconf remoteNodeConfiguration, nodename is provided but the node is not sync, the system should raise the appropriate error
    Given the project <ProjectName> and node <NodeName> <NodeType> and remote node <RemoteNodeName> <RemoteNodeType> pre-exists user has a project zip file named '<FileName>'
    When the user orders greenfield file with the Netconf remote node configuration but the node is not synced
    Then an error will occur
      And the validation error message will contain '<ValidationMessage>'
    Examples:
      | ProjectName             | NodeName           | NodeType    | RemoteNodeName     | RemoteNodeType  | FileName                                            | ValidationMessage  |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00030  | RadioNode   | LTE01dg2ERBS00031  | RadioNode       | valid_greenfield_netconf_file_one_remote_node.zip   | is Unsynchronized  |
      | lciadm100_apTafProject2 | LTE01dg2ERBS00030  | RadioNode   | LTE01dg2ERBS00033  | RadioNode       | valid_greenfield_netconf_file_one_remote_node.zip   | does not exist     |