package com.lanracing.GUI;

import com.lanracing.Game.Game;
import com.lanracing.Networking.GameClient;
import com.lanracing.Utility.Player;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;

public class LobbyScreen extends JPanel {
    private final Game game;
    private final GameClient client;
    private final String offlinePlayerId;
    private final boolean hostControls;
    private final Runnable onStart;
    private boolean transitioned;
    private JButton startButton;
    private JButton readyButton;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    public LobbyScreen(Game game, GameClient client, String offlinePlayerId, boolean hostControls, Runnable onStart) {
        this.game = game;
        this.client = client;
        this.offlinePlayerId = offlinePlayerId;
        this.hostControls = hostControls;
        this.onStart = onStart;

        setLayout(new BorderLayout(10, 10));
        JList<String> playerList = new JList<>(listModel);

        readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            String localPlayerId = resolveLocalPlayerId();
            if (localPlayerId == null) {
                return;
            }
            Player p = game.getGameState().getPlayersById().get(localPlayerId);
            if (p != null) {
                p.setReady(true);
                if (client != null) {
                    client.sendReady(true);
                }
            }
        });

        startButton = new JButton("Start Race");
        startButton.addActionListener(e -> {
            if (client != null) {
                client.sendStartRace();
                return;
            }

            game.startRace();
            transitionToRace();
        });

        add(playerList, BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.add(readyButton);
        bottom.add(startButton);
        add(bottom, BorderLayout.SOUTH);

        Timer refresh = new Timer(400, e -> refreshPlayers());
        refresh.start();
    }

    private void refreshPlayers() {
        listModel.clear();
        for (Player player : game.getGameState().getPlayersById().values()) {
            listModel.addElement(player.getName() + (player.isReady() ? " [READY]" : " [NOT READY]"));
        }
        startButton.setEnabled(hostControls && (client == null || game.canStartRace()));
        readyButton.setEnabled(resolveLocalPlayerId() != null);

        if (game.getGameState().isRaceStarted()) {
            transitionToRace();
        }
    }

    private String resolveLocalPlayerId() {
        if (client != null) {
            return client.getLocalPlayerId();
        }
        return offlinePlayerId;
    }

    private void transitionToRace() {
        if (transitioned) {
            return;
        }
        transitioned = true;
        onStart.run();
    }
}
