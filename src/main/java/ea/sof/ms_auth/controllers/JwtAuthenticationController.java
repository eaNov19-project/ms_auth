package ea.sof.ms_auth.controllers;

import ea.sof.ms_auth.config.JwtTokenUtil;
import ea.sof.ms_auth.config.JwtUserDetailsService;
import ea.sof.ms_auth.entities.User;
import ea.sof.ms_auth.services.UserService;
import ea.sof.shared.models.Auth;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@CrossOrigin
@RequestMapping("/auth")
public class JwtAuthenticationController {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	@Autowired
	private JwtUserDetailsService userDetailsService;
	@Autowired
	private UserService userService;

	@Value("${app.version}")
    private String appVersion;

	@GetMapping("/health")
	public ResponseEntity<?> index() {
		String host = "Unknown host";
		try {
			host = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return new ResponseEntity<>("Auth service (" + appVersion + "). Host: " + host, HttpStatus.OK);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationController.class);

	@PostMapping("/add-auth")
	public ResponseEntity<Response> addAuth(@RequestBody Auth auth) {

		try {
			LOGGER.info("trying to add to authentication");
			//should check username exists or not in db, then add
			User user = userService.findByEmail(auth.getEmail());

			//get the userId coming from user service.
			Long fromUserviceId = auth.getUserId();
			if (user != null) {
//				user.setPassword(auth.getPassword());
//				userService.saveUser(user);

				LOGGER.warn("User existed already with email: " + auth.getEmail());
				return ResponseEntity.ok(new Response(true, "User existed already!"));
			}

			User newUser = new User();
			newUser.setEmail(auth.getEmail());
			newUser.setPassword(auth.getPassword());
			newUser.setUserId(auth.getUserId());
			userService.saveUser(newUser);
			Response response = new Response(true, "Add authentication successfully!");

			//Here the token is generated using the userId from user service;
			response.getData().put("auth", new TokenUser(newUser.getUserId(), newUser.getEmail()));
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Response response = new Response(false, "Exception!");
			LOGGER.error("addAuth method failed: " + e.getMessage());
			response.addObject("exception", e);
			return ResponseEntity.ok(response);
		}

	}

	@PostMapping("/login")
	public ResponseEntity<Response> login(@RequestBody Auth auth) {
		try {
			LOGGER.info("some user is signing-in");
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));
			UserDetails userDetails = userDetailsService.loadUserByUsername(auth.getEmail());
			if (userDetails == null){
			    return ResponseEntity.ok(new Response(false, "Wrong user email or password"));
            }

			String token = jwtTokenUtil.generateToken(userDetails);

			Response response = new Response(true, "Login successfully!");
			response.getData().put("token", token);

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			Response response = new Response(false, "Wrong user email or password");
			LOGGER.info("user sign-in failed. Exception: " + e.getMessage());
//			response.getData().put("exception", e);
			return ResponseEntity.ok(response);
		}
	}

	@GetMapping("/validate-token")
	public ResponseEntity<Response> validateToken(@RequestHeader(name = "Authorization", required = false) String token) {

		try {
			LOGGER.info("trying to validate token");
			String jwttoken = jwtTokenUtil.getTokenFromBearer(token);
			String email = jwtTokenUtil.getUsernameFromToken(jwttoken);
			User user = userService.findByEmail(email);
			TokenUser tokenUser = new TokenUser(user.getUserId(), user.getEmail());
			Response response = new Response(true, "Token valid");
			response.getData().put("decoded_token", tokenUser);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			LOGGER.error("token validation failed");
			Response response = new Response(false, "Exception!");
			response.getData().put("exception", e);
			return ResponseEntity.ok(response);
		}


	}


}