package software.bigbade.clairvoyant.engine.game;

import javafx.geometry.Pos;
import lombok.Getter;
import lombok.Setter;
import software.bigbade.clairvoyant.engine.util.Position;

import java.util.ArrayList;
import java.util.List;

public class BattlesnakeState {
    @Setter
    @Getter
    private Position head;

    @Setter
    @Getter
    private int health = 100;

    @Getter
    private final List<Position> body = new ArrayList<>();

    private int growing = 3;

    @Getter
    private boolean dead = false;

    public BattlesnakeState() { }

    public BattlesnakeState(BattlesnakeState original) {
        head = new Position(original.head);
        health = original.health;
        for(Position position : original.body) {
            body.add(new Position(position));
        }
        growing = original.growing;
        dead = original.dead;
    }

    public void kill() {
        dead = true;
    }

    public void grow() {
        growing++;
    }

    public void finishGrowing() {
        growing--;
    }

    public void checkGrowing() {
        if(growing > 0) {
            growing--;
        } else {
            body.remove(body.size() - 1);
        }
        health--;
    }
}
