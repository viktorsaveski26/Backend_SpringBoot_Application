package com.telusko.quizapp.service.OpenAi;

import com.telusko.quizapp.model.QuestionWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAiQuizService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiQuizService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey; // You'll replace this with your xAI key later

    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF = 2000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    public List<QuestionWrapper> generateQuestions(String category, int numQ) throws JSONException {
        String prompt = "Generate " + numQ + " multiple-choice questions about " + category +
                ". Provide four options for each, with only one correct answer.";

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                ResponseEntity<String> response = makeApiCall(prompt);

                log.info("OpenAI API Response Status: {}", response.getStatusCode());
                log.debug("OpenAI API Response Body: {}", response.getBody());

                if (response.getStatusCode() == HttpStatus.OK) {
                    return parseOpenAiResponse(response.getBody());
                } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    long backoff = calculateBackoff(attempt);
                    log.warn("Too many requests. Retrying in {} ms", backoff);
                    TimeUnit.MILLISECONDS.sleep(backoff);
                    continue;
                } else {
                    log.error("OpenAI API request failed with status: {}", response.getStatusCode());
                    return new ArrayList<>();
                }
            } catch (HttpClientErrorException e) {
                log.error("HttpClientErrorException: Status {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    long backoff = calculateBackoff(attempt);
                    log.warn("Too many requests. Retrying in {} ms", backoff);
                    try {
                        TimeUnit.MILLISECONDS.sleep(backoff);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return new ArrayList<>();
                    }
                    continue;
                }
                throw e;
            } catch (InterruptedException e) {
                log.error("InterruptedException during retry", e);
                Thread.currentThread().interrupt();
                return new ArrayList<>();
            }
        }

        log.error("Failed to generate questions after {} attempts", MAX_RETRIES);
        throw new RuntimeException("Failed after " + MAX_RETRIES + " attempts");
    }

    private long calculateBackoff(int attempt) {
        return (long) (INITIAL_BACKOFF * Math.pow(BACKOFF_MULTIPLIER, attempt));
    }

    private ResponseEntity<String> makeApiCall(String prompt) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openAiApiKey); // Your xAI API key goes here
        headers.set("Content-Type", "application/json");

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "grok-2-1212"); // Use "grok-3" if available in xAI console

        JSONArray messages = new JSONArray();
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant that generates quiz questions.");
        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);
        requestBody.put("top_p", 1);
        requestBody.put("frequency_penalty", 0);
        requestBody.put("presence_penalty", 0);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        log.info("Sending OpenAI API request with prompt: {}", prompt);
        return restTemplate.exchange(
                "https://api.x.ai/v1/chat/completions", // Changed to xAI endpoint
                HttpMethod.POST,
                entity,
                String.class
        );
    }

    private List<QuestionWrapper> parseOpenAiResponse(String jsonResponse) {
        List<QuestionWrapper> questions = new ArrayList<>();

        try {
            JSONObject responseJson = new JSONObject(jsonResponse);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                String generatedText = choices.getJSONObject(0).getJSONObject("message").getString("content");
                log.debug("Generated text from OpenAI: {}", generatedText);

                String[] questionBlocks = generatedText.split("\n\n");

                for (int i = 0; i < questionBlocks.length; i++) {
                    String[] parts = questionBlocks[i].split("\n");
                    if (parts.length < 5) {
                        log.warn("Skipping question block due to insufficient parts: {}", questionBlocks[i]);
                        continue;
                    }

                    String questionTitle = parts[0].trim();
                    String option1 = parts[1].trim();
                    String option2 = parts[2].trim();
                    String option3 = parts[3].trim();
                    String option4 = parts[4].trim();

                    QuestionWrapper question = new QuestionWrapper(i + 1, questionTitle, option1, option2, option3, option4);
                    questions.add(question);
                }
            }

        } catch (JSONException e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            return new ArrayList<>();
        }

        return questions;
    }
}