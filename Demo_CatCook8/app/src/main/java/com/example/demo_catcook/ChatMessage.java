package com.example.demo_catcook;

public class ChatMessage {
    private String message;
    private boolean isUser; // true si es mensaje del usuario, false si es del chatbot

    // Constructor para crear un mensaje
    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }
    // Obtener el mensaje y si es del usuario
    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }
}

