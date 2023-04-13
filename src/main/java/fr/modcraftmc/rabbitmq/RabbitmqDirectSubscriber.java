package fr.modcraftmc.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import fr.modcraftmc.DataSync;
import fr.modcraftmc.References;

import java.io.IOException;

public class RabbitmqDirectSubscriber {
    public static RabbitmqDirectSubscriber instance;

    private final Channel rabbitmqChannel;

    public RabbitmqDirectSubscriber(RabbitmqConnection rabbitmqConnection) {
        this.rabbitmqChannel = rabbitmqConnection.createChannel();
        try {
            rabbitmqChannel.exchangeDeclare(References.DIRECT_EXCHANGE_NAME, "direct");
        } catch (IOException e) {
            DataSync.instance.getLogger().error("Error while creating RabbitMQ exchange");
            throw new RuntimeException(e);
        }
        instance = this;
    }

    public void subscribe(String routingKey, DeliverCallback listener) {
        String queueName = null;
        try {
            queueName = rabbitmqChannel.queueDeclare().getQueue();
            rabbitmqChannel.queueBind(queueName, References.DIRECT_EXCHANGE_NAME, routingKey);
            rabbitmqChannel.basicConsume(queueName, true, listener, consumerTag -> {});
        } catch (IOException e) {
            DataSync.instance.getLogger().error("Error while subscribing to RabbitMQ exchange");
            throw new RuntimeException(e);
        }
    }
}
