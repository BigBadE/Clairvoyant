package software.bigbade.clairvoyant.engine.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import software.bigbade.clairvoyant.engine.BattlesnakeEngine;
import software.bigbade.clairvoyant.engine.api.IJsonSerializable;

@Getter
@Setter
public class Position implements IJsonSerializable {
    private int x;
    private int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceSquared(Position other) {
        return Math.pow(x-other.getX(), 2)+Math.pow(y-other.getY(), 2);
    }

    public Position subtract(Position other) {
        return new Position(x-other.getX(), y-other.getY());
    }

    public Position add(Position other) {
        return new Position(x+other.getX(), y+other.getY());
    }

    @Override
    public JsonElement serialize(int apiVersion) {
        JsonObject serialized = new JsonObject();
        serialized.addProperty("x", x);
        serialized.addProperty("y", (apiVersion == 0) ? BattlesnakeEngine.SIZE.getY()-1-y : y);
        return serialized;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Position)) {
            return false;
        }
        Position pos = (Position) obj;
        return pos.getX() == x && pos.getY() == y;
    }

    @Override
    public int hashCode() {
        return (x * 31) + y;
    }
}
