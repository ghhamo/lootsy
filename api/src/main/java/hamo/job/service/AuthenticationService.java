package hamo.job.service;

import hamo.job.dto.LoginUserDto;
import hamo.job.dto.MyUserDetails;
import hamo.job.entity.User;
import hamo.job.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDetails authenticate(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.email(),
                        input.password()));
        User user = userRepository.findByEmail(input.email())
                .orElseThrow();
        return new MyUserDetails(user.getEmail(), user.getPassword());
    }
}