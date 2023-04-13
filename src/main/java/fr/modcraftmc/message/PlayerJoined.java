package fr.modcraftmc.message;

import com.google.gson.JsonObject;

public class PlayerJoined extends BaseMessage{
    public static final String MESSAGE_NAME = "PlayerJoined";

    public String playerName;
    public String serverName;

    public PlayerJoined(String playerName, String serverName) {
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

    public static PlayerJoined deserialize(JsonObject json) {
        String serverName = json.get("serverName").getAsString();
        String playerName = json.get("playerName").getAsString();
        return new PlayerJoined(playerName, serverName);
    }

    @Override
    protected void handle() {
        //handle is on server side
    }
}
