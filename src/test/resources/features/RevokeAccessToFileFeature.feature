Feature: Revoke File Access

Scenario: Revoke access for a file from valid user
    Given a logged user that owns the file
    When the file ID is <fileId> and the user email is <userEmail> 
    Then the server should delete the userPrivilege associated with this file and user
    And the user should receive a confirmation

    Examples:
    | hello-test@testing.com | 