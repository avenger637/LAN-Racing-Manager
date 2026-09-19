package com.lanracing.Game;

import com.lanracing.Utility.Constants;
import com.lanracing.Utility.GameState;
import com.lanracing.Utility.InputState;
import com.lanracing.Utility.Player;
import com.lanracing.Utility.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game {
    private final GameState gameState = new GameState();
    private final Track track = Track.defaultTrack();
    private final RaceManager raceManager = new RaceManager(Constants.DEFAULT_TOTAL_LAPS);
    private final Map<String, InputState> inputStates = new ConcurrentHashMap<>();
    private final List<PowerUp> powerUps = new ArrayList<>();

    private volatile long raceStartMillis;

    public Game() {
        powerUps.add(new PowerUp(PowerUp.Type.NITRO, new Vector2D(520, 165)));
        powerUps.add(new PowerUp(PowerUp.Type.SHIELD, new Vector2D(875, 330)));
        powerUps.add(new PowerUp(PowerUp.Type.REPAIR, new Vector2D(520, 510)));
        powerUps.add(new PowerUp(PowerUp.Type.TELEPORT, new Vector2D(200, 330)));
    }

    public void addPlayer(Player player, Car.VehicleType vehicleType) {
        gameState.getPlayersById().put(player.getId(), player);
        Car car = new Car(player.getId(), vehicleType, track.defaultSpawn(), 0);
        gameState.getCarsByPlayerId().put(player.getId(), car);
        inputStates.put(player.getId(), new InputState());
        raceManager.registerPlayer(player.getId());
    }

    public void removePlayer(String playerId) {
        gameState.getPlayersById().remove(playerId);
        gameState.getCarsByPlayerId().remove(playerId);
        inputStates.remove(playerId);
    }

    public void update(double dtSeconds) {
        if (!gameState.isRaceStarted()) {
            return;
        }

        long elapsed = System.currentTimeMillis() - raceStartMillis;

        for (Map.Entry<String, Car> entry : gameState.getCarsByPlayerId().entrySet()) {
            String playerId = entry.getKey();
            Car car = entry.getValue();
            InputState input = inputStates.get(playerId);

            if (input != null && input.reset) {
                car.reset(track.defaultSpawn());
                input.reset = false;
            }

            car.applyInput(input, dtSeconds);
            CollisionDetection.enforceTrackBounds(car, track);

            for (PowerUp powerUp : powerUps) {
                if (powerUp.canCollect(car)) {
                    powerUp.applyTo(car, track);
                }
            }

            raceManager.processCarProgress(playerId, track, car, elapsed);
            if (raceManager.isFinished(playerId)) {
                gameState.getPlayersById().get(playerId).setFinishTimeMillis(raceManager.getFinishTime(playerId));
            }
        }
    }

    public void startRace() {
        gameState.setRaceStarted(true);
        raceStartMillis = System.currentTimeMillis();
    }

    public boolean canStartRace() {
        int playerCount = gameState.getPlayersById().size();
        if (playerCount < Constants.MIN_PLAYERS || playerCount > Constants.MAX_PLAYERS) {
            return false;
        }
        for (Player player : gameState.getPlayersById().values()) {
            if (!player.isReady()) {
                return false;
            }
        }
        return true;
    }

    public boolean startRaceIfReady() {
        if (!canStartRace()) {
            return false;
        }
        startRace();
        return true;
    }

    public void stopRace() {
        gameState.setRaceStarted(false);
    }

    public InputState getInputState(String playerId) {
        return inputStates.get(playerId);
    }

    public GameState getGameState() {
        return gameState;
    }

    public Track getTrack() {
        return track;
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public List<PowerUp> getPowerUps() {
        return powerUps;
    }

    public long getRaceStartMillis() {
        return raceStartMillis;
    }
}
