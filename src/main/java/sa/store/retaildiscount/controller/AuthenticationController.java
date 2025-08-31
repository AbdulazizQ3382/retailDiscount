package sa.store.retaildiscount.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sa.store.retaildiscount.dto.AuthenticationRequest;
import sa.store.retaildiscount.dto.AuthenticationResponse;
import sa.store.retaildiscount.dto.GenericResponse;
import sa.store.retaildiscount.service.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate")
    public GenericResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return GenericResponse.success(authenticationService.authenticate(request));
    }
}