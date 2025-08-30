package sa.store.retaildiscount.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import sa.store.retaildiscount.config.JwtUtil;
import sa.store.retaildiscount.dto.AuthenticationRequest;
import sa.store.retaildiscount.dto.AuthenticationResponse;
import sa.store.retaildiscount.entity.Client;
import sa.store.retaildiscount.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthenticationService authenticationService;

    private Client testClient;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        testClient = new Client(
                "1",
                "testuser",
                "$2a$10$encodedPassword",
                LocalDateTime.now()
        );

        authRequest = new AuthenticationRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should successfully authenticate with valid credentials")
    void testAuthenticateSuccess() {
        // Given
        String expectedToken = "jwt.token.here";
        when(clientRepository.findByUsername("testuser")).thenReturn(Optional.of(testClient));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser")).thenReturn(expectedToken);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals("testuser", response.getUsername());
        
        verify(clientRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "$2a$10$encodedPassword");
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testAuthenticateUserNotFound() {
        // Given
        when(clientRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        authRequest.setUsername("nonexistent");

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
        
        verify(clientRepository).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw exception when password is incorrect")
    void testAuthenticateWrongPassword() {
        // Given
        when(clientRepository.findByUsername("testuser")).thenReturn(Optional.of(testClient));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);
        
        authRequest.setPassword("wrongpassword");

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
        
        verify(clientRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", "$2a$10$encodedPassword");
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should handle null username")
    void testAuthenticateNullUsername() {
        // Given
        authRequest.setUsername(null);

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    @DisplayName("Should handle empty username")
    void testAuthenticateEmptyUsername() {
        // Given
        authRequest.setUsername("");
        when(clientRepository.findByUsername("")).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }

    @Test
    @DisplayName("Should handle null password")
    void testAuthenticateNullPassword() {
        // Given
        authRequest.setPassword(null);
        when(clientRepository.findByUsername("testuser")).thenReturn(Optional.of(testClient));
        when(passwordEncoder.matches(null, "$2a$10$encodedPassword")).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Invalid credentials", exception.getReason());
    }
}