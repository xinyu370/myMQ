package org.example.service.impl;

import org.example.Msg;
import org.example.NettyServer;
import org.example.service.GetMsg;
import org.example.store.ConsumeIndexStore;

import java.util.concurrent.ArrayBlockingQueue;

public class GetMsgImpl implements GetMsg {
    private ConsumeIndexStore consumeIndexStore= ConsumeIndexStore.getInstance();
    @Override
    public Msg getMsg(String topic) {
        ArrayBlockingQueue arrayBlockingQueue = NettyServer.topicQueue.get(topic);
        Msg msg = (Msg)arrayBlockingQueue.poll();
        consumeIndexStore.updateConsumeIndex(msg);
        return msg;
    }
}
