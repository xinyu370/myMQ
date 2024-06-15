package org.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private Msg<T> message;

    // Constructors, getters, and setters
    public Response(Msg message) {
        this.message = message;
    }
}
