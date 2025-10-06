package hamo.job.controller;

import hamo.job.dto.LoginResponseDTO;
import hamo.job.dto.LoginUserDto;
import hamo.job.service.AuthenticationService;
import hamo.job.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class TokenController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    @Autowired
    public TokenController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    public ResponseEntity<LoginResponseDTO> authenticate(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Basic ")) {
            return ResponseEntity.badRequest().build();
        }
        String base64Credentials = header.substring("Basic ".length());
        String credentials;
        try {
            credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
        int sep = credentials.indexOf(':');
        if (sep < 0) {
            return ResponseEntity.badRequest().build();
        }
        String username = credentials.substring(0, sep);
        String password = credentials.substring(sep + 1);
        UserDetails authenticatedUser = authenticationService.authenticate(new LoginUserDto(username, password));
        String jwtToken = jwtService.generateToken(authenticatedUser);
        return ResponseEntity.ok(new LoginResponseDTO(jwtToken, jwtService.getExpirationTime()));
    }


}