Feature: Trainer Workload Summary
  As a workload service
  I want to retrieve trainer workload summary
  So that I can view training statistics

  Scenario: Retrieve summary for trainer with workload
    Given trainer "john.smith" exists with following workload:
      | year | month    | hours |
      | 2025 | JANUARY  | 10.0  |
      | 2025 | FEBRUARY | 15.0  |
    When I request summary for trainer "john.smith"
    Then the response status should be 200
    And the summary should contain username "john.smith"
    And the summary should contain 2 months of data

  Scenario: Retrieve summary for trainer without workload
    Given trainer "jane.doe" exists with no workload
    When I request summary for trainer "jane.doe"
    Then the response status should be 200
    And the summary should contain username "jane.doe"
    And the summary should contain 0 months of data

  Scenario: Retrieve summary for non-existing trainer
    Given trainer "unknown.trainer" does not exist in the system
    When I request summary for trainer "unknown.trainer"
    Then the response status should be 404

  Scenario: Retrieve summary with inactive trainer status
    Given trainer "john.smith" exists with status "INACTIVE" and 5.0 hours for January 2025
    When I request summary for trainer "john.smith"
    Then the response status should be 200
    And the summary should contain status "INACTIVE"
