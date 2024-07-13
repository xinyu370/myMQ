package org.example.service.impl;

import org.example.Msg;
import org.example.NettyServer;
import org.example.service.SaveMsg;
import org.example.store.MessageStore;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveMsgImpl implements SaveMsg {

    private AtomicInteger atomicInteger = new AtomicInteger();
    private MessageStore messageStore = MessageStore.getInstance();
    @Override
    public void saveMsg(Msg msg, Boolean autoCreateTopic) throws IOException {
        msg.setMsgId(UUID.randomUUID().toString());
        if(msg != null){
            if(NettyServer.topicQueue.containsKey(msg.getTitle())){
                save(msg);
            }else{
                if(autoCreateTopic){
                    NettyServer.topicQueue.put(msg.getTitle(),new ArrayBlockingQueue(1024));
                    save(msg);
                }
            }
        }
    }

    private void save(Msg msg) throws IOException {
        ArrayBlockingQueue queue = NettyServer.topicQueue.get(msg.getTitle());
        queue.add(msg);
        messageStore.putMessage(msg);

    }
}
