package software.bigbade.clairvoyant.engine.game;

import com.google.gson.JsonObject;
import software.bigbade.clairvoyant.engine.BattlesnakeEngine;
import software.bigbade.clairvoyant.engine.api.IJsonSerializable;

import java.util.UUID;

public class BattlesnakeGame implements IJsonSerializable {
    private final String id = UUID.randomUUID().toString();

    @Override
    public JsonObject serialize(int apiVersion) {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("timeout", BattlesnakeEngine.TIMEOUT);
        return object;
    }
}
