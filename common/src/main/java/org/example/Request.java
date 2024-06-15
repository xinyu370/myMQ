package org.example;

import lombok.Data;

import java.io.Serializable;

@Data
public class Request<T>  implements Serializable {
    private static final long serialVersionUID = 1L;
    private RequestType type;
    private Msg<T> message;

    // Constructors, getters, and setters
    public Request(RequestType type, Msg message) {
        this.type = type;
        this.message = message;
    }
}
