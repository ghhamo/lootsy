package hamo.job.controller;

import hamo.job.dto.LoginUserDto;
import hamo.job.dto.MyUserDetailsDTO;
import hamo.job.exception.handler.ApiExceptionHandler;
import hamo.job.service.AuthenticationService;
import hamo.job.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private TokenController tokenController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tokenController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/token -> 200 OK with JWT token on valid Basic credentials")
    void authenticateValidCredentialsAndReturnsToken() throws Exception {
        String username = "user@example.com";
        String password = "pass123";
        String basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        UserDetails userDetails = new MyUserDetailsDTO(username, password);
        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwtToken123");
        when(jwtService.getExpirationTime()).thenReturn(3600L);
        mockMvc.perform(post("/api/token")
                        .header(HttpHeaders.AUTHORIZATION, basicAuth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwtToken123"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
        verify(authenticationService).authenticate(any(LoginUserDto.class));
        verify(jwtService).generateToken(userDetails);
        verify(jwtService).getExpirationTime();
    }

    @Test
    @DisplayName("POST /api/token -> 400 BAD_REQUEST when Authorization header missing")
    void authenticateNoHeaderAndReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(authenticationService, jwtService);
    }

    @Test
    @DisplayName("POST /api/token -> 400 BAD_REQUEST when header is not Basic")
    void authenticateInvalidHeaderPrefixAndReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/token")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer sometoken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService, jwtService);
    }

    @Test
    @DisplayName("POST /api/token -> 400 BAD_REQUEST when Basic header malformed")
    void authenticateMalformedHeaderAndReturnsBadRequest() throws Exception {
        String malformedBasic = "Basic " + Base64.getEncoder()
                .encodeToString("missingcolon".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(post("/api/token")
                        .header(HttpHeaders.AUTHORIZATION, malformedBasic)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService, jwtService);
    }
}
