package hamo.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hamo.job.dto.*;
import hamo.job.exception.handler.ApiExceptionHandler;
import hamo.job.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setup() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        var field = UserController.class.getDeclaredField("pageMaxSize");
        field.setAccessible(true);
        field.set(userController, 50);
    }

    @Test
    @DisplayName("POST /api/users -> 201 CREATED and calls userService.createUser()")
    void createUserAndReturns201() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("John", "Doe", "john@example.com", "pass", "123");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        verify(userService).createUser(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("GET /api/users -> 200 OK and returns user list")
    void getAllUsersAndReturnsList() throws Exception {
        GetUserDTO user1 = new GetUserDTO(1L, "John", "Doe", "j@e.com", "123");
        GetUserDTO user2 = new GetUserDTO(2L, "Anna", "Smith", "a@e.com", "456");
        when(userService.getUsers(any())).thenReturn(List.of(user1, user2));
        mockMvc.perform(get("/api/users")
                        .param("pageIndex", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].email").value("a@e.com"));
        verify(userService).getUsers(any(PaginationDTO.class));
    }

    @Test
    @DisplayName("GET /api/users with pageSize > max -> 400 BAD_REQUEST")
    void getAllUsersPageTooLarge() throws Exception {
        mockMvc.perform(get("/api/users")
                        .param("pageIndex", "0")
                        .param("pageSize", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/id/{id} -> 200 OK and returns user")
    void getUserByIdAndReturnsUser() throws Exception {
        GetUserDTO dto = new GetUserDTO(1L, "John", "Doe", "john@example.com", "123");
        when(userService.getUserById(1L)).thenReturn(dto);
        mockMvc.perform(get("/api/users/id/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /api/users/email?email=... -> 200 OK and returns user by email")
    void getUserByEmailAndReturnsUser() throws Exception {
        GetUserDTO dto = new GetUserDTO(2L, "Anna", "Smith", "anna@example.com", "555");
        when(userService.getUserByEmail("anna@example.com")).thenReturn(dto);
        mockMvc.perform(get("/api/users/email")
                        .param("email", "anna@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("anna@example.com"));
        verify(userService).getUserByEmail("anna@example.com");
    }

    @Test
    @DisplayName("DELETE /api/users/{id} -> 200 OK (void return)")
    void deleteUserCallsService() throws Exception {
        mockMvc.perform(delete("/api/users/5"))
                .andExpect(status().isOk());
        verify(userService).deleteUser(5L);
    }

    @Test
    @DisplayName("GET /api/users/account -> 200 OK returns current account")
    void getCurrentUserAccountAndReturnsAccount() throws Exception {
        AccountDTO account = new AccountDTO(1L, "JOne", "Doe", "john@example.com", "123", true, new UserStatsDTO(2, 43.4));
        when(userService.getCurrentUserAccount()).thenReturn(account);
        mockMvc.perform(get("/api/users/account"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("john@example.com"));
        verify(userService).getCurrentUserAccount();
    }

    @Test
    @DisplayName("PUT /api/users/account -> 200 OK returns updated account")
    void updateCurrentUserAccountAndReturnsOk() throws Exception {
        UpdateAccountDTO req = new UpdateAccountDTO("Updated", "User", "123");
        AccountDTO resp = new AccountDTO(2L,"Updated", "User", "mail@mail.com", "123", true, new UserStatsDTO(2, 43.4));
        when(userService.updateCurrentUserAccount(any(UpdateAccountDTO.class))).thenReturn(resp);
        mockMvc.perform(put("/api/users/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.surname").value("User"));
        verify(userService).updateCurrentUserAccount(any(UpdateAccountDTO.class));
    }

    @Test
    @DisplayName("PUT /api/users/account -> 400 BAD_REQUEST when service throws exception")
    void updateCurrentUserAccountBadRequest() throws Exception {
        UpdateAccountDTO req = new UpdateAccountDTO("A", "B", "C");
        when(userService.updateCurrentUserAccount(any(UpdateAccountDTO.class)))
                .thenThrow(new RuntimeException("Invalid"));
        mockMvc.perform(put("/api/users/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
        verify(userService).updateCurrentUserAccount(any(UpdateAccountDTO.class));
    }
}
