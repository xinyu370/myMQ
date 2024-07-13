package org.example;


public class TestProducer {
    public static void main(String[] args) throws Exception {
        testProducer();
    }
    public static void testProducer() throws Exception {
        SendClient sendClient = new SendClient();
        sendClient.setHost("127.0.0.1");
        sendClient.setPort(8080);
        sendClient.start();
        for (int i = 0; i < 40; i++) {
            sendClient.send(new Msg("测试","hello world"+i));
        }

        //sendClient.shutDown();
    }
}
