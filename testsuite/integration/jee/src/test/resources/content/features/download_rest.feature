Feature: Download files from the system

  Scenario Outline: Download Generated Artifact using Rest call
    Given the user wants to download the generated artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    And the node has been ordered
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/octet-stream' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    And response header 'Content-Disposition' contains '<NodeName>'
    And response header 'Content-Disposition' contains '<FileType>'
    And response body contains '<FileContent>'
    Examples:
      | URI                                                                                      | StatusCode | ProjectName | NodeName | NodeType  | UserType | FileType    | FileContent                   |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 200        | TestProject | Node01   | RadioNode | ap_admin | SiteInstall | RbsSiteInstallationFile       |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 200        | TestProject | Node02   | ErbS      | ap_admin | SiteInstall | RbsSiteInstallationFile       |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 200        | TestProject | Node03   | MSRBS_V1  | ap_admin | ccf         | combinedConfigurationFileType |

  Scenario Outline: Download Generated Artifact using Rest call with invalid node type
    Given the user wants to download the generated artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    And the node has been ordered
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                                      | StatusCode | ProjectName | NodeName | NodeType | UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 403        | TestProject | Node01   | vPP      | ap_admin |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 403        | TestProject | Node03   | vSD      | ap_admin |

  Scenario Outline: Download Generated Artifact using Rest call with invalid node state
    Given the user wants to download the generated artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    And 'Project=<ProjectName>,Node=<NodeName>' is in state '<NodeState>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                                      | StatusCode | ProjectName | NodeName | NodeType  | UserType | NodeState             |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 403        | TestProject | Node01   | RadioNode | ap_admin | INTEGRATION_COMPLETED |

  Scenario Outline: Download Generated Artifact using Rest call with artifact does not exist
    Given the user wants to download the generated artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                                      | StatusCode | ProjectName | NodeName | NodeType  | UserType |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/<NodeName>/configurations/siteinstall | 403        | TestProject | Node01   | RadioNode | ap_admin |

  Scenario Outline: Download Generated Artifact using Rest call fails when node does not exist
    Given the user wants to download the generated artifacts for the node 'Project=<ProjectName>,Node=<NodeName>' of type <NodeType>
    And the node has already been precreated
    And the node does exist
    And the node has been ordered
    And the rest assured properties are set
    When a user of type '<UserType>' requests a rest call with Accept header 'application/json' with the uri '<URI>'
    Then the status code is <StatusCode>
    And no error will occur
    Examples:
      | URI                                                                                           | StatusCode | ProjectName | NodeName | NodeType  | UserType |
      | /auto-provisioning/v1/projects/InvalidProjectName/nodes/<NodeName>/configurations/siteinstall | 404        | TestProject | Node01   | RadioNode | ap_admin |
      | /auto-provisioning/v1/projects/<ProjectName>/nodes/InvalidNodeName/configurations/siteinstall | 404        | TestProject | Node02   | RadioNode | ap_admin |
