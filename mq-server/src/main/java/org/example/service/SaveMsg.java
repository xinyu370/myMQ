package org.example.service;

import io.netty.bootstrap.Bootstrap;
import org.example.Msg;

import java.io.IOException;

public interface SaveMsg {
    void saveMsg(Msg msg, Boolean autoCreateTopic) throws IOException;
}
