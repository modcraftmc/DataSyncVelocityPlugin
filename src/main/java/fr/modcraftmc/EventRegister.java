package fr.modcraftmc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.modcraftmc.message.SaveToDBMessage;
import fr.modcraftmc.message.TransferMessage;
import fr.modcraftmc.rabbitmq.RabbitmqDirectPublisher;

import java.io.IOException;

public class EventRegister {
    private DataSync plugin;
    private RabbitmqDirectPublisher rabbitmqDirectPublisher;

    public EventRegister(DataSync plugin) {
        this.plugin = plugin;
        try {
            this.rabbitmqDirectPublisher = new RabbitmqDirectPublisher(plugin.rabbitmqConnection);
        } catch (IOException e) {
            plugin.getLogger().error("Error while creating rabbitmq direct publisher : %s".formatted(e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // todo : fire event "save data to db"
        Player player = event.getPlayer();
        ServerConnection serverConnection = player.getCurrentServer().get();

        String message = "%s have been disconnected from the server : %s".formatted(player.getUsername(), serverConnection.getServerInfo().getName());
        plugin.getLogger().info(message);

        JsonObject jsonData = new SaveToDBMessage(player.getUsername(), serverConnection.getServerInfo().getName()).Serialize();
        Gson gson = new Gson();
        String data = gson.toJson(jsonData);
        String serverName = serverConnection.getServerInfo().getName();

        try {
            rabbitmqDirectPublisher.publish(serverName, data);
        } catch (Exception e) {
            plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
        }
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        // todo : fire event "send data to message queue"
        Player player = event.getPlayer();
        RegisteredServer oldServer = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null;
        RegisteredServer newServer = event.getResult().getServer().get();

        String message = "%s is connecting to the server : %s".formatted(player.getUsername(), newServer.getServerInfo().getName());
        plugin.getLogger().info(message);

        String oldServerName = oldServer == null ? "" : oldServer.getServerInfo().getName();
        String newServerName = newServer.getServerInfo().getName();
        boolean isSameGroup = plugin.areServersInSameGroup(oldServerName, newServerName);

        JsonObject jsonData = new TransferMessage(player.getUsername(), oldServerName, newServerName, isSameGroup).Serialize();
        Gson gson = new Gson();
        String data = gson.toJson(jsonData);

        plugin.getLogger().info("Sending message to rabbitmq : %s".formatted(data));
        try {
            rabbitmqDirectPublisher.publish(oldServerName, data);
        } catch (Exception e) {
            plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
        }
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event){
        plugin.loadConfig();
        try {
            rabbitmqDirectPublisher = new RabbitmqDirectPublisher(plugin.rabbitmqConnection);
        } catch (IOException e) {
            plugin.getLogger().error("Error while creating rabbitmq direct publisher : %s".formatted(e.getMessage()));
            throw new RuntimeException(e);
        }
    }
}
