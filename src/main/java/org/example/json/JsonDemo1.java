package org.example.json;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class JsonDemo1 {
    public static void main(String[] args) {
        String responseBody = "{\"role\": \"test\", \"content\": \"example\"}";
        Gson gson = new Gson();
        Message message = null;
        try {
            message = gson.fromJson(responseBody, Message.class);
        } catch (JsonSyntaxException e) {
            System.out.println("ERROR: " + e.getMessage());

        }
        System.out.println(message.role);
        System.out.println(message.content);
    }

    private static class Message {
        public String role;
        public String content;
    }
}
