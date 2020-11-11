package software.bigbade.clairvoyant.engine.util;

import javafx.util.Pair;
import lombok.Getter;
import software.bigbade.clairvoyant.engine.game.Battlesnake;
import software.bigbade.clairvoyant.engine.game.BattlesnakeBoard;
import software.bigbade.clairvoyant.engine.game.BattlesnakeState;
import software.bigbade.clairvoyant.engine.game.GameMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateEvaluator {
    private final Battlesnake current;
    private final BattlesnakeBoard board;
    private final List<Battlesnake> enemies = new ArrayList<>();

    @Getter
    private long calculationTime;

    public StateEvaluator(Battlesnake current, BattlesnakeBoard board) {
        this.current = current;
        this.board = board;

        for (Battlesnake enemy : board.getSnakes()) {
            if (!enemy.equals(current)) {
                enemies.add(enemy);
            }
        }
    }

    public GameMove getNextMove(long totalTime) {
        long time = System.currentTimeMillis() + totalTime;

        List<BattlesnakeState> originalStates = new ArrayList<>();
        for (Battlesnake snake : enemies) {
            GameMove best = GameMove.UP;
            double max = 0;
            for (GameMove move : GameMove.values()) {
                double value = getEnemyMoveValue(snake, move, 2);
                if (value > max) {
                    best = move;
                    max = value;
                }
            }
            originalStates.add(new BattlesnakeState(snake.getState()));
            snake.update(best, board, false);
        }

        Map<GameMove, Pair<BattlesnakeState, Double>> moves = new HashMap<>();

        while (checkMove(moves, board, current, time));

        for (int i = 0; i < board.getSnakes().size(); i++) {
            Battlesnake snake = board.getSnakes().get(i);
            if (!snake.equals(current)) {
                snake.setState(originalStates.get(i));
            }
        }

        calculationTime = System.currentTimeMillis()-time;

        return GameMove.UP;
    }

    private boolean checkMove(Map<GameMove, Pair<BattlesnakeState, Double>> moves, BattlesnakeBoard board, Battlesnake current, long time) {
        for (GameMove move : GameMove.values()) {
            BattlesnakeState original = new BattlesnakeState(current.getState());
            if (moves.containsKey(move)) {
                current.setState(moves.get(move).getKey());
            }

            double max = 0;
            for (GameMove next : GameMove.values()) {
                max = Math.max(getMoveValue(current, next, true), max);
            }

            current.update(move, board, false);
            moves.put(move, new Pair<>(current.getState(), max));

            current.setState(original);
            if (System.currentTimeMillis() >= time) {
                return false;
            }
        }
        return true;
    }

    private double getMoveValue(Battlesnake battlesnake, GameMove move, boolean checkEnemies) {
        BattlesnakeState state = battlesnake.getState();
        BattlesnakeState original = new BattlesnakeState(state);
        battlesnake.update(move, board, false);
        double value = checkEnemies ? getStateWithEnemies() : getState(battlesnake);
        battlesnake.setState(original);
        return value;
    }

    private double getEnemyMoveValue(Battlesnake battlesnake, GameMove move, int depth) {
        if (depth > 0) {
            double max = 0;
            for (GameMove nextMove : GameMove.values()) {
                max = Math.max(max, getEnemyMoveValue(battlesnake, nextMove, depth - 1));
            }
            return max;
        } else {
            return getMoveValue(battlesnake, move, false);
        }
    }

    public double getState(Battlesnake battlesnake) {
        //TODO
        return 0d;
    }

    public double getStateWithEnemies() {
        //TODO
        return 0d;
    }
}
