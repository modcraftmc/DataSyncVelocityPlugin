package fr.modcraftmc;

import com.rabbitmq.client.Channel;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.modcraftmc.message.SaveToDBMessage;
import fr.modcraftmc.message.TransferMessage;
import net.kyori.adventure.text.Component;

public class EventRegister {
    private DataSync plugin;
    private RabbitmqDirectPublisher rabbitmqDirectPublisher;

    public EventRegister(DataSync plugin) {
        this.plugin = plugin;
        this.rabbitmqDirectPublisher = new RabbitmqDirectPublisher(plugin.rabbitmqConnection);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // todo : fire event "save data to db"
        Player player = event.getPlayer();
        ServerConnection serverConnection = player.getCurrentServer().get();
        String message = "%s have been disconnected from the server : %s".formatted(player.getUsername(), serverConnection.getServerInfo().getName());
        plugin.getLogger().debug(message);
        String data = new SaveToDBMessage(player.getUsername(), serverConnection.getServerInfo().getName()).Serialize().toString();
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
        RegisteredServer oldServer = player.getCurrentServer().get().getServer();
        RegisteredServer newServer = event.getResult().getServer().get();
        String message = "%s is connecting to the server : %s".formatted(player.getUsername(), newServer.getServerInfo().getName());
        plugin.getLogger().debug(message);
        String oldServerName = oldServer.getServerInfo().getName();
        String newServerName = newServer.getServerInfo().getName();
        String data = new TransferMessage(player.getUsername(), oldServerName, newServerName).Serialize().toString();
        try {
            rabbitmqDirectPublisher.publish(oldServerName, data);
        } catch (Exception e) {
            plugin.getLogger().error("Error while publishing message to rabbitmq : %s".formatted(e.getMessage()));
        }
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event){
        plugin.loadConfig();
        rabbitmqDirectPublisher = new RabbitmqDirectPublisher(plugin.rabbitmqConnection);
    }
}
