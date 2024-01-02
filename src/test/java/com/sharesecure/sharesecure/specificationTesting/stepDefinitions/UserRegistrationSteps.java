package com.sharesecure.sharesecure.specificationTesting.stepDefinitions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sharesecure.sharesecure.repositories.FolderRepo;
import com.sharesecure.sharesecure.repositories.UserPrivilegeRepo;
import com.sharesecure.sharesecure.repositories.UserRepo;
import com.sharesecure.sharesecure.rest.users.AuthController;
import com.sharesecure.sharesecure.security.jwt.JwtUtils;
import com.sharesecure.sharesecure.security.payload.request.SignupRequest;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


public class UserRegistrationSteps {
    
    private AuthController authController;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private PasswordEncoder encoder;
    @MockBean
    private UserRepo userRepo;
    @MockBean
    private FolderRepo folderRepo;
    @MockBean
    private UserPrivilegeRepo userPrivilegeRepo;
    @Autowired
    private JwtUtils jwtUtils;

    @Given("an anonymous user")
    public void anAnonymousUser(){

    }

    @When("his email is {string} his password is {string} his first name is {string} and his last name is {string}")
    public void theUserUploadsAValidFileWithTheNameToTheServer(String email, String password, String firstName, String lastName) {
        try{
        authController.registerUser(new SignupRequest(email, password, firstName, lastName));
    }
    catch(Exception exception){

    }
    }

    @Then("the server should create the user in the DB and create his root folder")
    public void theServerShouldStoreTheFileSuccessfully() {
    }

    @And("the user should receive confirmation message")
    public void theUserShouldReceiveAConfirmationMessage() {
    }
}
