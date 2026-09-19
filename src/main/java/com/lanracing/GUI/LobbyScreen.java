package com.lanracing.GUI;

import com.lanracing.Game.Game;
import com.lanracing.Utility.Player;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;

public class LobbyScreen extends JPanel {
    private final Game game;
    private final String localPlayerId;
    private final Runnable onStart;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();

    public LobbyScreen(Game game, String localPlayerId, Runnable onStart) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        this.onStart = onStart;

        setLayout(new BorderLayout(10, 10));
        JList<String> playerList = new JList<>(listModel);

        JButton readyButton = new JButton("Ready");
        readyButton.addActionListener(e -> {
            Player p = game.getGameState().getPlayersById().get(localPlayerId);
            if (p != null) {
                p.setReady(true);
            }
        });

        JButton startButton = new JButton("Start Race");
        startButton.addActionListener(e -> {
            game.startRace();
            onStart.run();
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
    }
}
