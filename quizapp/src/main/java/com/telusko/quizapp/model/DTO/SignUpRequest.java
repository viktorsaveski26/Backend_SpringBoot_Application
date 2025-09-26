package com.telusko.quizapp.model.DTO;


import com.telusko.quizapp.model.Enum.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private Role role; // Must be either PROFESSOR or STUDENT
}
