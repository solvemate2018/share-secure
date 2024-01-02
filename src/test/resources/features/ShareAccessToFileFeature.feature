Feature: Share File Access

Scenario: Share access for a file from valid user
    Given a logged user that owns the file
    When the file ID is <fileId> and the user email is <userEmail> and the user privilage is <userPrivilage>
    Then the server should create a new userPrivilege for the user and the file
    And the user should receive a confirmation

    Examples:
    | hello-test@testing.com | 