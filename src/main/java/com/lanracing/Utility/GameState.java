package com.lanracing.Utility;

import com.lanracing.Game.Car;
import com.lanracing.Game.RaceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private final Map<String, Player> playersById = new ConcurrentHashMap<>();
    private final Map<String, Car> carsByPlayerId = new ConcurrentHashMap<>();
    private volatile boolean raceStarted;

    public Map<String, Player> getPlayersById() {
        return playersById;
    }

    public Map<String, Car> getCarsByPlayerId() {
        return carsByPlayerId;
    }

    public boolean isRaceStarted() {
        return raceStarted;
    }

    public void setRaceStarted(boolean raceStarted) {
        this.raceStarted = raceStarted;
    }

    public List<Player> sortedLeaderboard(RaceManager raceManager) {
        List<Player> players = new ArrayList<>(playersById.values());
        players.sort(Comparator
                .comparingInt((Player p) -> raceManager.getLapsCompleted(p.getId())).reversed()
                .thenComparingLong(Player::getFinishTimeMillis));
        return players;
    }
}
