package software.bigbade.clairvoyant.engine.game;

import com.google.gson.JsonObject;
import lombok.Getter;
import software.bigbade.clairvoyant.engine.BattlesnakeEngine;
import software.bigbade.clairvoyant.engine.api.IBattlesnakePlayer;
import software.bigbade.clairvoyant.engine.api.IJsonSerializable;
import software.bigbade.clairvoyant.engine.util.Position;
import software.bigbade.clairvoyant.engine.util.SerializeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public class BattlesnakeBoard implements IJsonSerializable {
    @Getter
    private final Random random = new Random();
    private final Position size;

    private final List<Position> foods = new ArrayList<>();
    private final List<Position> hazards = new ArrayList<>();
    private final List<Battlesnake> snakes = new ArrayList<>();

    private boolean[][] board;

    public BattlesnakeBoard(Position size) {
        this.size = size;
    }

    public void placeFood(int amount) {
        for(int i = 0; i < amount; i++) {
            int x = random.nextInt(BattlesnakeEngine.SIZE.getX());
            int y = random.nextInt(BattlesnakeEngine.SIZE.getY());
            Position position = new Position(x, y);
            while (board[x][y] && !foods.contains(position)) {
                x = random.nextInt(BattlesnakeEngine.SIZE.getX());
                y = random.nextInt(BattlesnakeEngine.SIZE.getY());
                position = new Position(x, y);
            }
            foods.add(position);
        }
    }

    public boolean isStandardSize() {
        return (size.getX() == 7 || size.getX() == 11 || size.getX() == 19)
                && (size.getY() == 7 || size.getY() == 11 || size.getY() == 19);
    }
    public void placeStartFood() {
        if(isStandardSize()) {
            for(Battlesnake snake : snakes) {
                foods.add(GameMove.values()[random.nextInt(4)]
                        .getRelative(GameMove.values()[random.nextInt(4)].getRelative(snake.getHead())));
            }

            foods.add(new Position(size.getX()/2, size.getY()/2));
        } else {
            placeFood(snakes.size());
        }
    }

    public void update() {
        board = new boolean[size.getX()][size.getY()];
        for (Position hazard : hazards) {
            board[hazard.getX()][hazard.getY()] = true;
        }

        for (Battlesnake snake : snakes) {
            if(snake.isDead()) {
                continue;
            }
            for (Position body : snake.getBody()) {
                board[body.getX()][body.getY()] = true;
            }
            for (Battlesnake other : snakes) {
                if(other.isDead()) {
                    continue;
                }
                if (!snake.equals(other) && snake.getHead().equals(other.getHead())) {
                    switch (Integer.compare(snake.getBody().size(), other.getBody().size())) {
                        case 1:
                            other.kill();
                            break;
                        case 0:
                            snake.kill();
                            other.kill();
                            break;
                        case -1:
                            snake.kill();
                    }
                }
            }
        }
    }

    public void print() {
        System.out.println("----------------------");
        char[][] charBoard = new char[size.getX()][size.getY() * 2];
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                charBoard[y][x * 2] = ' ';
                charBoard[y][x * 2 + 1] = 'O';
            }
        }
        for (Position food : foods) {
            charBoard[food.getY()][food.getX() * 2 + 1] = 'A';
        }
        for (Battlesnake battlesnake : snakes) {
            if(battlesnake.isDead()) {
                continue;
            }
            for (int i = 0; i < battlesnake.getBody().size(); i++) {
                Position body = battlesnake.getBody().get(i);
                charBoard[body.getY()][body.getX() * 2 + 1] = '@';
            }
            charBoard[battlesnake.getHead().getY()][battlesnake.getHead().getX() * 2 + 1] = '#';
        }
        for (Position hazard : hazards) {
            charBoard[hazard.getY()][hazard.getX() * 2 + 1] = 'H';
        }

        for (int i = charBoard.length-1; i >= 0; i--) {
            System.out.println(charBoard[i]);
        }
    }

    public void placeSnakes(List<IBattlesnakePlayer> players) {
        for (int i = 0; i < players.size(); i++) {
            snakes.add(new Battlesnake(UUID.randomUUID().toString(), players.get(i), i + 1));
        }
        for (int i = 0; i < players.size(); i++) {
            snakes.get(i).setHead(new Position(size.getX()/(players.size()+1), size.getY() / 2));
        }
        board = new boolean[size.getX()][size.getY()];
        for (Battlesnake battlesnake : snakes) {
            board[battlesnake.getHead().getX()][battlesnake.getHead().getY()] = true;
        }
        //TODO
    }

    public boolean ended() {
        int alive = 0;
        for (Battlesnake snake : snakes) {
            if (!snake.isDead()) {
                alive++;
            }
        }
        if (snakes.size() == 1) {
            return alive == 0;
        } else {
            return alive <= 1;
        }
    }

    @Override
    public JsonObject serialize(int apiVersion) {
        JsonObject serialized = new JsonObject();
        serialized.addProperty("height", size.getY());
        serialized.addProperty("width", size.getX());
        serialized.add("food", SerializeUtils.serializeList(foods, apiVersion));
        serialized.add("hazards", SerializeUtils.serializeList(hazards, apiVersion));
        serialized.add("snakes", SerializeUtils.serializeList(snakes, apiVersion));
        return serialized;
    }
}
