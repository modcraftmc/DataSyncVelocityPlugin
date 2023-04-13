package fr.modcraftmc.rabbitmq;

import com.rabbitmq.client.Channel;
import fr.modcraftmc.DataSync;
import fr.modcraftmc.References;

import java.io.IOException;

public class RabbitmqPublisher {
    public static RabbitmqPublisher instance;

    private final Channel rabbitmqChannel;

    public RabbitmqPublisher(RabbitmqConnection rabbitmqConnection) {
        this.rabbitmqChannel = rabbitmqConnection.createChannel();
        try {
            rabbitmqChannel.exchangeDeclare(References.GLOBAL_EXCHANGE_NAME, "fanout");
        } catch (IOException e) {
            DataSync.instance.getLogger().error("Error while creating RabbitMQ exchange");
            throw new RuntimeException(e);
        }
        instance = this;
    }

    public void publish(String message) throws IOException {
        DataSync.instance.getLogger().debug(String.format("Publishing message to %s", References.GLOBAL_EXCHANGE_NAME));
        rabbitmqChannel.basicPublish(References.GLOBAL_EXCHANGE_NAME, "", null, message.getBytes());
    }
}
