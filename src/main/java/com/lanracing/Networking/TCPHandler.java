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
            game.addPlayer(new Player(playerId, joinName), Car.VehicleType.BALANCED);

            out.println("WELCOME|playerId=" + playerId);
            broadcast("PLAYER_JOINED|playerId=" + playerId + "|name=" + joinName);

            String line;
            while ((line = in.readLine()) != null) {
                MessageHandler.ParsedMessage msg = MessageHandler.parse(line);
                handleMessage(msg, out);
            }
        } catch (IOException ignored) {
        } finally {
            if (playerId != null) {
                clientOutputs.remove(playerId);
                game.getGameState().getPlayersById().remove(playerId);
                game.getGameState().getCarsByPlayerId().remove(playerId);
                broadcast("PLAYER_LEFT|playerId=" + playerId);
            }
        }
    }

    private void handleMessage(MessageHandler.ParsedMessage msg, PrintWriter out) {
        switch (msg.getType()) {
            case "READY":
                Player player = game.getGameState().getPlayersById().get(playerId);
                if (player != null) {
                    player.setReady(true);
                    broadcast("PLAYER_READY|playerId=" + playerId);
                }
                break;
            case "START_RACE":
                game.startRace();
                broadcast("RACE_STARTED|time=" + game.getRaceStartMillis());
                break;
            case "PING":
                out.println("PONG");
                break;
            default:
                break;
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientOutputs.values()) {
            writer.println(message);
        }
    }
}
