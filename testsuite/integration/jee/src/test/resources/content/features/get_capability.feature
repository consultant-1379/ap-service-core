Feature: Get capability

Scenario Outline: Get supported nodes capability
    Given the rest assured properties are set
    When the user requests supported node list
    Then the status code is <ResponseCode>
    And response body contains '<ResponseBody>'
    Examples:
        | ResponseCode | ResponseBody                         |
        | 200          | {"supportedNodeTypes":["RadioNode"]} |
