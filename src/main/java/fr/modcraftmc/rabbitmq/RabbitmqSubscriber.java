package fr.modcraftmc.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import fr.modcraftmc.DataSync;
import fr.modcraftmc.References;

import java.io.IOException;

public class RabbitmqSubscriber {
    public static RabbitmqSubscriber instance;

    private final Channel rabbitmqChannel;

    public RabbitmqSubscriber(RabbitmqConnection rabbitmqConnection) {
        this.rabbitmqChannel = rabbitmqConnection.createChannel();
        try {
            rabbitmqChannel.exchangeDeclare(References.GLOBAL_EXCHANGE_NAME, "fanout");
        } catch (IOException e) {
            DataSync.instance.getLogger().error("Error while creating RabbitMQ exchange");
            throw new RuntimeException(e);
        }
        instance = this;
    }

    public void subscribe(DeliverCallback listener) {
        String queueName = null;
        try {
            queueName = rabbitmqChannel.queueDeclare().getQueue();
            rabbitmqChannel.queueBind(queueName, References.GLOBAL_EXCHANGE_NAME, "");
            rabbitmqChannel.basicConsume(queueName, true, listener, consumerTag -> {});
        } catch (IOException e) {
            DataSync.instance.getLogger().error("Error while subscribing to RabbitMQ exchange");
            throw new RuntimeException(e);
        }
    }
}
