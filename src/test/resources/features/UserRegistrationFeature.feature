Feature: User Registration

Scenario: Register a user with valid data
    Given an anonymous user
    When his email is <email> his password is <password> his first name is <firstName> and his last name is <lastName>
    Then the server should create the user in the DB and create his root folder
    And the user should receive confirmation message

    Examples:
    | hello-test@testing.com | test123 | Testing | Testing |