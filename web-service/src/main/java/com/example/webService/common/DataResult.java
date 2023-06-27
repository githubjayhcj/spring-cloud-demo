package com.example.webService.common;

public class DataResult<T> {
    // 0  fail , 1 success
    private int code = 1;
    private String message;
    private T Data;

    public DataResult() {
    }

    public DataResult(int code) {
        this.code = code;
    }

    public DataResult(String message) {
        this.message = message;
    }

    public DataResult(T data) {
        Data = data;
    }

    public DataResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public DataResult(int code, T data) {
        this.code = code;
        Data = data;
    }

    public DataResult(String message, T data) {
        this.message = message;
        Data = data;
    }

    public DataResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        Data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return Data;
    }

    public void setData(T data) {
        Data = data;
    }

    @Override
    public String toString() {
        return "DataResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", Data=" + Data +
                '}';
    }
}
