package fr.modcraftmc;

import com.rabbitmq.client.Channel;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public class EventRegister {
    private DataSync plugin;
    private Channel rabbitmqChannel;

    public EventRegister(DataSync plugin) {
        this.plugin = plugin;
        this.rabbitmqChannel = plugin.rabbitmqConnection.createChannel();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // todo : fire event "save data to db" and "send data to message queue"
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        // todo : fire event "read data from message queue"
    }
}
