package com.telusko.quizapp.AI.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    // Enhanced SQL context with better instructions
    private static final String SQL_CONTEXT = """
    You are an expert SQL generator for a quiz application database.
    Database: PostgreSQL
    
    SCHEMA:
    1. users table:
       - id (integer, primary key)
       - email (varchar)
       - name (varchar) 
       - surname (varchar)
       - password (varchar)
       - role (varchar)
    
    2. quiz table:
       - id (integer, primary key)
       - title (varchar)
       - quiz_difficulty_level (varchar)
    
    3. quiz_result table:
       - id (integer, primary key)
       - correct_answers (integer)
       - total_questions (integer)
       - quiz_id (integer, foreign key to quiz.id)
       - user_id (integer, foreign key to users.id)
    
    STRICT RULES:
    - Generate ONLY SELECT queries
    - Never use INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, or any modification commands
    - NEVER use SELECT * - always specify explicit column names
    - When selecting from users table, always list: id, email, name, surname, role (NEVER include password)
    - When joining tables, use table aliases and prefix columns to avoid duplicates
    - For quiz_result queries with joins, use: qr.id AS result_id, qr.correct_answers, qr.total_questions, qr.quiz_id, qr.user_id
    - For quiz queries with joins, use: q.id AS quiz_id, q.title AS quiz_title, q.quiz_difficulty_level
    - For users queries with joins, use: u.id AS user_id, u.email, u.name, u.surname, u.role
    - Use proper JOINs when querying multiple tables
    - Return ONLY the SQL query - no explanations, no code blocks, no formatting
    - Use proper column names and table names as specified above
    - For exact name matching: Use LOWER(name) = LOWER('searchterm') for case-insensitive exact matching
    - For exact surname matching: Use LOWER(surname) = LOWER('searchterm') for case-insensitive exact matching
    - Never use LIKE or ILIKE with % wildcards unless explicitly asked for partial matching
    
    IMPORTANT MATCHING RULES:
    - When searching for a specific name like "Jovan", use: WHERE LOWER(name) = LOWER('Jovan')
    - When searching for a specific surname like "Smith", use: WHERE LOWER(surname) = LOWER('Smith')
    - Only use LIKE '%term%' when explicitly asked for partial matching or "contains"
    - "students" means users with role = 'STUDENT'
    
    EXAMPLES:
    - "Give me all users named Jovan" -> SELECT id, email, name, surname, role FROM users WHERE LOWER(name) = LOWER('Jovan')
    - "Give me all students" -> SELECT id, email, name, surname, role FROM users WHERE LOWER(role) = 'student'
    - "Show all quiz results for John" -> SELECT qr.id AS result_id, qr.correct_answers, qr.total_questions, qr.quiz_id, qr.user_id, u.id AS user_id, u.email, u.name, u.surname, u.role, q.id AS quiz_id, q.title AS quiz_title FROM quiz_result qr JOIN users u ON qr.user_id = u.id JOIN quiz q ON qr.quiz_id = q.id WHERE LOWER(u.name) = LOWER('John')
    - "Users with surname containing Smith" -> SELECT id, email, name, surname, role FROM users WHERE LOWER(surname) LIKE LOWER('%Smith%')
    - "All users" -> SELECT id, email, name, surname, role FROM users
    - "Find user by email john@example.com" -> SELECT id, email, name, surname, role FROM users WHERE LOWER(email) = LOWER('john@example.com')
    """;

    public String generateSQL(String userQuestion) {
        String finalPrompt = SQL_CONTEXT + "\n\nUser question: " + userQuestion + "\n\nGenerate SQL query:";
        String aiResponse = sendMessageToAI(finalPrompt);
        return cleanSQLResponse(aiResponse);
    }

    private String cleanSQLResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return "SELECT 1 WHERE FALSE"; // Safe fallback that returns no results
        }

        // Remove common AI response formatting
        String cleaned = aiResponse.trim();

        // Remove code block markers
        cleaned = cleaned.replaceAll("```sql", "");
        cleaned = cleaned.replaceAll("```", "");

        // Remove common prefixes
        cleaned = cleaned.replaceAll("(?i)^(SQL:|Query:|Here's the SQL:|The SQL query is:|Answer:)\\s*", "");

        // Extract SQL from the response using regex
        Pattern sqlPattern = Pattern.compile("(SELECT\\s+.*?)(?:\\s*;\\s*)?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = sqlPattern.matcher(cleaned);

        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        // Remove trailing semicolon and whitespace
        cleaned = cleaned.replaceAll(";\\s*$", "").trim();

        // Remove any extra quotes that might wrap the entire query
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.startsWith("'") && cleaned.endsWith("'")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // SECURITY: Additional SQL injection protection
        if (!validateSQLSafety(cleaned)) {
            System.err.println("SQL failed safety validation: " + cleaned);
            return "SELECT 1 WHERE FALSE";
        }

        // Fix wildcard selections and security issues
        cleaned = fixWildcardAndSecurityIssues(cleaned);

        // Log the cleaned SQL for debugging
        System.out.println("Original AI response: " + aiResponse);
        System.out.println("Cleaned SQL: " + cleaned);

        // Validate it starts with SELECT (case insensitive)
        if (!cleaned.toLowerCase().startsWith("select")) {
            System.err.println("AI returned non-SELECT query: " + cleaned);
            return "SELECT 1 WHERE FALSE"; // Safe fallback
        }

        return cleaned;
    }

    /**
     * Additional SQL injection protection - validates that the query doesn't contain
     * dangerous patterns even if it starts with SELECT
     */
    private boolean validateSQLSafety(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String lowerSQL = sql.toLowerCase();

        // Check for multiple statements (semicolon followed by more SQL)
        if (lowerSQL.matches(".*;\\s*(select|insert|update|delete|drop|alter|create|grant|revoke|truncate).*")) {
            System.err.println("SECURITY: Multiple statements detected");
            return false;
        }

        // Check for dangerous SQL commands (even if not at the start)
        String[] dangerousKeywords = {
                "insert ", "update ", "delete ", "drop ", "alter ", "create ",
                "grant ", "revoke ", "truncate ", "exec ", "execute ", "xp_"
        };

        for (String keyword : dangerousKeywords) {
            if (lowerSQL.contains(keyword)) {
                System.err.println("SECURITY: Dangerous keyword detected: " + keyword);
                return false;
            }
        }

        // Check for comment-based injection attempts
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/")) {
            System.err.println("SECURITY: SQL comments detected");
            return false;
        }

        // Check for attempts to access password column
        if (lowerSQL.matches(".*\\bpassword\\b.*")) {
            System.err.println("SECURITY: Password column access attempt detected");
            return false;
        }

        return true;
    }

    private String fixWildcardAndSecurityIssues(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // CRITICAL SECURITY: Replace SELECT * FROM users with explicit columns (no password)
        sql = sql.replaceAll(
                "(?i)SELECT\\s+\\*\\s+FROM\\s+users\\b",
                "SELECT id, email, name, surname, role FROM users"
        );

        // Handle aliased table: SELECT * FROM users u
        sql = sql.replaceAll(
                "(?i)SELECT\\s+\\*\\s+FROM\\s+users\\s+([a-zA-Z]+)\\b",
                "SELECT $1.id, $1.email, $1.name, $1.surname, $1.role FROM users $1"
        );

        // Replace qr.* in quiz_result queries with explicit columns
        sql = sql.replaceAll(
                "(?i)\\bqr\\.\\*\\b",
                "qr.id AS result_id, qr.correct_answers, qr.total_questions, qr.quiz_id, qr.user_id"
        );

        // Replace quiz_result.* with explicit columns
        sql = sql.replaceAll(
                "(?i)\\bquiz_result\\.\\*\\b",
                "quiz_result.id AS result_id, quiz_result.correct_answers, quiz_result.total_questions, quiz_result.quiz_id, quiz_result.user_id"
        );

        // Replace q.* in quiz queries with explicit columns
        sql = sql.replaceAll(
                "(?i)\\bq\\.\\*\\b",
                "q.id AS quiz_id, q.title AS quiz_title, q.quiz_difficulty_level"
        );

        // Replace quiz.* with explicit columns
        sql = sql.replaceAll(
                "(?i)\\bquiz\\.\\*\\b",
                "quiz.id AS quiz_id, quiz.title AS quiz_title, quiz.quiz_difficulty_level"
        );

        // Handle u.* in joins (common alias for users)
        sql = sql.replaceAll(
                "(?i)\\bu\\.\\*\\b",
                "u.id AS user_id, u.email, u.name, u.surname, u.role"
        );

        // Handle users.* in joins
        sql = sql.replaceAll(
                "(?i)\\busers\\.\\*\\b",
                "users.id AS user_id, users.email, users.name, users.surname, users.role"
        );

        // SECURITY: Remove explicit password column references
        sql = sql.replaceAll("(?i),\\s*password\\s*,", ",");
        sql = sql.replaceAll("(?i),\\s*u\\.password\\s*,", ",");
        sql = sql.replaceAll("(?i),\\s*users\\.password\\s*,", ",");
        sql = sql.replaceAll("(?i)(SELECT\\s+)password\\s*,", "$1");
        sql = sql.replaceAll("(?i)(SELECT\\s+)u\\.password\\s*,", "$1");
        sql = sql.replaceAll("(?i)(SELECT\\s+)users\\.password\\s*,", "$1");
        sql = sql.replaceAll("(?i),\\s*password\\s+(FROM)", " $1");
        sql = sql.replaceAll("(?i),\\s*u\\.password\\s+(FROM)", " $1");
        sql = sql.replaceAll("(?i),\\s*users\\.password\\s+(FROM)", " $1");

        // Clean up any resulting double commas
        sql = sql.replaceAll(",\\s*,", ",");
        sql = sql.replaceAll("(?i)SELECT\\s+,", "SELECT ");
        sql = sql.replaceAll(",\\s+(FROM)", " $1");

        System.out.println("Fixed SQL: " + sql);

        return sql.trim();
    }

    public String sendMessageToAI(String message) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);
        headers.set("HTTP-Referer", "http://localhost:3000");
        headers.set("X-Title", "Quiz App");

        Map<String, Object> body = new HashMap<>();
        body.put("model", "meta-llama/llama-3.3-70b-instruct:free");
        body.put("messages", List.of(
                Map.of("role", "user", "content", message)
        ));
        body.put("temperature", 0.1); // Lower temperature for more consistent SQL generation
        body.put("max_tokens", 500);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    API_URL, HttpMethod.POST, request, Map.class);

            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");

                if (!choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Object messageObj = choice.get("message");

                    if (messageObj instanceof Map) {
                        Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                        Object content = messageMap.get("content");
                        if (content instanceof String) {
                            return ((String) content).trim();
                        }
                    }
                }
            }
            return "Error: Invalid AI response format";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}