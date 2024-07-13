package org.example.store;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConsumeIndex implements Serializable {

    private Long startIndex;

    private Long endIndex;

    private String msgId;

    private  Boolean consumed;
}
