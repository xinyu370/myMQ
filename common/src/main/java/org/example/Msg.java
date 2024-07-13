package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Msg<T> {

    public Msg(String title,T msg){
        this.title = title;
        this.msg = msg;
    }
    private String msgId;

    private String title;

    private T msg;
}
