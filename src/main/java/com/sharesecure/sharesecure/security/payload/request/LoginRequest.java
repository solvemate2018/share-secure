package com.sharesecure.sharesecure.security.payload.request;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Setter
@Getter
public class LoginRequest {
	@NotBlank
	private String email;

	@NotBlank
	private String password;

}
