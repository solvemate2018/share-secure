Feature: Share Folder Access

Scenario: Share access for a folder from valid user
    Given a logged user that owns the folder
    When the folder ID is <folder> and the user email is <userEmail> and the user privilage is <userPrivilage>
    Then the server should create a new userPrivilege for the user and the folder 
    And the user should receive a confirmation

    Examples:
    | hello-test@testing.com | 