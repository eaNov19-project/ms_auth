package ea.sof.ms_auth.controllers;


import ea.sof.ms_auth.config.JwtTokenUtil;
import ea.sof.ms_auth.config.JwtUserDetailsService;
import ea.sof.ms_auth.entities.User;
import ea.sof.ms_auth.services.UserService;
import ea.sof.shared.models.Auth;
import ea.sof.shared.models.Response;
import ea.sof.shared.models.TokenUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    public ResponseEntity<Response> addAuth(@RequestBody Auth auth){

        try{
            //should check username exists or not in db, then add
            User user = userService.findByEmail(auth.getEmail());
            if(user != null)
                return ResponseEntity.ok(new Response(false, "User existed already!" ));

            User newUser = new User();
            newUser.setEmail(auth.getEmail());
            newUser.setPassword(auth.getPassword());
            userService.saveUser(newUser);

            Response response = new Response(true, "Add authentication successuflly!");
            response.getData().put("auth", new TokenUser(newUser.getId(), newUser.getEmail()));
            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            Response response = new Response(false, "Exception!");
            response.getData().put("exception", e);
            return ResponseEntity.ok(response);
        }

    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody Auth auth){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(auth.getEmail());
            String token = jwtTokenUtil.generateToken(userDetails);
            Response response = new Response(true, "Login successfully!");
            response.getData().put("token", token);
            return ResponseEntity.ok(response);
        }
         catch (Exception e) {
             Response response = new Response(false, "Exception!");
             response.getData().put("exception", e);
             return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Response> validateToken(@RequestHeader(name="Authorization", required = false) String token){

        try{
            String jwttoken = jwtTokenUtil.getTokenFromBearer(token);
            String email = jwtTokenUtil.getUsernameFromToken(jwttoken);
            User user = userService.findByEmail(email);
            TokenUser tokenUser = new TokenUser(user.getId(), user.getEmail());
            Response response = new Response(true, "Token valid");
            response.getData().put("decoded_token", tokenUser);
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            Response response = new Response(false, "Exception!");
            response.getData().put("exception", e);
            return ResponseEntity.ok(response);
        }



    }


}