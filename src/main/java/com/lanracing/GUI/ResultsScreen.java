package com.lanracing.GUI;

import com.lanracing.Game.Game;
import com.lanracing.Utility.Player;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class ResultsScreen extends JScrollPane {
    public ResultsScreen(Game game) {
        List<Player> players = game.getGameState().sortedLeaderboard(game.getRaceManager());
        DefaultTableModel model = new DefaultTableModel(new Object[] {"Position", "Player", "Laps", "Time (ms)"}, 0);
        int pos = 1;
        for (Player p : players) {
            model.addRow(new Object[] {
                    pos++,
                    p.getName(),
                    game.getRaceManager().getLapsCompleted(p.getId()),
                    p.getFinishTimeMillis() == 0 ? "DNF" : p.getFinishTimeMillis()
            });
        }

        JTable table = new JTable(model);
        setViewportView(table);
    }
}
