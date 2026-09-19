package com.lanracing.Networking;

import com.lanracing.Game.Game;
import com.lanracing.Utility.Constants;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private final Game game;
    private final Map<String, PrintWriter> clientOutputs = new ConcurrentHashMap<>();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    private volatile boolean running;
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;

    public GameServer(Game game) {
        this.game = game;
    }

    public void start() throws IOException {
        running = true;
        tcpServerSocket = new ServerSocket(Constants.TCP_PORT);
        udpSocket = new DatagramSocket(Constants.UDP_PORT);
        Thread udpThread = new Thread(new UDPHandler(udpSocket, game), "udp-server-thread");
        udpThread.setDaemon(true);
        udpThread.start();

        Thread simulation = new Thread(() -> {
            while (running) {
                game.update(1.0 / Constants.SERVER_TICK_RATE);
                try {
                    Thread.sleep(1000 / Constants.SERVER_TICK_RATE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "server-simulation-thread");
        simulation.setDaemon(true);
        simulation.start();

        while (running) {
            Socket clientSocket = tcpServerSocket.accept();
            clientPool.submit(new TCPHandler(clientSocket, game, clientOutputs));
        }
    }

    public void stop() {
        running = false;
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        if (tcpServerSocket != null && !tcpServerSocket.isClosed()) {
            try {
                tcpServerSocket.close();
            } catch (IOException ignored) {
            }
        }
        clientPool.shutdownNow();
    }
}
