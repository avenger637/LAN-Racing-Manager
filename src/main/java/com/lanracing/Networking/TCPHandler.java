package com.lanracing.Networking;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Utility.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

public class TCPHandler implements Runnable {
    private final Socket socket;
    private final Game game;
    private final Map<String, PrintWriter> clientOutputs;
    private String playerId;

    public TCPHandler(Socket socket, Game game, Map<String, PrintWriter> clientOutputs) {
        this.socket = socket;
        this.game = game;
        this.clientOutputs = clientOutputs;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            playerId = UUID.randomUUID().toString();
            clientOutputs.put(playerId, out);

            String joinName = "Player-" + playerId.substring(0, 4);
            game.addPlayer(new Player(playerId, joinName), Car.VehicleType.STANDARD);

            out.println("WELCOME|playerId=" + playerId + "|name=" + joinName);
            sendLobbySnapshot(out);
            broadcast("PLAYER_JOINED|playerId=" + playerId + "|name=" + joinName + "|ready=false");

            String line;
            while ((line = in.readLine()) != null) {
                MessageHandler.ParsedMessage msg = MessageHandler.parse(line);
                handleMessage(msg, out);
            }
        } catch (IOException ignored) {
        } finally {
            if (playerId != null) {
                clientOutputs.remove(playerId);
                game.removePlayer(playerId);
                broadcast("PLAYER_LEFT|playerId=" + playerId);
            }
        }
    }

    private void handleMessage(MessageHandler.ParsedMessage msg, PrintWriter out) {
        switch (msg.getType()) {
            case "READY":
                Player player = game.getGameState().getPlayersById().get(playerId);
                if (player != null) {
                    boolean ready = !"false".equalsIgnoreCase(msg.getPayload().getOrDefault("value", "true"));
                    player.setReady(ready);
                    broadcast("PLAYER_READY_STATE|playerId=" + playerId + "|ready=" + ready);
                }
                break;
            case "START_RACE":
                if (game.startRaceIfReady()) {
                    broadcast("RACE_STARTED|time=" + game.getRaceStartMillis());
                } else {
                    out.println("START_DENIED|reason=NOT_READY_OR_MIN_PLAYERS");
                }
                break;
            case "PING":
                out.println("PONG");
                break;
            default:
                break;
        }
    }

    private void sendLobbySnapshot(PrintWriter out) {
        for (Player p : game.getGameState().getPlayersById().values()) {
            out.println("PLAYER_JOINED|playerId=" + p.getId() + "|name=" + p.getName() + "|ready=" + p.isReady());
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientOutputs.values()) {
            writer.println(message);
        }
    }
}
