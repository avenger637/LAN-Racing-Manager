package com.lanracing.Game;

import com.lanracing.Utility.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameReadinessTest {

    @Test
    void shouldNotStartWithLessThanMinimumPlayers() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.BALANCED);
        game.getGameState().getPlayersById().get("p1").setReady(true);

        assertFalse(game.canStartRace());
        assertFalse(game.startRaceIfReady());
    }

    @Test
    void shouldRequireAllPlayersToBeReady() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.BALANCED);
        game.addPlayer(new Player("p2", "P2"), Car.VehicleType.BALANCED);

        game.getGameState().getPlayersById().get("p1").setReady(true);
        game.getGameState().getPlayersById().get("p2").setReady(false);

        assertFalse(game.canStartRace());
        assertFalse(game.startRaceIfReady());
    }

    @Test
    void shouldStartWhenMinimumPlayersAreReady() {
        Game game = new Game();
        game.addPlayer(new Player("p1", "P1"), Car.VehicleType.BALANCED);
        game.addPlayer(new Player("p2", "P2"), Car.VehicleType.BALANCED);

        game.getGameState().getPlayersById().get("p1").setReady(true);
        game.getGameState().getPlayersById().get("p2").setReady(true);

        assertTrue(game.canStartRace());
        assertTrue(game.startRaceIfReady());
        assertTrue(game.getGameState().isRaceStarted());
    }
}
