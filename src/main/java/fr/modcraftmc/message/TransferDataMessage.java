package fr.modcraftmc.message;

import com.google.gson.JsonObject;

public class TransferDataMessage extends BaseMessage {
    public static final String MESSAGE_NAME = "TransferDataMessage";
    private final String playerName;
    private final String oldServerName;
    private final String newServerName;
    private final boolean areLinked;

    public TransferDataMessage(String playerName, String oldServerName, String newServerName, boolean areLinked) {
        super(MESSAGE_NAME);
        this.playerName = playerName;
        this.oldServerName = oldServerName;
        this.newServerName = newServerName;
        this.areLinked = areLinked;
    }

    public JsonObject serialize() {
        JsonObject jsonObject = super.serialize();
        jsonObject.addProperty("oldServerName", oldServerName);
        jsonObject.addProperty("newServerName", newServerName);
        jsonObject.addProperty("playerName", playerName);
        jsonObject.addProperty("areLinked", areLinked);
        return jsonObject;
    }

    @Override
    protected void handle() {}

    protected static TransferDataMessage deserialize(JsonObject json) {
        String oldServerName = json.get("oldServerName").getAsString();
        String newServerName = json.get("newServerName").getAsString();
        String playerName = json.get("playerName").getAsString();
        boolean areLinked = json.get("areLinked").getAsBoolean();
        return new TransferDataMessage(playerName, oldServerName, newServerName, areLinked);
    }
}
