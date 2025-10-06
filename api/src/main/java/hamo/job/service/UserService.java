package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.User;
import hamo.job.exception.exceptions.token.NoAuthenticatedException;
import hamo.job.exception.exceptions.userException.UserEmailAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserEmailNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.OrderRepository;
import hamo.job.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;
    private final OrderRepository orderRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       CartService cartService,
                       PasswordEncoder passwordEncoder,
                       OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void createUser(CreateUserDTO createUserDTO) {
        Optional<User> userFromDB = userRepository.findByEmail(createUserDTO.email());
        if (userFromDB.isPresent()) {
            throw new UserEmailAlreadyExistsException(createUserDTO.email());
        }
        User user = CreateUserDTO.toUser(createUserDTO);
        user.setPassword(passwordEncoder.encode(createUserDTO.password()));
        user.setEnabled(true);
        User userFromDb = userRepository.save(user);
        cartService.create(userFromDb.getId());
    }

    @Transactional(readOnly = true)
    public Iterable<GetUserDTO> getUsers(PaginationDTO paginationDTO) {
        PageRequest pageRequest = PageRequest.of(paginationDTO.pageNumber(), paginationDTO.pageSize());
        Page<User> users = userRepository.findAll(pageRequest);
        return GetUserDTO.mapUserListToUserDtoList(users);
    }

    @Transactional(readOnly = true)
    public GetUserDTO getUserById(Long id) {
        Objects.requireNonNull(id);
        User user = userRepository.findById(id).orElseThrow(() -> new UserIdNotFoundException(id));
        return GetUserDTO.fromUser(user);
    }

    @Transactional(readOnly = true)
    public GetUserDTO getUserByEmail(String email) {
        Objects.requireNonNull(email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserEmailNotFoundException(email));
        return GetUserDTO.fromUser(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        Objects.requireNonNull(id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    }

    @Transactional
    public List<User> saveAll(List<User> users) {
        for (User user : users) {
            String password = user.getPassword();
            user.setPassword(passwordEncoder.encode(password));
        }
        return userRepository.saveAll(users);
    }
    
    @Transactional(readOnly = true)
    public Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserEmailNotFoundException(email));
            return user.getId();
        }
        throw new NoAuthenticatedException("No authenticated user found");
    }
    
    @Transactional(readOnly = true)
    public AccountDTO getCurrentUserAccount() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        int totalOrders = orderRepository.countByUser(user);
        double totalSpent = orderRepository.sumTotalAmountByUser(user);
        UserStatsDTO stats = new UserStatsDTO(totalOrders, totalSpent);
        
        return AccountDTO.fromUser(user, stats);
    }
    
    @Transactional
    public AccountDTO updateCurrentUserAccount(UpdateAccountDTO updateAccountDTO) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserIdNotFoundException(userId));
        user.setName(updateAccountDTO.name());
        user.setSurname(updateAccountDTO.surname());
        user.setPhoneNumber(updateAccountDTO.phoneNumber());
        user = userRepository.save(user);
        int totalOrders = orderRepository.countByUser(user);
        double totalSpent = orderRepository.sumTotalAmountByUser(user);
        UserStatsDTO stats = new UserStatsDTO(totalOrders, totalSpent);
        return AccountDTO.fromUser(user, stats);
    }
}