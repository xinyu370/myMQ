package org.example.store;

import com.alibaba.fastjson.JSON;
import org.example.Msg;
import org.example.NettyServer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;


public class MessageStore {
    private static volatile MessageStore  messageStore= null;
    public static MessageStore getInstance(){
        if(messageStore==null){
            synchronized (MessageStore.class){
                if(messageStore==null){
                    messageStore = new MessageStore();
                }
            }
        }
        return messageStore;
    }

    private ConsumeIndexStore consumeIndexStore = null;
    private final AtomicLong offset = new AtomicLong(0);

    public static final String STORE_PATH = "message_store";
    public static final String STORE_FILE_NAME = "commitlog.txt";

    private MessageStore() {
        consumeIndexStore = ConsumeIndexStore.getInstance();
    }

    public void resumeMsg(){
        if(consumeIndexStore.getConsumeIndex()!=null && new File(STORE_PATH + "/" + STORE_FILE_NAME).exists()){
            try(FileChannel fileChannel = FileChannel.open(Paths.get(STORE_PATH + "/" + STORE_FILE_NAME),StandardOpenOption.READ)){
                consumeIndexStore.getConsumeIndex().forEach((k,v)->{
                    ArrayBlockingQueue queue = new ArrayBlockingQueue(1024);
                    v.forEach(ele->{
                        if(!ele.getConsumed()){
                            Long startIndex = ele.getStartIndex();
                            Long endIndex = ele.getEndIndex();
                            byte[] data = null;
                            ByteBuffer buffer = ByteBuffer.allocate((int)(endIndex-startIndex));
                            try {
                                int read = fileChannel.read(buffer, startIndex);
                                if(read>0){
                                    buffer.flip(); //切换缓冲区模式
                                    data = new byte[buffer.remaining()];
                                    buffer.get(data);
                                    queue.add(JSON.parseObject(data,Msg.class));
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });
                    NettyServer.topicQueue.put(k,queue);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("队列消息恢复完成");
            NettyServer.topicQueue.forEach((k,v)->{
                System.out.println("topic:"+k+"values:"+v);
            });
        }
    }

    /**
     * 将消息写入磁盘：
     * 问题：消息是否被消费过？如何读取？如何确保消息被消费过？
     * @throws IOException
     */
    public void putMessage(Msg msg) throws IOException {
        Map<String, List<ConsumeIndex>> consumeIndex = consumeIndexStore.getConsumeIndex();
        if(msg == null){
            return;
        }
        //消息偏移量
        List<ConsumeIndex> consumeIndices = new ArrayList<>();
        if(consumeIndexStore.getConsumeIndex().containsKey(msg.getTitle())){
            consumeIndices = consumeIndex.get(msg.getTitle());
        }else{
            consumeIndex.put(msg.getTitle(),consumeIndices);
        }
        ConsumeIndex index = new ConsumeIndex();
        index.setStartIndex(offset.get());
        index.setConsumed(false);
        index.setMsgId(msg.getMsgId());
        //消息存储
        File file = new File(STORE_PATH + "/" + STORE_FILE_NAME);
        if(!file.exists()){
            file.createNewFile();
        }
        String msgStr = JSON.toJSONString(msg);
        ByteBuffer buffer = ByteBuffer.allocate(msgStr.getBytes().length);
        buffer.put(msgStr.getBytes());
        buffer.flip();
        try( FileChannel fileChannel = FileChannel.open(Paths.get(STORE_PATH + "/" + STORE_FILE_NAME), StandardOpenOption.APPEND)){
            fileChannel.write(buffer);
        }
        // 更新偏移量
        offset.addAndGet(buffer.limit());
        index.setEndIndex(offset.get());
        consumeIndices.add(index);
    }
}
