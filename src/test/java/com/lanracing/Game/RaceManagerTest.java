package com.lanracing.Game;

import com.lanracing.Utility.Vector2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceManagerTest {

    @Test
    void shouldValidateCheckpointsBeforeLaps() {
        Track track = Track.defaultTrack();
        RaceManager manager = new RaceManager(1);
        Car car = new Car("p1", Car.VehicleType.BALANCED, track.defaultSpawn(), 0);

        manager.registerPlayer("p1");

        Checkpoint first = track.getCheckpoints().get(0);
        car.setPosition(new Vector2D(first.getCenter().x, first.getCenter().y));
        manager.processCarProgress("p1", track, car, 500);

        assertEquals(0, manager.getLapsCompleted("p1"));

        for (int i = 1; i < track.getCheckpoints().size(); i++) {
            Checkpoint cp = track.getCheckpoints().get(i);
            car.setPosition(new Vector2D(cp.getCenter().x, cp.getCenter().y));
            manager.processCarProgress("p1", track, car, 500 + i);
        }

        car.setPosition(new Vector2D(first.getCenter().x, first.getCenter().y));
        manager.processCarProgress("p1", track, car, 1000);

        assertEquals(1, manager.getLapsCompleted("p1"));
        assertTrue(manager.isFinished("p1"));
    }
}
