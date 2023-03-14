package fr.modcraftmc.message;

import com.google.gson.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseMessage<T extends BaseMessage> {

    static final Map<String, Function<JsonObject, ? extends BaseMessage>> messageMap = new HashMap<>();
    String messageName = null;

    BaseMessage(String messageName, Function<JsonObject, T> deserializer) {
        this.messageName = messageName;
        if(!messageMap.containsKey(messageName))
            messageMap.put(messageName, deserializer);
    }

    static BaseMessage GetInstance(JsonObject message){
        String messageName = message.get("messageName").getAsString();
        if(messageMap.containsKey(messageName))
            return messageMap.get(messageName).apply(message);
        return null;
    }
    protected JsonObject Serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("messageName", messageName);
        return jsonObject;
    }
}
