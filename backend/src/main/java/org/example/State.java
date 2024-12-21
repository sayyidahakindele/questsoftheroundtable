package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class State<T> {
    //    @JsonProperty("message")
    public String message;

    public String context;

    //    @JsonProperty("data")
    public T data;  // Generic type for flexible data

    public State(String message, String context, T data) {
        this.message = message;
        this.context = context;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public String getContext() {
        return context;
    }

    public T getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
