package org.example.store;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.store.ConsumeIndexStore.CONSUMER_INDEX_FILE;

public class demo {
    public static void main(String[] args) {
        try(FileChannel fileChannel = FileChannel.open(Paths.get(MessageStore.STORE_PATH+"/"+CONSUMER_INDEX_FILE),StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int)fileChannel.size());
            //读到缓冲区
            fileChannel.read(buffer);
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            System.out.println(new String(bytes));

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<ConsumeIndex>> map = objectMapper.readValue(new String(bytes), new TypeReference<Map<String, List<ConsumeIndex>>>(){});
            System.out.println("Map: " + map);
            System.out.println("恢复文件成功");
            map.forEach((k,v)->{
                System.out.print("topic:"+k);
                System.out.println(" values:"+v);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
