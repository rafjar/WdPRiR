package Rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class SendMsgs implements Runnable {
    private static final String EXCHANGE_NAME = "logs";
    private Connection connection;
    private Channel channel;

    SendMsgs() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
    }

    public void run() {
        Scanner input = new Scanner(System.in);
        while(true) {
            String message = input.nextLine();
            try {
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}