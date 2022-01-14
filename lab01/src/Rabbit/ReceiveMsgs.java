package Rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

public class ReceiveMsgs implements Runnable {
    private static final String EXCHANGE_NAME = "logs";
    private Channel channel;
    private DeliverCallback deliverCallback;
    String queueName;

    ReceiveMsgs() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received message: " + message);
        };
    }

    @Override
    public void run() {
        try {
            while(true)
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}