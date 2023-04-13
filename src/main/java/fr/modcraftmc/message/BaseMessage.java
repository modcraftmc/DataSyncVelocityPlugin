package fr.modcraftmc.message;

import com.google.gson.JsonObject;

public abstract class BaseMessage {
    String messageName;

    BaseMessage(String messageName) {
        this.messageName = messageName;
    }
    protected JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messageName", messageName);
        return jsonObject;
    }

    public String serializeToString() {
        return serialize().toString();
    }

    protected abstract void handle();
}
