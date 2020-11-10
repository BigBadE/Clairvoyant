package software.bigbade.clairvoyant.engine.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import software.bigbade.clairvoyant.engine.BattlesnakeEngine;
import software.bigbade.clairvoyant.engine.api.IBattlesnakePlayer;
import software.bigbade.clairvoyant.engine.game.Battlesnake;
import software.bigbade.clairvoyant.engine.game.BattlesnakeBoard;
import software.bigbade.clairvoyant.engine.game.BattlesnakeGame;
import software.bigbade.clairvoyant.engine.game.GameMove;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class NetworkPlayer implements IBattlesnakePlayer {
    private static final ExecutorService service = Executors.newCachedThreadPool();

    @Setter(AccessLevel.PRIVATE)
    private static JsonObject lastCachedObject = null;
    @Setter(AccessLevel.PRIVATE)
    private static int lastCachedTurn = 0;

    private final String playerURL;

    @Getter
    private int apiVersion = 0;

    private Battlesnake snake;

    private final AtomicLong latency = new AtomicLong();

    public NetworkPlayer(String playerURL) {
        this.playerURL = playerURL;
        service.submit(() -> {
            long start = System.currentTimeMillis();
            Optional<JsonObject> response = sendPostRequest(new JsonObject(), "start");
            latency.set(System.currentTimeMillis()-start);
            response.ifPresent(json -> {
                if(json.has("apiversion")) {
                    apiVersion = json.get("apiversion").getAsInt();
                }
            });
        });
    }

    @Override
    public void start(BattlesnakeGame game, BattlesnakeBoard board, Battlesnake snake) {
        this.snake = snake;
        service.submit(() -> {
            JsonObject sending;
            if (lastCachedTurn == 0 && lastCachedObject != null) {
                sending = lastCachedObject;
            } else {
                sending = getGameRequest(game, board, 0);

                setLastCachedObject(sending);
                setLastCachedTurn(0);
            }
            long start = System.currentTimeMillis();
            sendPostRequest(sending, "start");
            latency.set(System.currentTimeMillis()-start);
        });
    }

    @Override
    public CompletableFuture<GameMove> getNextMove(BattlesnakeGame game, BattlesnakeBoard board, int turn) {
        return CompletableFuture.supplyAsync(() -> {
            JsonObject sending;
            if(lastCachedTurn == turn && lastCachedObject != null) {
                sending = lastCachedObject;
            } else {
                sending = getGameRequest(game, board, turn);

                setLastCachedObject(sending);
                setLastCachedTurn(turn);
            }
            long start = System.currentTimeMillis();
            JsonObject response = sendPostRequest(sending, "move").orElse(new JsonObject());
            latency.set(System.currentTimeMillis()-start);
            if(!response.has("move")) {
                return GameMove.UP;
            }

            try {
                return GameMove.valueOf(response.get("move").getAsString().toUpperCase());
            } catch (IllegalArgumentException | ClassCastException | IllegalStateException e) {
                return GameMove.UP;
            }
        }, service);
    }

    @Override
    public void end(BattlesnakeGame game, BattlesnakeBoard board, int turn) {
        service.submit(() -> {
            JsonObject sending;
            if (lastCachedTurn == turn && lastCachedObject != null) {
                sending = lastCachedObject;
            } else {
                sending = getGameRequest(game, board, turn);
                setLastCachedObject(sending);
                setLastCachedTurn(turn);
            }
            sendPostRequest(sending, "end");
        });
    }

    private JsonObject getGameRequest(BattlesnakeGame game, BattlesnakeBoard board, int turn) {
        JsonObject sending = new JsonObject();
        sending.add("game", game.serialize(apiVersion));
        sending.addProperty("turn", turn);
        sending.add("board", board.serialize(apiVersion));

        JsonObject sendingSnake = (JsonObject) snake.serialize(apiVersion);
        sendingSnake.addProperty("latency", latency.get());
        sending.add("you", sendingSnake);

        return sending;
    }

    @Override
    public String getName() {
        return "Clairvoyant-Network-Enemy";
    }

    private Optional<JsonObject> sendPostRequest(JsonObject sending, String type) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(playerURL + type).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(BattlesnakeEngine.TIMEOUT);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            writeBytesToConnection(connection, sending.toString().getBytes(StandardCharsets.UTF_8));
            if(connection.getResponseCode() != 200) {
                return Optional.empty();
            }
            JsonObject returned = readInputStream(connection.getInputStream());
            return Optional.of(returned);
        } catch (IOException | ClassCastException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static void writeBytesToConnection(URLConnection connection, byte[] output) throws IOException {
        try(OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write(output);
        }
    }

    private static JsonObject readInputStream(InputStream stream) throws IOException {
        try(Reader input = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return (JsonObject) JsonParser.parseReader(input);
        }
    }
}
