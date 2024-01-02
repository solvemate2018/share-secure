Feature: Upload File

Scenario: Upload a file from logged user
    Given a logged user with valid data
    When the file is <localFileURL> with a path of <parentFolderID>
    Then the server should handle the file and add it to the given folder
    And the user should receive confirmation message

    Examples:
    | hello-test@testing.com | 