Feature: Delete Folder

Scenario: Delete folder for a logged user
    Given a logged user with valid data wants to delete empty
    When the folder ID is <folderId>
    Then the server should delete the folder
    And the user should receive the parent folder

    Examples:
    | hello-test@testing.com | 