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

@RequiredArgsConstructor
public class Battlesnake implements IJsonSerializable {
    private final String id;

    @Getter
    private final IBattlesnakePlayer player;

    private final int squad;

    @Setter
    @Getter
    private BattlesnakeState state = new BattlesnakeState();

    public void kill() {
        state.kill();
    }

    public void update(GameMove direction, BattlesnakeBoard board, boolean shouldEat) {
        if(player.getApiVersion() == 0 && direction == GameMove.UP || direction == GameMove.DOWN) {
            direction = direction.getOpposite();
        }
        Position head = direction.getRelative(state.getHead());
        state.setHead(head);
        if(head.getX() < 0 || head.getY() < 0 || head.getX() == board.getSize().getX()
                || head.getY() == board.getSize().getY() || board.getBoard()[head.getX()][head.getY()]) {
            state.kill();
            state.setHealth(0);
            return;
        }

        boolean eat = board.getFoods().contains(head);
        if(eat) {
            if(shouldEat) {
                board.getFoods().remove(head);
            }
            state.grow();
            state.setHealth(101);
        }

        if(state.getHealth() == 0) {
            state.kill();
            return;
        }

        state.getBody().add(0, head);
        state.checkGrowing();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Battlesnake)) {
            return false;
        }
        return state.equals(((Battlesnake) obj).getState());
    }

    @Override
    public JsonElement serialize(int apiVersion) {
        JsonObject serialize = new JsonObject();
        serialize.addProperty("id", id);
        serialize.addProperty("name", player.getName());
        serialize.addProperty("health", state.getHealth());
        serialize.add("body", SerializeUtils.serializeList(state.getBody(), apiVersion));
        serialize.add("head", state.getHead().serialize(apiVersion));
        serialize.addProperty("length", state.getBody().size());
        serialize.addProperty("shout", "Clairvoyant is the best!");
        serialize.addProperty("squad", squad);
        return serialize;
    }
}
