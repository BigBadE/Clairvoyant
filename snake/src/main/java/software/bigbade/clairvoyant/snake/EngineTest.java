package software.bigbade.clairvoyant.snake;

import lombok.SneakyThrows;
import software.bigbade.clairvoyant.engine.BattlesnakeEngine;

import java.util.Collections;

public class EngineTest {
    @SneakyThrows
    public static void main(String[] args) {
        new BattlesnakeEngine(Collections.singletonList("https://walllover.herokuapp.com/")).runToEnd(true, false);
    }
}
