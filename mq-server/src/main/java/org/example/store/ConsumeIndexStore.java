package org.example.store;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializable;
import com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Msg;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ConsumeIndexStore {
    private static final ConsumeIndexStore consumeIndexStore = new ConsumeIndexStore();
    public static ConsumeIndexStore getInstance(){
       return consumeIndexStore;
    }
    public Map<String, List<ConsumeIndex>> consumeIndex = new ConcurrentHashMap<>();

    public Map<String, List<ConsumeIndex>> getConsumeIndex(){
        return consumeIndex;
    }
    public static final String CONSUMER_INDEX_FILE = "consumeIndex.txt";

    ScheduledExecutorService druidExecutors = Executors.newScheduledThreadPool(1);
    ScheduledExecutorService clearExecutors = Executors.newScheduledThreadPool(1);

    private ConsumeIndexStore(){
        scheldStoreConsumerIndex();
        clearConsumedMsg();
    }

    public void shutDown(){
        druidExecutors.shutdown();
        clearExecutors.shutdown();
    }

    public void resumeMsg(){
        consumeIndexInit();
    }

    //有消息被消费，更新consumeIndex
    public void updateConsumeIndex(Msg msg){
        if(msg == null){
            return;
        }
        if(consumeIndex.containsKey(msg.getTitle())){
            List<ConsumeIndex> consumeIndices = consumeIndex.get(msg.getTitle());
            Map<String, ConsumeIndex> msgMap = consumeIndices.stream().collect(Collectors.toMap(ConsumeIndex::getMsgId, a -> a, (a, b) -> a));
            ConsumeIndex consumeIndex1 = msgMap.get(msg.getMsgId());
            if(consumeIndex1!=null){
                consumeIndex1.setConsumed(true);
            }
        }
    }

    //定时持久化消费队列
    public void scheldStoreConsumerIndex(){
        druidExecutors.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                File file = new File(MessageStore.STORE_PATH+"/"+CONSUMER_INDEX_FILE);
                if(!file.exists()){
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try(FileChannel writeChannel = FileChannel.open(Paths.get(MessageStore.STORE_PATH+"/"+CONSUMER_INDEX_FILE), StandardOpenOption.WRITE)){
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonString = objectMapper.writeValueAsString(consumeIndex);
                    byte[] bytes = jsonString.getBytes();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                    byteBuffer.put(bytes);
                    //下面这条不能忘
                    byteBuffer.flip();
                    writeChannel.write(byteBuffer);
                    byteBuffer.flip();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        },0,5, TimeUnit.SECONDS);
    }


    /**
     * 定时清除，已经被消费的ConsumeIndex
     */
    public void clearConsumedMsg(){

        clearExecutors.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Set<String> keys = consumeIndex.keySet();
                for(String key : keys){
                    List<ConsumeIndex> indexList = consumeIndex.get(key);
                    if(indexList!=null){
                        List<ConsumeIndex> liveIndex = new ArrayList<>();
                        indexList.forEach(ele->{
                            if(!ele.getConsumed()){
                                liveIndex.add(ele);
                            }
                        });
                        consumeIndex.put(key,liveIndex);
                    }

                }
            }
        },60,60, TimeUnit.SECONDS);
    }

    /**
     * 每次启动前需要先保证该方法被调用
     */
    public void consumeIndexInit(){
        File file = new File(MessageStore.STORE_PATH+"/"+CONSUMER_INDEX_FILE);
        if(!file.exists()){
            return;
        }
        try(FileChannel fileChannel = FileChannel.open(Paths.get(MessageStore.STORE_PATH+"/"+CONSUMER_INDEX_FILE),StandardOpenOption.READ)) {
            if(fileChannel.size()==0){
                consumeIndex = new ConcurrentHashMap<>();
                return;
            }
            ByteBuffer buffer = ByteBuffer.allocate((int)fileChannel.size());
            //读到缓冲区
            fileChannel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            ObjectMapper objectMapper = new ObjectMapper();
            consumeIndex = objectMapper.readValue(new String(bytes), new TypeReference<ConcurrentHashMap<String, List<ConsumeIndex>>>(){});
//            consumeIndex = (Map)JSON.parse(bytes);
//            consumeIndex = (Map<String, List<ConsumeIndex>>) byteToObj(bytes);
            System.out.println("恢复文件成功");
            consumeIndex.forEach((k,v)->{
                System.out.print("topic:"+k);
                System.out.println(" values:"+v);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MessageStore.getInstance().resumeMsg();
    }
}
