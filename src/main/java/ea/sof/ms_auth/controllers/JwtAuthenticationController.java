package ea.sof.ms_auth.controllers;


import ea.sof.ms_auth.config.JwtTokenUtil;
import ea.sof.ms_auth.config.JwtUserDetailsService;
import ea.sof.ms_auth.entities.User;
import ea.sof.ms_auth.model.JwtRequest;
import ea.sof.ms_auth.model.JwtResponse;
import ea.sof.ms_auth.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private UserService userService;

    @PostMapping("/add-auth")
    public ResponseEntity<?> addAuth(@RequestBody JwtRequest auth) throws Exception {

        //should check username exists or not in db, then add
//        final UserDetails userDetails = userDetailsService.loadUserByUsername(newAuthenticate.getUsername());
//        if(userDetails != null)
//            return ResponseEntity.ok("Username existed in the system!!!");

        User newUser = new User();
        newUser.setUsername(auth.getUsername());
        newUser.setPassword(auth.getPassword());
        userService.saveUser(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authenticationRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            String token = jwtTokenUtil.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader(name="Authorization", required = false) String token){
        return ResponseEntity.ok().build();
    }


}