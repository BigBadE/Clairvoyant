package software.bigbade.clairvoyant.engine.util;

import com.google.gson.JsonArray;
import software.bigbade.clairvoyant.engine.api.IJsonSerializable;

import java.util.List;

public final class SerializeUtils {
    private SerializeUtils() {}

    public static JsonArray serializeList(List<? extends IJsonSerializable> list, int apiVersion) {
        JsonArray array = new JsonArray();
        for(IJsonSerializable object : list) {
            array.add(object.serialize(apiVersion));
        }
        return array;
    }
}
