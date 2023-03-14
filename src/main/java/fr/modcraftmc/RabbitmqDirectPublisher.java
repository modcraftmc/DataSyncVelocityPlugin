package fr.modcraftmc;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public class RabbitmqDirectPublisher {
    private final String EXCHANGE_NAME = "direct_events";

    private Channel rabbitmqChannel;

    public RabbitmqDirectPublisher(RabbitmqConnection rabbitmqConnection) {
        this.rabbitmqChannel = rabbitmqConnection.createChannel();
    }

    public void publish(String routingKey, String message) throws IOException {
        rabbitmqChannel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
    }
}