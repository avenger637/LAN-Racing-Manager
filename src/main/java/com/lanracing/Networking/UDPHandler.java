package com.lanracing.Networking;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Utility.Vector2D;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
        byte[] buf = new byte[512];
        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                subscribers.add(new ClientAddress(packet.getAddress(), packet.getPort()));

                String payload = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                handleMovementPacket(payload);
            } catch (IOException ignored) {
                running = false;
            }
        }
    }

    private void handleMovementPacket(String payload) throws IOException {
        String[] parts = payload.split("\\|");
        if (parts.length < 6 || !"MOVE".equals(parts[0])) {
            return;
        }

        String playerId = parts[1];
        double x = Double.parseDouble(parts[2]);
        double y = Double.parseDouble(parts[3]);
        double angle = Double.parseDouble(parts[4]);
        int sequence = Integer.parseInt(parts[5]);

        Car car = game.getGameState().getCarsByPlayerId().get(playerId);
        if (car != null && sequence >= car.getLastInputSequence()) {
            car.setPosition(new Vector2D(x, y));
            car.setDirectionDeg(angle);
            car.setLastInputSequence(sequence);
        }

        String update = "STATE|" + playerId + "|" + x + "|" + y + "|" + angle + "|" + sequence;
        byte[] response = update.getBytes(StandardCharsets.UTF_8);

        for (ClientAddress address : subscribers) {
            DatagramPacket outPacket = new DatagramPacket(response, response.length, address.address, address.port);
            socket.send(outPacket);
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
