package com.telusko.quizapp.web;
import com.telusko.quizapp.model.DTO.SignUpRequest;
import com.telusko.quizapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signUpUser(@RequestBody SignUpRequest request) {
        userService.registerUser(request.getName(), request.getSurname(), request.getEmail(), request.getPassword(), request.getRole());
        return ResponseEntity.ok("User registered successfully!");
    }
}
