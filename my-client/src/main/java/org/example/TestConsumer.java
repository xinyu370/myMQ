package org.example;

public class TestConsumer {
    public static void main(String[] args) throws Exception {
      testConsumer();
        //testProducer();
    }



    public static void testConsumer() throws Exception {
        ConsumerClient client = new ConsumerClient();
        MessageListener messageListener =(msg)->{
            System.out.println("消费者获取消息："+msg.getMsg());
        };
        client.setMessageListener(messageListener);
        client.setHost("127.0.0.1");
        client.setPort(8080);
        client.start();
        client.getMessage();
    }


}