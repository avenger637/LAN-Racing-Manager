package com.lanracing.Networking;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Utility.Constants;
import com.lanracing.Utility.InputState;
import com.lanracing.Utility.Vector2D;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameClient {
    private final String host;
    private final Game game;

    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;

    private DatagramSocket udpSocket;

    private volatile String localPlayerId;
    private volatile int inputSequence;
    private final Deque<PendingInput> predictionBuffer = new ArrayDeque<>();
    private final Map<String, RemoteState> remoteTargets = new ConcurrentHashMap<>();

    public GameClient(String host, Game game) {
        this.host = host;
        this.game = game;
    }

    public void connect() throws IOException {
        tcpSocket = new Socket(host, Constants.TCP_PORT);
        tcpOut = new PrintWriter(tcpSocket.getOutputStream(), true);
        tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));

        udpSocket = new DatagramSocket();

        Thread tcpListen = new Thread(this::listenTcp, "tcp-client-listener");
        tcpListen.setDaemon(true);
        tcpListen.start();

        Thread udpListen = new Thread(this::listenUdp, "udp-client-listener");
        udpListen.setDaemon(true);
        udpListen.start();
    }

    public void sendReady() {
        sendReady(true);
    }

    public void sendReady(boolean ready) {
        tcpOut.println("READY|value=" + ready);
    }

    public void sendStartRace() {
        tcpOut.println("START_RACE");
    }

    public void sendMovement(InputState inputState) {
        if (localPlayerId == null) {
            return;
        }
        Car car = game.getGameState().getCarsByPlayerId().get(localPlayerId);
        if (car == null) {
            return;
        }

        inputSequence++;
        InputState localInput = game.getInputState(localPlayerId);
        if (localInput == null) {
            return;
        }
        localInput.accelerate = inputState.accelerate;
        localInput.brake = inputState.brake;
        localInput.turnLeft = inputState.turnLeft;
        localInput.turnRight = inputState.turnRight;
        localInput.nitro = inputState.nitro;
        localInput.reset = inputState.reset;

        car.applyInput(inputState, 1.0 / Constants.CLIENT_UDP_SEND_RATE);
        predictionBuffer.addLast(new PendingInput(inputSequence, copyInput(inputState)));
        if (predictionBuffer.size() > 50) {
            predictionBuffer.removeFirst();
        }

        String packet = "INPUT|"
                + localPlayerId + "|"
                + inputState.accelerate + "|"
                + inputState.brake + "|"
                + inputState.turnLeft + "|"
                + inputState.turnRight + "|"
                + inputState.nitro + "|"
                + inputState.reset + "|"
                + inputSequence;
        byte[] data = packet.getBytes(StandardCharsets.UTF_8);
        try {
            udpSocket.send(new DatagramPacket(data, data.length, InetAddress.getByName(host), Constants.UDP_PORT));
        } catch (IOException ignored) {
        }
    }

    private void listenTcp() {
        try {
            String line;
            while ((line = tcpIn.readLine()) != null) {
                MessageHandler.ParsedMessage msg = MessageHandler.parse(line);
                if ("WELCOME".equals(msg.getType())) {
                    localPlayerId = msg.getPayload().get("playerId");
                    ensurePlayerExists(localPlayerId, msg.getPayload().getOrDefault("name", "You"));
                } else if ("PLAYER_JOINED".equals(msg.getType())) {
                    String playerId = msg.getPayload().get("playerId");
                    String name = msg.getPayload().getOrDefault("name", "Player");
                    boolean ready = Boolean.parseBoolean(msg.getPayload().getOrDefault("ready", "false"));
                    ensurePlayerExists(playerId, name);
                    com.lanracing.Utility.Player player = game.getGameState().getPlayersById().get(playerId);
                    if (player != null) {
                        player.setReady(ready);
                    }
                } else if ("PLAYER_LEFT".equals(msg.getType())) {
                    String playerId = msg.getPayload().get("playerId");
                    if (playerId != null) {
                        game.removePlayer(playerId);
                    }
                } else if ("PLAYER_READY_STATE".equals(msg.getType())) {
                    String playerId = msg.getPayload().get("playerId");
                    boolean ready = Boolean.parseBoolean(msg.getPayload().getOrDefault("ready", "false"));
                    com.lanracing.Utility.Player player = game.getGameState().getPlayersById().get(playerId);
                    if (player != null) {
                        player.setReady(ready);
                    }
                } else if ("RACE_STARTED".equals(msg.getType())) {
                    game.startRace();
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void listenUdp() {
        byte[] buffer = new byte[512];
        while (!udpSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                String payload = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                String[] parts = payload.split("\\|");
                if (parts.length >= 8 && "STATE".equals(parts[0])) {
                    String playerId = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    double angle = Double.parseDouble(parts[4]);
                    int seq = Integer.parseInt(parts[5]);
                    double vx = Double.parseDouble(parts[6]);
                    double vy = Double.parseDouble(parts[7]);

                    Car car = game.getGameState().getCarsByPlayerId().get(playerId);
                    if (car != null) {
                        if (playerId.equals(localPlayerId)) {
                            reconcile(car, x, y, angle, seq, vx, vy);
                        } else {
                            remoteTargets.put(playerId, new RemoteState(x, y, angle, vx, vy));
                            interpolateRemote(car, remoteTargets.get(playerId));
                        }
                    }
                }
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private void reconcile(Car car, double x, double y, double angle, int seq, double vx, double vy) {
        car.setPosition(new Vector2D(x, y));
        car.setVelocity(new Vector2D(vx, vy));
        car.setDirectionDeg(angle);

        while (!predictionBuffer.isEmpty() && predictionBuffer.peekFirst().sequence <= seq) {
            predictionBuffer.removeFirst();
        }

        for (PendingInput pending : predictionBuffer) {
            car.applyInput(pending.inputState, 1.0 / Constants.CLIENT_UDP_SEND_RATE);
        }
    }

    private void interpolateRemote(Car car, RemoteState target) {
        double factor = 0.35;
        Vector2D current = car.getPosition();
        car.setPosition(new Vector2D(
                current.x + (target.x - current.x) * factor,
                current.y + (target.y - current.y) * factor
        ));
        car.setVelocity(new Vector2D(target.vx, target.vy));
        car.setDirectionDeg(interpolateAngle(car.getDirectionDeg(), target.angle, factor));
    }

    private double interpolateAngle(double current, double target, double factor) {
        double delta = ((target - current + 540) % 360) - 180;
        return current + (delta * factor);
    }

    private InputState copyInput(InputState input) {
        InputState copy = new InputState();
        copy.accelerate = input.accelerate;
        copy.brake = input.brake;
        copy.turnLeft = input.turnLeft;
        copy.turnRight = input.turnRight;
        copy.nitro = input.nitro;
        copy.reset = input.reset;
        return copy;
    }

    private void ensurePlayerExists(String playerId, String name) {
        if (playerId == null || game.getGameState().getPlayersById().containsKey(playerId)) {
            return;
        }
        game.addPlayer(new com.lanracing.Utility.Player(playerId, name), Car.VehicleType.BALANCED);
    }

    public void disconnect() {
        try {
            if (tcpSocket != null) {
                tcpSocket.close();
            }
        } catch (IOException ignored) {
        }
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }

    public String getLocalPlayerId() {
        return localPlayerId;
    }

    private static class PendingInput {
        private final int sequence;
        private final InputState inputState;

        private PendingInput(int sequence, InputState inputState) {
            this.sequence = sequence;
            this.inputState = inputState;
        }
    }

    private static class RemoteState {
        private final double x;
        private final double y;
        private final double angle;
        private final double vx;
        private final double vy;

        private RemoteState(double x, double y, double angle, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.vx = vx;
            this.vy = vy;
        }
    }
}
