Feature: Trainer Workload Management
  As a workload service
  I want to process trainer workload events
  So that I can track training hours

  Scenario: Add training hours for new trainer
    Given trainer "john.smith" does not exist in the system
    When I send ADD workload request for trainer "john.smith" with 2.5 hours on "2025-01-15"
    Then the response status should be 200
    And trainer "john.smith" should have 2.5 hours for January 2025

  Scenario: Add training hours for existing trainer
    Given trainer "john.smith" exists with 5.0 hours for January 2025
    When I send ADD workload request for trainer "john.smith" with 3.0 hours on "2025-01-20"
    Then the response status should be 200
    And trainer "john.smith" should have 8.0 hours for January 2025

  Scenario: Delete training hours from trainer workload
    Given trainer "john.smith" exists with 10.0 hours for January 2025
    When I send DELETE workload request for trainer "john.smith" with 3.0 hours on "2025-01-20"
    Then the response status should be 200
    And trainer "john.smith" should have 7.0 hours for January 2025

  Scenario: Add training hours for multiple months
    Given trainer "john.smith" exists with 5.0 hours for January 2025
    When I send ADD workload request for trainer "john.smith" with 4.0 hours on "2025-02-15"
    Then the response status should be 200
    And trainer "john.smith" should have 5.0 hours for January 2025
    And trainer "john.smith" should have 4.0 hours for February 2025

  Scenario: Invalid workload request with negative hours
    When I send ADD workload request with negative hours -2.5
    Then the response status should be 400

  Scenario: Invalid workload request with missing username
    When I send ADD workload request without username
    Then the response status should be 400
