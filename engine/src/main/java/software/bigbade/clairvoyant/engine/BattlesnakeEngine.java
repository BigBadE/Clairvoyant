package software.bigbade.clairvoyant.engine;

import software.bigbade.clairvoyant.engine.api.IBattlesnakePlayer;
import software.bigbade.clairvoyant.engine.factory.BattleSnakeFactory;
import software.bigbade.clairvoyant.engine.game.Battlesnake;
import software.bigbade.clairvoyant.engine.game.BattlesnakeBoard;
import software.bigbade.clairvoyant.engine.game.BattlesnakeGame;
import software.bigbade.clairvoyant.engine.game.GameMove;
import software.bigbade.clairvoyant.engine.util.Position;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BattlesnakeEngine {
    public static final int TIMEOUT = 500;
    public static final Position SIZE = new Position(11, 11);

    private final List<IBattlesnakePlayer> players = new ArrayList<>();

    private BattlesnakeGame game = new BattlesnakeGame();
    private BattlesnakeBoard board = new BattlesnakeBoard(SIZE);

    private int turn = 0;

    private boolean running = true;

    public BattlesnakeEngine(List<String> snakes) {
        for (String url : snakes) {
            players.add(BattleSnakeFactory.getBattlesnake(url));
        }
    }

    public void runToEnd(boolean print, boolean placeFood) {
        start();
        while (running) {
            long time = System.currentTimeMillis();
            tick(print, placeFood);
            while (System.currentTimeMillis() - time < TIMEOUT) {
                //Wait until next tick
            }
        }
    }

    public void start() {
        board.placeSnakes(players);
        board.placeStartFood();
        for (Battlesnake battlesnake : board.getSnakes()) {
            battlesnake.getPlayer().start(game, board, battlesnake);
        }
    }

    public void tick(boolean print, boolean placeFood) {
        if (!running) {
            throw new IllegalStateException("Cannot tick ended game!");
        }
        turn++;
        CompletableFuture<GameMove>[] moves = new CompletableFuture[players.size()];
        for (int i = 0; i < players.size(); i++) {
            moves[i] = players.get(i).getNextMove(game, board, turn);
        }
        for (int i = 0; i < board.getSnakes().size(); i++) {
            try {
                GameMove move = moves[i].get(2, TimeUnit.SECONDS);
                board.getSnakes().get(i).update(move, board, true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                board.getSnakes().get(i).update(GameMove.UP, board, true);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        board.update();
        if(placeFood && (board.getFoods().isEmpty() || board.getRandom().nextFloat() <= 0.15)) {
            board.placeFood(1);
        }
        if (board.ended()) {
            for (IBattlesnakePlayer player : players) {
                player.end(game, board, turn);
            }
            running = false;
        }
        if (print) {
            board.print();
        }
    }
}
