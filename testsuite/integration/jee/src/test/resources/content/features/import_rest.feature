Feature: Import and order integration for project archive through REST

  Scenario Outline: Service should allow authorized users to import a valid project archive through REST
    Given the user has a zip file named 'ORDER_01.zip' for a project of type <NodeType>
    And project 1 is named '<ProjectName>'
    And project 1 has its nodes configured like this:
      | NodeIdentifier | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                | HardwareSerialNumber |
      | 18.Q4-R57A02   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal | F123456789           |
    And security is enabled
    And the user is <UserType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a post rest call with file 1 with uri '<URI>'
    Then the status code is <StatusCode>
    And the project <ProjectName> exists
    Examples: Project and REST details
      | ProjectName       | UserType | URI                                   | StatusCode | NodeType  |
      | ArquillianProject | ap_admin | /auto-provisioning/v1/projects |        201 | RadioNode |

  Scenario Outline: If the user is ordering a project with validation errors, the system should raise the appropriate error
    Given the user has a zip file named 'ORDER_01.zip' for a project of type <NodeType>
    And project 1 is named '<ProjectName>'
    And project 1 has its nodes configured like this:
      | NodeIdentifier       | NodeArtifacts | AutoIntegrationOptions     | SecurityConfig     | NodeConfig                | HardwareSerialNumber |
      | '<NodeIdentifier>'   | ecimArtifacts | ecimAutoIntegrationOptions | ecimSecurityConfig | ecimConfigurationsMinimal | F123456789           |
    And security is enabled
    And the user is <UserType>
    And the rest assured properties are set
    When a user of type '<UserType>' requests a post rest call with file 1 with uri '<URI>'
    Then the status code is <StatusCode>
    And response body contains '<ValidationMessage>'
    Examples: Project and REST details
      | ProjectName          | UserType | URI                            | StatusCode | NodeType    | NodeIdentifier | ValidationMessage                 |
      | Project with Space   | ap_admin | /auto-provisioning/v1/projects |        417 | RadioNode   | 18.Q4-R57A02   | Error(s) found validating project |
      | ArquillianProject    | ap_admin | /auto-provisioning/v1/projects |        417 | RadioNode   | Invalid£$%^&   | Error(s) found validating project |
      | ArquillianProject    | ap_admin | /auto-provisioning/v1/projects |        417 | Radiooode   | 18.Q4-R57A02   | Error(s) found validating project |

  Scenario Outline: If the user is ordering a project with validation errors, the system should raise the appropriate error
    Given security is enabled
    And the user is <UserType>
    And the user has an invalid file named '<FileName>'
    And the rest assured properties are set
    When a user of type '<UserType>' requests a post rest call with zip with the uri '<URI>'
    Then the status code is <StatusCode>
    And response body contains '<ValidationMessage>'
    Examples:
      | FileName                                        | ValidationMessage                 | URI                            | UserType | StatusCode |
      | Standard_Project_Multiple_Validation_Errors.zip | Error(s) found validating project | /auto-provisioning/v1/projects | ap_admin |        417 |
