package software.bigbade.clairvoyant.engine.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface IJsonSerializable {
    JsonElement serialize(int apiVersion);
}
