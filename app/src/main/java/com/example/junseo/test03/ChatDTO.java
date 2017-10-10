package com.example.junseo.test03;

public class ChatDTO {

    //  private String userName;
    private String message;


    public ChatDTO() {}
    public ChatDTO(String message) {
        // this.userName = userName;
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}