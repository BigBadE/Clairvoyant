package software.bigbade.clairvoyant.engine.game;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import software.bigbade.clairvoyant.engine.api.IBattlesnakePlayer;
import software.bigbade.clairvoyant.engine.api.IJsonSerializable;
import software.bigbade.clairvoyant.engine.util.Position;
import software.bigbade.clairvoyant.engine.util.SerializeUtils;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Battlesnake implements IJsonSerializable {
    private final String id;

    @Getter
    private final IBattlesnakePlayer player;

    private final int squad;

    @Setter
    @Getter
    private Position head;

    @Getter
    private int health = 100;

    @Getter
    private final List<Position> body = new ArrayList<>();

    private int growing = 3;

    @Getter
    private boolean dead = false;

    public void kill() {
        dead = true;
    }

    public void update(GameMove direction, BattlesnakeBoard board) {
        if(player.getApiVersion() == 0 && direction == GameMove.UP || direction == GameMove.DOWN) {
            direction = direction.getOpposite();
        }
        head = direction.getRelative(head);
        if(head.getX() < 0 || head.getY() < 0 || head.getX() == board.getSize().getX()
                || head.getY() == board.getSize().getY() || board.getBoard()[head.getX()][head.getY()]) {
            dead = true;
            health = 0;
            return;
        }

        boolean eat = board.getFoods().contains(head);
        if(eat) {
            board.getFoods().remove(head);
            growing++;
            health = 101;
        }

        if(health == 0) {
            dead = true;
            return;
        }

        body.add(0, head);
        if(growing > 0) {
            growing--;
        } else {
            body.remove(body.size() - 1);
        }
        health--;
    }

    @Override
    public JsonElement serialize(int apiVersion) {
        JsonObject serialize = new JsonObject();
        serialize.addProperty("id", id);
        serialize.addProperty("name", player.getName());
        serialize.addProperty("health", health);
        serialize.add("body", SerializeUtils.serializeList(body, apiVersion));
        serialize.add("head", head.serialize(apiVersion));
        serialize.addProperty("length", body.size());
        serialize.addProperty("shout", "Clairvoyant is the best!");
        serialize.addProperty("squad", squad);
        return serialize;
    }
}
