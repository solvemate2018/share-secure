package com.sharesecure.sharesecure.rest.users;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.sharesecure.sharesecure.security.payload.request.LoginRequest;
import com.sharesecure.sharesecure.security.payload.request.SignupRequest;
import com.sharesecure.sharesecure.security.payload.response.JwtResponse;

import jakarta.validation.Valid;

public interface AuthControllerInterface {
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest);

    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws Exception;

    public ResponseEntity<?> getCSRF();
}
