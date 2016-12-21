package ru.mail.park.Controllers;

import org.jetbrains.annotations.NotNull;

public class DescResponse {

    public static final int OK = 0;
    public static final int NOT_FOUND = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int INCORRECT_REQUEST = 3;
    public static final int UNKNOWN_ERROR = 4;
    public static final int ALREADY_EXIST = 5;

    private int code;

    @NotNull
    private Object response;

    public DescResponse(@NotNull Object response, int code) {
        this.response = response;
        this.code = code;
    }
    public DescResponse() {
        this.response = "";
        this.code = -1;
    }

    @NotNull
    public Object getResponse() { return response; }

    public void setResponse(@NotNull Object response) { this.response = response; }

    public int getCode() { return code; }

    public void setCode(int code) { this.code = code; }
}
