package fr.yoannbarrallon.runtrack.auth;

import fr.yoannbarrallon.runtrack.auth.dto.AuthResponse;
import fr.yoannbarrallon.runtrack.auth.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void shouldRegisterANewUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest("runner@example.com", "secret", "Runner");
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .firstName(request.firstName())
                .build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(savedUser)).thenReturn("jwt-token");

        Optional<AuthResponse> response = authService.register(request);

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().token()).isEqualTo("jwt-token");
        verify(userRepository).save(argThat(user ->
                {
                    if (!user.getEmail().equals(request.email())) return false;
                    assert user.getPassword() != null;
                    return user.getPassword().equals("encoded-secret");
                }
        ));
    }

    @Test
    void shouldNotRegisterAnExistingEmail() {
        RegisterRequest request = new RegisterRequest("runner@example.com", "secret", "Runner");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThat(authService.register(request)).isEmpty();
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService);
    }
}
