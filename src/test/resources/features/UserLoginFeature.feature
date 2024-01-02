Feature: User Login

Scenario: Login a user with valid data
    Given an anonymous user that is previously registered
    When his email is <email> his password is <password>
    Then the server should authenticate the user and generate a JWT
    And the user should receive a valid JWT

    Examples:
    | hello-test@testing.com | test123 |