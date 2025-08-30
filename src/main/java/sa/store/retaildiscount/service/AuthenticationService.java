package sa.store.retaildiscount.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.config.JwtUtil;
import sa.store.retaildiscount.dto.AuthenticationRequest;
import sa.store.retaildiscount.dto.AuthenticationResponse;
import sa.store.retaildiscount.entity.Client;
import sa.store.retaildiscount.repository.ClientRepository;

@Service
public class AuthenticationService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationService(ClientRepository clientRepository, 
                                PasswordEncoder passwordEncoder, 
                                JwtUtil jwtUtil) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Client client = clientRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(client.getUsername());
        
        return AuthenticationResponse.builder()
                .token(token)
                .username(client.getUsername())
                .build();
    }
}