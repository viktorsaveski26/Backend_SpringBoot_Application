package com.telusko.quizapp.security;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class RoleBasedAnswerSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isProfessor = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PROFESSOR"));

        if (isProfessor) {
            // Professors see the right answer
            gen.writeString(value);
        } else {
            gen.writeNull();
            // Students and others don’t — omit field entirely
            // If you want to show null instead, call gen.writeNull()
        }
    }
}
