package hamo.job.controller;

import hamo.job.dto.AccountDTO;
import hamo.job.dto.PaginationDTO;
import hamo.job.dto.UpdateAccountDTO;
import hamo.job.dto.UserDTO;
import hamo.job.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Value("${page.max.size}")
    private Integer pageMaxSize;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<HttpStatus> createUser(@RequestBody UserDTO user) {
        userService.createUser(user);
        return ResponseEntity.ok().body(HttpStatus.CREATED);
    }

    @GetMapping
    public Iterable<UserDTO> getAll(@RequestParam int pageIndex, @RequestParam int pageSize) {
        if (pageMaxSize < pageSize) {
            throw new IllegalStateException();
        }
        return userService.getUsers(new PaginationDTO(pageIndex, pageSize));
    }

    @GetMapping("/id/{id}")
    public UserDTO getOne(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/email")
    public UserDTO getByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email);
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id) {
        return userService.updateUser(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
    @GetMapping("/account")
    public AccountDTO getCurrentUserAccount() {
        return userService.getCurrentUserAccount();
    }
    
    @PutMapping("/account")
    public ResponseEntity<AccountDTO> updateCurrentUserAccount(@RequestBody UpdateAccountDTO updateAccountDTO) {
        try {
            AccountDTO updatedAccount = userService.updateCurrentUserAccount(updateAccountDTO);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
