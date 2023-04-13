package fr.modcraftmc.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.modcraftmc.DataSync;
import fr.modcraftmc.rabbitmq.RabbitmqDirectSubscriber;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MessageHandler {
    static final Map<String, Function<JsonObject, ? extends BaseMessage>> messageMap = new HashMap<>();
    public static Gson GSON = new Gson();

    public static void init(){
        messageMap.put(TransferDataMessage.MESSAGE_NAME, TransferDataMessage::deserialize);
        messageMap.put(SaveToDBMessage.MESSAGE_NAME, SaveToDBMessage::deserialize);
        messageMap.put(PlayerJoined.MESSAGE_NAME, PlayerJoined::deserialize);
        messageMap.put(PlayerLeaved.MESSAGE_NAME, PlayerLeaved::deserialize);
        messageMap.put(TransferPlayer.MESSAGE_NAME, TransferPlayer::deserialize);

        DataSync.instance.onConfigLoad.add(() -> {
            RabbitmqDirectSubscriber.instance.subscribe("proxy", (consumerTag, message) -> {
                DataSync.instance.getLogger().debug("Received message: " + new String(message.getBody()));
                String messageJson = new String(message.getBody(), StandardCharsets.UTF_8);
                MessageHandler.handle(messageJson);
            });
        });
    }

    public static void handle(JsonObject message){
        if(messageMap.containsKey(message.get("messageName").getAsString()))
            messageMap.get(message.get("messageName").getAsString()).apply(message).handle();
        else
            DataSync.instance.getLogger().error("Message id not found");
    }

    public static void handle(String message){
        handle(GSON.fromJson(message, JsonObject.class));
    }
}
