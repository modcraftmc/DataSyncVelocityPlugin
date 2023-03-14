package fr.modcraftmc.message;

import com.google.gson.JsonObject;

import java.util.function.Function;

public abstract class BaseMessage<T extends BaseMessage> {
    String messageName = null;

    BaseMessage(String messageName, Function<JsonObject, T> deserializer) {
        this.messageName = messageName;
    }
    protected JsonObject Serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messageName", messageName);
        return jsonObject;
    }

    protected abstract void Handle();
}
