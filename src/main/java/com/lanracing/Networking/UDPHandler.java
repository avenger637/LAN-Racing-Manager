package com.lanracing.Networking;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Utility.Constants;
import com.lanracing.Utility.InputState;
import com.lanracing.Utility.Vector2D;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UDPHandler implements Runnable {
    private final DatagramSocket socket;
    private final Game game;
    private final Set<ClientAddress> subscribers = ConcurrentHashMap.newKeySet();
    private volatile boolean running = true;

    public UDPHandler(DatagramSocket socket, Game game) {
        this.socket = socket;
        this.game = game;
    }

    @Override
    public void run() {
        long lastBroadcastAt = System.currentTimeMillis();
        int tickIntervalMs = 1000 / Constants.SERVER_TICK_RATE;
        byte[] buf = new byte[512];
        try {
            socket.setSoTimeout(20);
        } catch (Exception ignored) {
        }
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                subscribers.add(new ClientAddress(packet.getAddress(), packet.getPort()));

                String payload = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                handleInputPacket(payload);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException ignored) {
                running = false;
            }

            long now = System.currentTimeMillis();
            if (now - lastBroadcastAt >= tickIntervalMs) {
                broadcastAuthoritativeState();
                lastBroadcastAt = now;
            }
        }
    }

    private void handleInputPacket(String payload) {
        try {
            String[] parts = payload.split("\\|");
            if (parts.length < 9 || !"INPUT".equals(parts[0])) {
                return;
            }

            String playerId = parts[1];
            boolean accelerate = Boolean.parseBoolean(parts[2]);
            boolean brake = Boolean.parseBoolean(parts[3]);
            boolean turnLeft = Boolean.parseBoolean(parts[4]);
            boolean turnRight = Boolean.parseBoolean(parts[5]);
            boolean nitro = Boolean.parseBoolean(parts[6]);
            boolean reset = Boolean.parseBoolean(parts[7]);
            int sequence = Integer.parseInt(parts[8]);

            InputState inputState = game.getInputState(playerId);
            Car car = game.getGameState().getCarsByPlayerId().get(playerId);
            if (inputState != null && car != null && sequence >= car.getLastInputSequence()) {
                inputState.accelerate = accelerate;
                inputState.brake = brake;
                inputState.turnLeft = turnLeft;
                inputState.turnRight = turnRight;
                inputState.nitro = nitro;
                inputState.reset = reset;
                car.setLastInputSequence(sequence);
            }
        } catch (RuntimeException ignored) {
        }
    }

    private void broadcastAuthoritativeState() {
        if (subscribers.isEmpty()) {
            return;
        }

        for (Car car : game.getGameState().getCarsByPlayerId().values()) {
            Vector2D position = car.getPosition();
            Vector2D velocity = car.getVelocity();
            String update = "STATE|"
                    + car.getPlayerId() + "|"
                    + position.x + "|"
                    + position.y + "|"
                    + car.getDirectionDeg() + "|"
                    + car.getLastInputSequence() + "|"
                    + velocity.x + "|"
                    + velocity.y;
            byte[] response = update.getBytes(StandardCharsets.UTF_8);

            for (ClientAddress address : subscribers) {
                try {
                    DatagramPacket outPacket = new DatagramPacket(response, response.length, address.address, address.port);
                    socket.send(outPacket);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public void stop() {
        running = false;
        socket.close();
    }

    private static final class ClientAddress {
        private final InetAddress address;
        private final int port;

        private ClientAddress(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ClientAddress)) {
                return false;
            }
            ClientAddress other = (ClientAddress) o;
            return port == other.port && address.equals(other.address);
        }

        @Override
        public int hashCode() {
            return address.hashCode() * 31 + port;
        }
    }
}
