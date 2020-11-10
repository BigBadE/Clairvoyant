package software.bigbade.clairvoyant.engine.api;

import software.bigbade.clairvoyant.engine.game.Battlesnake;
import software.bigbade.clairvoyant.engine.game.BattlesnakeBoard;
import software.bigbade.clairvoyant.engine.game.BattlesnakeGame;
import software.bigbade.clairvoyant.engine.game.GameMove;

import java.util.concurrent.CompletableFuture;

public interface IBattlesnakePlayer {
    int getApiVersion();

    void start(BattlesnakeGame game, BattlesnakeBoard board, Battlesnake snake);

    CompletableFuture<GameMove> getNextMove(BattlesnakeGame game, BattlesnakeBoard board, int turn);

    void end(BattlesnakeGame game, BattlesnakeBoard board, int turn);

    String getName();
}
