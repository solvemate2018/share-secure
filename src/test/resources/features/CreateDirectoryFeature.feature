Feature: Create Folder

Scenario: Create folder for a logged user
    Given a logged user with valid data
    When the parent folder ID is <parentFolderID> and the folder name is <folderName>
    Then the server should create a folder within the parent folder
    And the user should receive the parent folder which now should contain the new folder

    Examples:
    | hello-test@testing.com | 