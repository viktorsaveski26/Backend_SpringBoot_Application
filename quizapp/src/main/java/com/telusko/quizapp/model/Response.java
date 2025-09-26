package com.telusko.quizapp.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Response {
    private Integer id;
    private String response;


    public Response(Integer id, String response) {
        this.id = id;
        this.response = response;
    }
}
