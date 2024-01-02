Feature: Revoke Folder Access

Scenario: Revoke access for a folder from valid user
    Given a logged user that owns the folder
    When the folder ID is <folderID> and the user email is <userEmail> 
    Then the server should delete the userPrivilege associated with this folder and user
    And the user should receive a confirmation

    Examples:
    | hello-test@testing.com | 