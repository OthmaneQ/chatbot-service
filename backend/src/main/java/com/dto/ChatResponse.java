package com.chatbot.dto;

public class ChatResponse {
    private String content;
    private String role;

    public ChatResponse() {}

    public ChatResponse(String content, String role) {
        this.content = content;
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}