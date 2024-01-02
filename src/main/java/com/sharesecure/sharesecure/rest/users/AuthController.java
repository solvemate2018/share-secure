package com.sharesecure.sharesecure.rest.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sharesecure.sharesecure.entities.User;
import com.sharesecure.sharesecure.entities.folder.RootFolder;
import com.sharesecure.sharesecure.repositories.FolderRepo;
import com.sharesecure.sharesecure.repositories.UserRepo;
import com.sharesecure.sharesecure.security.jwt.JwtUtils;
import com.sharesecure.sharesecure.security.payload.request.LoginRequest;
import com.sharesecure.sharesecure.security.payload.request.SignupRequest;
import com.sharesecure.sharesecure.security.payload.response.JwtResponse;
import com.sharesecure.sharesecure.security.services.UserDetailsImpl;
import com.sharesecure.sharesecure.services.utils.fileio.FileIOServiceInterface;
import com.sharesecure.sharesecure.services.utils.validation.ValidationServiceInterface;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController implements AuthControllerInterface {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder encoder;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private FolderRepo folderRepo;

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private ValidationServiceInterface validationService;

	@Autowired
	private FileIOServiceInterface fileIOService;

	@PostMapping("/signin")
	public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		log.info("Login request received");

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		log.info("User successfully authenticated");

		String jwt = jwtUtils.generateJwtToken(authentication);

		log.info("JWT generated");

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		return ResponseEntity.ok(new JwtResponse(jwt,
				userDetails.getId(),
				userDetails.getEmail()));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) throws Exception {
		if (userRepo.existsByEmail(signUpRequest.getEmail())) {
			log.error("User already exists.");
			throw new Exception("Email is already taken");
		}

		try {
			// Create User Root Folder
			String sterilizedUserFolder = validationService.sanitizeEmail(signUpRequest.getEmail());
			fileIOService.createDir(sterilizedUserFolder);
			log.info("User root folder created");

			// Create the User Entity
			User user = new User();
			user.setEmail(signUpRequest.getEmail());
			user.setPassword(encoder.encode(signUpRequest.getPassword()));
			user.setFirstName(signUpRequest.getFirstName());
			user.setLastName(signUpRequest.getLastName());

			log.info("User entity created:" + user);

			RootFolder rootFolder = new RootFolder();

			rootFolder.setFolderNormalizedName(sterilizedUserFolder);
			rootFolder.setFolderOwner(user);

			log.info("Root folder entity created:" + rootFolder);

			userRepo.save(user);
			folderRepo.save(rootFolder);

			log.info("User and Root folder saved in the DB.");

			return authenticateUser(new LoginRequest(signUpRequest.getEmail(), signUpRequest.getPassword()));

		} catch (Exception exception) {
			log.error("Something went wrong, further investigation required.");
			return ResponseEntity.badRequest().body(exception.getMessage());
		}
	}


	@Override
	@GetMapping("/csrf")
	public ResponseEntity<?> getCSRF() {
		return ResponseEntity.ok().build();
	}
	
}
