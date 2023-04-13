package fr.modcraftmc.message;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.modcraftmc.DataSync;

public class TransferPlayer extends BaseMessage{
    public static final String MESSAGE_NAME = "TransferPlayer";

    public String playerName;
    public String serverName;

    public TransferPlayer(String playerName, String serverName) {
        super(MESSAGE_NAME);
        this.playerName = playerName;
        this.serverName = serverName;
    }

    @Override
    protected JsonObject serialize() {
        JsonObject jsonObject = super.serialize();
        jsonObject.addProperty("serverName", serverName);
        jsonObject.addProperty("playerName", playerName);
        return jsonObject;
    }

    public static TransferPlayer deserialize(JsonObject json) {
        String serverName = json.get("serverName").getAsString();
        String playerName = json.get("playerName").getAsString();
        return new TransferPlayer(playerName, serverName);
    }

    @Override
    protected void handle() {
        ProxyServer proxy = DataSync.instance.getProxyServer();
        proxy.getPlayer(playerName).ifPresent( player -> {
            proxy.getServer(serverName).ifPresent( server -> {
                player.createConnectionRequest(server).connect();
            });
        });
    }
}
