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

public class GameClient {
    private final String host;
    private final Game game;

    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;

    private DatagramSocket udpSocket;

    private volatile String localPlayerId;
    private volatile int inputSequence;
    private final Deque<PredictedState> predictionBuffer = new ArrayDeque<>();

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
        tcpOut.println("READY");
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
        game.getInputState(localPlayerId).accelerate = inputState.accelerate;
        game.getInputState(localPlayerId).brake = inputState.brake;
        game.getInputState(localPlayerId).turnLeft = inputState.turnLeft;
        game.getInputState(localPlayerId).turnRight = inputState.turnRight;
        game.getInputState(localPlayerId).nitro = inputState.nitro;

        car.applyInput(inputState, 1.0 / Constants.CLIENT_UDP_SEND_RATE);
        predictionBuffer.addLast(new PredictedState(inputSequence, car.getPosition(), car.getDirectionDeg()));
        if (predictionBuffer.size() > 50) {
            predictionBuffer.removeFirst();
        }

        String packet = "MOVE|" + localPlayerId + "|" + car.getPosition().x + "|" + car.getPosition().y + "|" + car.getDirectionDeg() + "|" + inputSequence;
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
                    game.addPlayer(new com.lanracing.Utility.Player(localPlayerId, "You"), Car.VehicleType.BALANCED);
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
                if (parts.length >= 6 && "STATE".equals(parts[0])) {
                    String playerId = parts[1];
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    double angle = Double.parseDouble(parts[4]);
                    int seq = Integer.parseInt(parts[5]);

                    Car car = game.getGameState().getCarsByPlayerId().get(playerId);
                    if (car != null) {
                        if (playerId.equals(localPlayerId)) {
                            reconcile(car, x, y, angle, seq);
                        } else {
                            car.setPosition(new Vector2D((car.getPosition().x + x) / 2.0, (car.getPosition().y + y) / 2.0));
                            car.setDirectionDeg((car.getDirectionDeg() + angle) / 2.0);
                        }
                    }
                }
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private void reconcile(Car car, double x, double y, double angle, int seq) {
        car.setPosition(new Vector2D(x, y));
        car.setDirectionDeg(angle);

        while (!predictionBuffer.isEmpty() && predictionBuffer.peekFirst().sequence <= seq) {
            predictionBuffer.removeFirst();
        }

        for (PredictedState predicted : predictionBuffer) {
            car.setPosition(predicted.position);
            car.setDirectionDeg(predicted.angle);
        }
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

    private static class PredictedState {
        private final int sequence;
        private final Vector2D position;
        private final double angle;

        private PredictedState(int sequence, Vector2D position, double angle) {
            this.sequence = sequence;
            this.position = new Vector2D(position.x, position.y);
            this.angle = angle;
        }
    }
}
