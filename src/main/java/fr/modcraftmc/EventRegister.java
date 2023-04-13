package fr.modcraftmc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.modcraftmc.message.PlayerLeaved;
import fr.modcraftmc.message.SaveToDBMessage;
import fr.modcraftmc.message.TransferDataMessage;
import fr.modcraftmc.rabbitmq.RabbitmqDirectPublisher;
import fr.modcraftmc.rabbitmq.RabbitmqPublisher;
import fr.modcraftmc.message.PlayerJoined;

import java.io.IOException;

public class EventRegister {
    private DataSync plugin;

    public EventRegister(DataSync plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        player.getCurrentServer().ifPresentOrElse(serverConnection -> {
            String message = "%s have been disconnected from the server : %s".formatted(player.getUsername(), serverConnection.getServerInfo().getName());
            plugin.getLogger().info(message);

            String serverName = serverConnection.getServerInfo().getName();

            try {
                RabbitmqDirectPublisher.instance.publish(serverName, new SaveToDBMessage(player.getUsername(), serverConnection.getServerInfo().getName()).serializeToString());
                RabbitmqPublisher.instance.publish(new PlayerLeaved(player.getUsername(), serverName).serializeToString());
            } catch (Exception e) {
                plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
            }
        }, () -> {
            plugin.getLogger().warn("Player %s have been disconnected but was not connected to any server".formatted(player.getUsername()));
        });
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer oldServer = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null;
        RegisteredServer newServer = event.getResult().getServer().get();

        String message = "%s is connecting to the server : %s".formatted(player.getUsername(), newServer.getServerInfo().getName());
        plugin.getLogger().info(message);

        String oldServerName = oldServer == null ? "" : oldServer.getServerInfo().getName();
        String newServerName = newServer.getServerInfo().getName();
        boolean isSameGroup = plugin.areServersInSameGroup(oldServerName, newServerName);

        JsonObject jsonData = new TransferDataMessage(player.getUsername(), oldServerName, newServerName, isSameGroup).serialize();
        Gson gson = new Gson();
        String data = gson.toJson(jsonData);

        plugin.getLogger().info("Sending message to rabbitmq : %s".formatted(data));
        try {
            RabbitmqDirectPublisher.instance.publish(oldServerName, data);
        } catch (Exception e) {
            plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
        }
    }

    @Subscribe
    public void onPlayerJoin(ServerConnectedEvent event){
        try {
            RabbitmqPublisher.instance.publish(new PlayerJoined(event.getPlayer().getUsername(), event.getServer().getServerInfo().getName()).serializeToString());
        } catch (IOException e) {
            plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
        }
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event){
        plugin.loadConfig();
    }
}
