Feature: Upload an artifact to the system via REST

  Scenario Outline: Uploading a valid artifact via REST should change its remote content
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | <NodeName> |
    And the rest assured properties are set
    And security is enabled
    When a user of type '<UserType>' uploads a file '<FilePath>' to the uri '<URI>' for node '<NodeName>'
    Then the status code is <StatusCode>
    Examples: ERBS files
      | NodeType | URI                                                                    | ProjectName | NodeName | FilePath                            | StatusCode | UserType |
      | erbs     | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/erbs/radio.xml     | 204        | ap_admin |
      | erbs     | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/erbs/SiteBasic.xml | 204        | ap_admin |
    Examples: RadioNode files
      | NodeType  | URI                                                                    | ProjectName | NodeName | FilePath                                     | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/radio.xml              | 204        | ap_admin |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/unlockCell.xml         | 204        | ap_admin |

  Scenario Outline: Uploading a artifact via REST requires format and content validation
    Given the system has the following project name <ProjectName>, node type <NodeType>, node name <NodeName> and file format <FileFormat>
    And the rest assured properties are set
    And security is enabled
    When a user of type '<UserType>' uploads a file '<FilePath>' to the uri '<URI>' for node '<NodeName>'
    Then the status code is <StatusCode>
    Examples: Bulk3GPP files
      | NodeType  | FileFormat | URI                                                                    | ProjectName | NodeName | FilePath                                     | StatusCode | UserType |
      | RadioNode | BULK_3GPP  | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/radio.xml              | 204        | ap_admin |
      | RadioNode | BULK_3GPP  | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/netconf/unlockCell.xml | 417        | ap_admin |
    Examples: NETCONF files
      | NodeType  | FileFormat | URI                                                                    | ProjectName | NodeName | FilePath                                     | StatusCode | UserType |
      | RadioNode | NETCONF    | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/radio.xml              | 417        | ap_admin |
      | RadioNode | NETCONF    | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/netconf/unlockCell.xml | 204        | ap_admin |

  Scenario Outline: Uploading an artifact to missing node or project should return an error
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | Node01     |
    And the rest assured properties are set
    And security is enabled
    When a user of type '<UserType>' uploads a file '<FilePath>' to the uri '<URI>' for node '<NodeName>'
    Then the status code is <StatusCode>
    Examples: Error scenarios
      | NodeType  | URI                                                                         | ProjectName | NodeName    | FilePath                             | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/InvalidProject/nodes/Node01/configurations   | TestProject | Node01      | upload-artifacts/ecim/radio.xml      | 404        | ap_admin |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/InvalidNode/configurations | TestProject | InvalidNode | upload-artifacts/ecim/unlockCell.xml | 404        | ap_admin |

  Scenario Outline: Uploading an artifact to node in invalid state should return an error
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | Node01     |
    And the rest assured properties are set
    And 'Project=<ProjectName>,Node=<NodeName>' is in state '<State>'
    And security is enabled
    When a user of type '<UserType>' uploads a file '<FilePath>' to the uri '<URI>' for node '<NodeName>'
    Then the status code is <StatusCode>
    Examples: Error scenarios
      | NodeType  | URI                                                                    | ProjectName | NodeName | FilePath                             | State                 | StatusCode | UserType |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations | TestProject | Node01   | upload-artifacts/ecim/unlockCell.xml | INTEGRATION_COMPLETED | 406        | ap_admin |

  Scenario Outline: Uploading an artifact to node should return an error in invalid scenarios
    Given the system has the following project(s):
      | NodeType   | NodeCount | ProjectName   | NodeNames  |
      | <NodeType> | 1         | <ProjectName> | Node01     |
    And the rest assured properties are set
    And security is enabled
    When a user of type '<UserType>' uploads a file '<FilePath>' to the uri '<URI>' for node '<NodeName>'
    Then the status code is <StatusCode>
    And response body contains '<ErrorMessage>'
    Examples: Error scenarios - node or project not found
      | NodeType  | URI                                                                         | ProjectName | NodeName    | FilePath                                | StatusCode | UserType | ErrorMessage |
      | RadioNode | /auto-provisioning/v1/projects/InvalidProject/nodes/Node01/configurations   | TestProject | Node01      | upload-artifacts/ecim/radio.xml         | 404        | ap_admin | Project does not exist |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/InvalidNode/configurations | TestProject | InvalidNode | upload-artifacts/ecim/unlockCell.xml    | 404        | ap_admin | Node does not exist |
    Examples: Error scenarios - missing artifact
      | NodeType  | URI                                                                         | ProjectName | NodeName    | FilePath                                | StatusCode | UserType | ErrorMessage |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations      | TestProject | Node01      | upload-artifacts/ecim/radio2.xml        | 403        | ap_admin | Node does not contain any matching configuration file radio2.xml |
    Examples: Error scenarios - unsupported artifact type
      | NodeType  | URI                                                                         | ProjectName | NodeName    | FilePath                                | StatusCode | UserType | ErrorMessage |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations      | TestProject | Node01      | upload-artifacts/ecim/SiteInstall.xml   | 409        | ap_admin | The file type for file siteInstallation is not supported by the upload action |
    Examples: Error scenarios - file not well formed xml
      | NodeType  | URI                                                                         | ProjectName | NodeName    | FilePath                                | StatusCode | UserType | ErrorMessage |
      | RadioNode | /auto-provisioning/v1/projects/TestProject/nodes/Node01/configurations      | TestProject | Node01      | upload-artifacts/ecim/invalid/radio.xml | 417        | ap_admin | Error validating file for node Node01 radio.xml |
