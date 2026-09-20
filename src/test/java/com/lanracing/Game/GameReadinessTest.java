package com.lanracing.Game;

import com.lanracing.Utility.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameReadinessTest {

    @Test
    void shouldNotStartWithLessThanMinimumPlayers() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.STANDARD);
        game.getGameState().getPlayersById().get("p1").setReady(true);

        assertFalse(game.canStartRace());
        assertFalse(game.startRaceIfReady());
    }

    @Test
    void shouldRequireAllPlayersToBeReady() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p2", "P2"), Car.VehicleType.STANDARD);

        game.getGameState().getPlayersById().get("p1").setReady(true);
        game.getGameState().getPlayersById().get("p2").setReady(false);

        assertFalse(game.canStartRace());
        assertFalse(game.startRaceIfReady());
    }

    @Test
    void shouldStartWhenMinimumPlayersAreReady() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p2", "P2"), Car.VehicleType.STANDARD);

        game.getGameState().getPlayersById().get("p1").setReady(true);
        game.getGameState().getPlayersById().get("p2").setReady(true);

        assertTrue(game.canStartRace());
        assertTrue(game.startRaceIfReady());
        assertTrue(game.getGameState().isRaceStarted());
    }

    @Test
    void shouldNotStartWhenPlayerCountExceedsMaximum() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p2", "P2"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p3", "P3"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p4", "P4"), Car.VehicleType.STANDARD);
        game.addPlayer(new Player("p5", "P5"), Car.VehicleType.STANDARD);

        game.getGameState().getPlayersById().values().forEach(player -> player.setReady(true));

        assertFalse(game.canStartRace());
        assertFalse(game.startRaceIfReady());
    }

    @Test
    void shouldKeepOptionalPowerUpsDisabledByDefault() {
        Game game = new Game();
        assertTrue(game.getPowerUps().isEmpty());
    }
}
