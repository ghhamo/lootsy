package hamo.job.service;

import hamo.job.dto.*;
import hamo.job.entity.User;
import hamo.job.exception.exceptions.token.NoAuthenticatedException;
import hamo.job.exception.exceptions.userException.UserEmailAlreadyExistsException;
import hamo.job.exception.exceptions.userException.UserEmailNotFoundException;
import hamo.job.exception.exceptions.userException.UserIdNotFoundException;
import hamo.job.repository.OrderRepository;
import hamo.job.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CartService cartService;
    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    UserService userService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUserSavesUserEncodesPasswordAndCreatesCart() {
        CreateUserDTO dto = new CreateUserDTO("name", "surname", "mail@test.com", "123", "0999");
        when(userRepository.findByEmail("mail@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123")).thenReturn("ENC");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        User saved = new User();
        saved.setId(42L);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        try (MockedStatic<CreateUserDTO> ms = mockStatic(CreateUserDTO.class)) {
            User toSave = new User();
            ms.when(() -> CreateUserDTO.toUser(dto)).thenReturn(toSave);
            userService.createUser(dto);
            verify(userRepository).findByEmail("mail@test.com");
            verify(passwordEncoder).encode("123");
            verify(userRepository).save(captor.capture());
            User passed = captor.getValue();
            assertEquals("ENC", passed.getPassword());
            assertTrue(passed.isEnabled());
            verify(cartService).create(42L);
        }
    }

    @Test
    void createUserWhenEmailExistsAndThrows() {
        CreateUserDTO dto = new CreateUserDTO("n", "s", "dup@test.com", "p", "ph");
        when(userRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(new User()));
        assertThrows(UserEmailAlreadyExistsException.class, () -> userService.createUser(dto));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(cartService);
    }

    @Test
    void getUsersAndMapsPageToDto() {
        PaginationDTO pg = new PaginationDTO(0, 2);
        Page<User> page = new PageImpl<>(List.of(new User(), new User()), PageRequest.of(0, 2), 2);
        when(userRepository.findAll(PageRequest.of(0, 2))).thenReturn(page);
        Iterable<GetUserDTO> expected = List.of(new GetUserDTO(1L, "a", "b", "e", "p"),
                new GetUserDTO(2L, "c", "d", "f", "q"));
        try (MockedStatic<GetUserDTO> ms = mockStatic(GetUserDTO.class)) {
            ms.when(() -> GetUserDTO.mapUserListToUserDtoList(page)).thenReturn(expected);
            Iterable<GetUserDTO> actual = userService.getUsers(pg);
            assertSame(expected, actual);
        }
    }

    @Test
    void getUserByIdFoundMaps() {
        User u = new User();
        u.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(u));
        GetUserDTO dto = new GetUserDTO(7L, "n", "s", "e", "p");
        try (MockedStatic<GetUserDTO> ms = mockStatic(GetUserDTO.class)) {
            ms.when(() -> GetUserDTO.fromUser(u)).thenReturn(dto);
            GetUserDTO actual = userService.getUserById(7L);
            assertSame(dto, actual);
        }
    }

    @Test
    void getUserByIdNotFoundAndThrows() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(UserIdNotFoundException.class, () -> userService.getUserById(9L));
    }

    @Test
    void getUserByEmailFoundAndMaps() {
        User u = new User();
        u.setEmail("x@y.z");
        when(userRepository.findByEmail("x@y.z")).thenReturn(Optional.of(u));
        GetUserDTO dto = new GetUserDTO(1L, "n", "s", "x@y.z", "p");
        try (MockedStatic<GetUserDTO> ms = mockStatic(GetUserDTO.class)) {
            ms.when(() -> GetUserDTO.fromUser(u)).thenReturn(dto);
            GetUserDTO actual = userService.getUserByEmail("x@y.z");
            assertSame(dto, actual);
        }
    }

    @Test
    void getUserByEmailNotFoundAndThrows() {
        when(userRepository.findByEmail("no@no.com")).thenReturn(Optional.empty());
        assertThrows(UserEmailNotFoundException.class, () -> userService.getUserByEmail("no@no.com"));
    }

    @Test
    void deleteUserWhenExistsDeletes() {
        when(userRepository.existsById(5L)).thenReturn(true);
        userService.deleteUser(5L);
        verify(userRepository).deleteById(5L);
    }

    @Test
    void deleteUserWhenNotExistsNoop() {
        when(userRepository.existsById(6L)).thenReturn(false);
        userService.deleteUser(6L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void saveAllEncodesEachPasswordThenSaves() {
        User u1 = new User();
        u1.setPassword("a");
        User u2 = new User();
        u2.setPassword("b");
        when(passwordEncoder.encode("a")).thenReturn("A");
        when(passwordEncoder.encode("b")).thenReturn("B");
        when(userRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        List<User> out = userService.saveAll(List.of(u1, u2));
        assertEquals("A", u1.getPassword());
        assertEquals("B", u2.getPassword());
        assertEquals(2, out.size());
        verify(userRepository).saveAll(out);
    }

    @Test
    void getCurrentUserIdFromSecurityContextAndFetchesUserId() {
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("me@test.com");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
        User u = new User();
        u.setId(123L);
        u.setEmail("me@test.com");
        when(userRepository.findByEmail("me@test.com")).thenReturn(Optional.of(u));
        Long id = userService.getCurrentUserId();
        assertEquals(123L, id);
    }

    @Test
    void getCurrentUserIdNoAuthAndThrows() {
        SecurityContextHolder.clearContext();
        assertThrows(NoAuthenticatedException.class, () -> userService.getCurrentUserId());
    }

    @Test
    void getCurrentUserAccountBuildsStatsAndMapsToAccountDTO() {
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("acc@test.com");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
        User user = new User();
        user.setId(10L);
        user.setEmail("acc@test.com");
        when(userRepository.findByEmail("acc@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(orderRepository.countByUser(user)).thenReturn(7);
        when(orderRepository.sumTotalAmountByUser(user)).thenReturn(1234.56);
        AccountDTO expected = new AccountDTO(10L, "n", "s", "acc@test.com", "p", true,
                new UserStatsDTO(7, 1234.56));
        try (MockedStatic<AccountDTO> ms = mockStatic(AccountDTO.class)) {
            ms.when(() -> AccountDTO.fromUser(eq(user), argThat(st ->
                            st != null && st.totalOrders() == 7 && st.totalSpent() == 1234.56)))
                    .thenReturn(expected);
            AccountDTO actual = userService.getCurrentUserAccount();
            assertSame(expected, actual);
        }
    }

    @Test
    void updateCurrentUserAccountUpdatesFieldsSavesAndMaps() {
        UserDetails principal = mock(UserDetails.class);
        when(principal.getUsername()).thenReturn("upd@test.com");
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(sc);
        User user = new User();
        user.setId(77L);
        user.setEmail("upd@test.com");
        when(userRepository.findByEmail("upd@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(77L)).thenReturn(Optional.of(user));
        when(orderRepository.countByUser(user)).thenReturn(3);
        when(orderRepository.sumTotalAmountByUser(user)).thenReturn(99.0);
        UpdateAccountDTO upd = new UpdateAccountDTO("NewN", "NewS", "099-99-99");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        AccountDTO expected = new AccountDTO(77L, "NewN", "NewS", "upd@test.com", "099-99-99", true,
                new UserStatsDTO(3, 99.0));
        try (MockedStatic<AccountDTO> ms = mockStatic(AccountDTO.class)) {
            ms.when(() -> AccountDTO.fromUser(any(User.class), any(UserStatsDTO.class)))
                    .thenReturn(expected);
            AccountDTO actual = userService.updateCurrentUserAccount(upd);
            assertEquals("NewN", user.getName());
            assertEquals("NewS", user.getSurname());
            assertEquals("099-99-99", user.getPhoneNumber());
            verify(userRepository).save(user);
            assertSame(expected, actual);
        }
    }
}
