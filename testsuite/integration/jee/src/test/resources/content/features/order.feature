Feature: Order a project

    Scenario Outline: Service should successfully order a project with multiple nodes
    Given the user has a zip file named 'ORDER_02.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | HardwareSerialNumber | NodeIdentifier   | WorkOrderId   | UserLabel   | NodeArtifacts   | AutoIntegrationOptions   | NodeUserCredentials   | SecurityConfig   | NodeConfig   | Notification   |
       | N001     | ABC1234567           | <NodeIdentifier> | <WorkOrderId> | <UserLabel> | <NodeArtifacts> | <AutoIntegrationOptions> | <NodeUserCredentials> | <SecurityConfig> | <NodeConfig> | <Notification> |
       | N002     | ABC1234568           | <NodeIdentifier> | <WorkOrderId> | <UserLabel> | <NodeArtifacts> | <AutoIntegrationOptions> | <NodeUserCredentials> | <SecurityConfig> | <NodeConfig> | <Notification> |
     When the user orders project archive 1
     Then no error will occur
      And the system will create a managed object for project 1
      And the system will create a managed object for each node from project 1
      And the system will create a NodeUserCredentials mo for each node from project 1
      And the system will create a Notification mo for each node from project 1
      And the system will create folders for all nodes from project 1
      And these created folders will contain minimal default artifacts
    Examples: erbs nodes
      | NodeType | ProjectName | NodeIdentifier | UserLabel    | NodeArtifacts | AutoIntegrationOptions     | NodeUserCredentials | SecurityConfig     | NodeConfig                | Notification     |
      | erbs     | ERBSProject | 6607-651-025   | Athlone-East | erbsArtifacts | erbsAutoIntegrationOptions | nodeUserCredentials | erbsSecurityConfig | erbsConfigurationsMinimal | erbsNotification |
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeIdentifier | WorkOrderId | UserLabel    | NodeArtifacts | AutoIntegrationOptions     | NodeUserCredentials | SecurityConfig     | NodeConfig                | Notification     |
      | RadioNode | ECIMProject | 18.Q4-R57A02   | WABCT456    | Athlone-West | ecimArtifacts | ecimAutoIntegrationOptions | nodeUserCredentials | ecimSecurityConfig | ecimConfigurationsMinimal | ecimNotification |


    Scenario Outline: Service should allow authorized users to order a file if security is enabled
    Given the user has a zip file named 'ORDER_03.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | NodeUserCredentials   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <NodeUserCredentials> | <SecurityConfig> | <NodeConfig> | F123456789           |
      And the user is <UserType>
      And security is enabled
     When the user orders project archive 1
     Then no error will occur
      And the system will create a managed object for project 1
      And the system will create a managed object for each node from project 1
      And the system will create folders for all nodes from project 1
      And these created folders will contain all default artifacts
    Examples: User is administrator
      | NodeType  | UserType     | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | NodeUserCredentials | SecurityConfig     | NodeConfig          |
      | erbs      | ap_admin     | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | nodeUserCredentials | erbsSecurityConfig | erbsConfigurations  |
      | RadioNode | ap_admin     | ECIMProject | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | nodeUserCredentials | ecimSecurityConfig | ecimConfigurations  |
    Examples: User is operator
      | NodeType  | UserType     | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | NodeUserCredentials | SecurityConfig     | NodeConfig          |
      | erbs      | ap_operator  | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | nodeUserCredentials | erbsSecurityConfig | erbsConfigurations  |
      | RadioNode | ap_operator  | ECIMProject | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | nodeUserCredentials | ecimSecurityConfig | ecimConfigurations  |


    Scenario Outline: Service should not order project having two nodes with duplicate ip
    Given the user has a zip file named 'ORDER_04.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | IpAddress   | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456788           |
       | N002     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'N002 - Duplicate IP address <IpAddress>'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | IpAddress   | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 10.10.10.1  | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should not order project with a node that already exists in the NRM database
    Given the user has a zip file named 'ORDER_05.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      But there already exists a network element of type <NodeType> with name 'N001' in the NRM database
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'N001 - ENM node already exists NetworkElement=N001'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 18.Q4-R57A02      | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |


    Scenario Outline: Service should not order project with an IP address that already exists in the NRM database
    Given the user has a zip file named 'ORDER_06.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | IpAddress   | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      But there already exists a network element of type <NodeType> with ip <IpAddress> in the NRM database
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'N001 - IP address <IpAddress> is already in use (found in NetworkElement=EXISTING_PREVIOUS0001,<ConnectivityInformation>=1)'
      And the system will not create a managed object for project 1
    Examples: RadioNode nodes
      | NodeType  | ProjectName | IpAddress   | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                | ConnectivityInformation    |
      | RadioNode | ECIMProject | 10.10.10.1  | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal | ComConnectivityInformation |


    Scenario Outline: Service should not order two projects having a node with the same name
    Given the user has a zip file named 'ORDER_08_A.zip' for a project of type <NodeType>
      And the user has another zip file named 'ORDER_08_B.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>-A'
      And project 1 has its nodes configured like this:
       | NodeName | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      And project 2 is named '<ProjectName>-B'
      And project 2 has its nodes configured like this:
       | NodeName | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F923456789           |
     When the user orders project archive 1
     Then no error will occur
      But after the user orders project archive 2
     Then an error will occur
      And the validation error message will be 'N001 - Node=N001 already exists in project <ProjectName>-A'
      And the system will create a managed object for project 1
      But the system will not create a managed object for project 2
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |


    Scenario Outline: Service should not order project with invalid extension
    Given the user has a zip file named 'ORDER_09.zap' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'Invalid file extension ORDER_09.zap. Only .zip supported'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should not order project without referenced artifact
    Given the user has a zip file named 'ORDER_11.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      But project 1 does not contain a file named 'N001/siteEquipment.xml'
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'N001 - Artifact siteEquipment.xml referenced in nodeInfo.xml is not found'
      And the system will not create a managed object for project 1
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 18.Q4-R57A02      | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |


    Scenario Outline: Service should block unauthorized users from ordering a file if security is enabled
    Given the user has a zip file named 'ORDER_12.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      And the user is <UserType>
      And security is enabled
     When the user orders project archive 1
     Then an error will occur
      And the error will have type 'SecurityViolationException'
      And the system will not create a managed object for project 1
    Examples: User is security administrator
      | NodeType  | UserType     | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ap_sec_admin | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |


    Scenario Outline: Service should not order project with problematic nodeInfo.xml
    Given the user has a zip file named 'ORDER_13.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | IpAddress   | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
       | N001     | <IpAddress> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
     When the user orders project archive 1
     Then an error will occur
      And the error message will contain '<ErrorMessage>'
      And the system will not create a managed object for project 1
    Examples: nodeInfo.xml is incorrect
      | NodeType  | ProjectName | IpAddress                             | NodeIdentifier     | ErrorMessage                                          | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 0.0.0.0</ipAddress><a></a><ipAddress> | 18.Q4-R57A02       | N001 - nodeInfo.xml failed to validate against schema | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |
    Examples: nodeInfo.xml is damaged
      | NodeType  | ProjectName | IpAddress | NodeIdentifier     | ErrorMessage                                                | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 0.0.0.0   | </nodeIdentifier>  | N001 - Artifact file nodeInfo.xml is not a well-formed file | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |


    Scenario Outline: Service should not order project with duplicate projectInfo.xml
    Given the user has a zip file named 'ORDER_14.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      But project 1 has an extra file called 'projectInfo.xml' in folder 'N001/' with content '<xml/>'
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'Multiple projectInfo.xml files found in project file'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |


    Scenario Outline: Service should not order a project with multiple nodes and duplicate hardware serial numbers
    Given the user has a zip file named 'ORDER_20.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | HardwareSerialNumber   | NodeIdentifier   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
       | N001     | <HardwareSerialNumber> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
       | N002     | <HardwareSerialNumber> | <NodeIdentifier> | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will be 'N002 - Duplicate hardware serial number <HardwareSerialNumber>'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | ProjectName | HardwareSerialNumber | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | F123456789           | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should successfully order a project with hardware serial number provided
    Given the user has a zip file named 'ORDER_22.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeIdentifier   | HardwareSerialNumber   | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
       | <NodeIdentifier> | ABC1234567             | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
     When the user orders project archive 1
     Then no error will occur
      And the system will create a managed object for project 1
      And the system will create a managed object for each node from project 1
      And the system will create a folder for node 1 from project 1
      And this folder will contain minimal default artifacts
    Examples: erbs nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | ERBSProject | 6607-651-025   | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |
    Examples: Ecim nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |

    Scenario Outline: Service should not order a project with a hardware serial number already used to bind another node
    Given the user has a zip file named 'ORDER_23_A.zip' for a project of type <NodeType>
      And the user has another zip file named 'ORDER_23_B.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>-A'
      And project 1 has its nodes configured like this:
       | NodeName | NodeIdentifier   | HardwareSerialNumber| NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
       | Node001  | <NodeIdentifier> | 1234545667          | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
      And project 2 is named '<ProjectName>-B'
      And project 2 has its nodes configured like this:
       | NodeName | NodeIdentifier   | HardwareSerialNumber| NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   |
       | Node002  | <NodeIdentifier> | 1234545667          | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> |
     When the user orders project archive 1
     Then no error will occur
      But after the user orders project archive 2
     Then an error will occur
       And the validation error message will be 'Project 'Node002' import has failed because the hardware serial number '1234545667' is already in use by 'ECIMProject-A' with 'Node001' in AP.'
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | RadioNode | ECIMProject | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal |

    Scenario Outline: Service should not order project with damaged projectInfo.xml
    Given the user has a zip file named 'ORDER_21.zip' for a project of type <NodeType>
      And project 1 is named '</name><invalid><name>'
      And project 1 has its nodes configured like this:
        | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
        | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
       When the user orders project archive 1
       Then an error will occur
         And the validation error message will contain 'Artifact projectInfo.xml failed to validate against schema'
         And the system will not create a managed object for project 1
      Examples: erbs nodes
        | NodeType  | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
        | erbs      | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should not order project with faulty projectinfo
    Given the user has a zip file named 'ORDER_22.zip' for a project of type <NodeType>
      And project 1 is named '</name><invalid></invalid><name>'
      And project 1 has its nodes configured like this:
      | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
      | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will contain 'Artifact projectInfo.xml failed to validate against schema'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should not order project with incomplete projectInfo.xml
    Given the user has a zip file named 'ORDER_23.zip' for a project of type <NodeType>
      And project 1 does not have a name
      And project 1 has its nodes configured like this:
      | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
      | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will contain 'Artifact projectInfo.xml failed to validate against schema'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
     | NodeType  | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
     | erbs      | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |

    Scenario Outline: Service should not order broken file
    Given the user has a zip file named 'ORDER_24.zip' for a project of type <NodeType>
      And the zip file for project 1 is broken
      And project 1 has its nodes configured like this:
      | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
      | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will contain 'Artifact projectInfo.xml failed to validate against schema'
      And the system will not create a managed object for project 1
    Examples: erbs nodes
      | NodeType  | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                |
      | erbs      | erbsArtifacts | erbsAutoIntegrationOptions | erbsSecurityConfig | erbsConfigurationsMinimal |


    Scenario Outline: Service should order physical node project having nodes without identifier in nodeInfo.xml if service can determine the node
    identifier based on the Upgrade Package selected
    Given the user has a zip file named 'ORDER_25.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      And node 1 from project 1 does not have an identifier
      But node identifier can be determined based on <UpgradePackage>
     When the user orders project archive 1
     Then no error will occur
      And the system will create a managed object for project 1
      And the system will create a managed object for each node from project 1
      And the system will create a folder for node 1 from project 1
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                | UpgradePackage       |
      | RadioNode | ECIMProject | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal | CXP9024418_6_R40A109 |


    Scenario Outline: Service should not order physical node project having nodes without identifier in nodeInfo.xml if service cannot determine the node
    identifier based on the Upgrade Package selected
    Given the user has a zip file named 'ORDER_26.zip' for a project of type <NodeType>
      And project 1 is named '<ProjectName>'
      And project 1 has its nodes configured like this:
       | NodeName | NodeArtifacts   | AutoIntegrationOptions   | SecurityConfig   | NodeConfig   | HardwareSerialNumber |
       | N001     | <NodeArtifacts> | <AutoIntegrationOptions> | <SecurityConfig> | <NodeConfig> | F123456789           |
      But node 1 from project 1 does not have an identifier
      And node identifier cannot be determined based on <UpgradePackage>
     When the user orders project archive 1
     Then an error will occur
      And the validation error message will contain 'N001 - Node Identifier could not be determined. Use '
      And the validation error message will contain 'for supported Node/Model Identifiers, and update the nodeInfo.xml to align'
      And the system will not create a managed object for project 1
    Examples: RadioNode nodes
      | NodeType  | ProjectName | NodeArtifacts | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                | UpgradePackage       |
      | RadioNode | ECIMProject | ecimArtifacts | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal | CXP9024418_6_R29A151 |
