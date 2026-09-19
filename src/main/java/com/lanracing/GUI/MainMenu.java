package com.lanracing.GUI;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Networking.GameClient;
import com.lanracing.Networking.GameServer;
import com.lanracing.Utility.Constants;
import com.lanracing.Utility.Player;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

public class MainMenu extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final Game game = new Game();

    private GameServer server;
    private GameClient client;

    public MainMenu() {
        setTitle("LAN Racing Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));

        JPanel menuPanel = new JPanel(new GridLayout(4, 1, 8, 8));

        JButton hostButton = new JButton("Host Game");
        hostButton.addActionListener(e -> hostGame());

        JButton joinButton = new JButton("Join Game");
        joinButton.addActionListener(e -> joinGame());

        JButton offlineButton = new JButton("Play Offline");
        offlineButton.addActionListener(e -> startOffline());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> dispose());

        menuPanel.add(hostButton);
        menuPanel.add(joinButton);
        menuPanel.add(offlineButton);
        menuPanel.add(exitButton);

        root.add(menuPanel, "menu");
        add(root);

        pack();
        setLocationRelativeTo(null);
    }

    private void hostGame() {
        try {
            server = new GameServer(game);
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception ignored) {
                }
            }, "tcp-server-main-thread");
            serverThread.setDaemon(true);
            serverThread.start();

            joinGameByHost("127.0.0.1");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to host game: " + ex.getMessage());
        }
    }

    private void joinGame() {
        String host = JOptionPane.showInputDialog(this, "Enter host IP:", "127.0.0.1");
        if (host == null || host.isBlank()) {
            return;
        }
        joinGameByHost(host.trim());
    }

    private void joinGameByHost(String host) {
        try {
            client = new GameClient(host, game);
            client.connect();
            SwingUtilities.invokeLater(() -> showLobby("pending"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to join game: " + ex.getMessage());
        }
    }

    private void startOffline() {
        String localId = "local-player";
        game.addPlayer(new Player(localId, "Player 1"), Car.VehicleType.BALANCED);
        showLobby(localId);
    }

    private void showLobby(String localPlayerId) {
        LobbyScreen lobby = new LobbyScreen(game, localPlayerId, () -> showGame(localPlayerId));
        root.add(lobby, "lobby");
        cardLayout.show(root, "lobby");
        revalidate();
        repaint();
    }

    private void showGame(String localPlayerId) {
        GamePanel gamePanel = new GamePanel(game, localPlayerId);
        root.add(gamePanel, "game");
        cardLayout.show(root, "game");
        gamePanel.requestFocusInWindow();
        revalidate();
        repaint();
    }
}
