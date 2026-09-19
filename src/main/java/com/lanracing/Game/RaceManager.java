package com.lanracing.Game;

import com.lanracing.DSA.CustomQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RaceManager {
    private final int totalLaps;
    private final Map<String, Integer> lapsCompleted = new HashMap<>();
    private final Map<String, Integer> nextCheckpointIndex = new HashMap<>();
    private final Map<String, Long> finishTimes = new HashMap<>();
    private final CustomQueue<String> eventQueue = new CustomQueue<>();

    public RaceManager(int totalLaps) {
        this.totalLaps = totalLaps;
    }

    public synchronized void registerPlayer(String playerId) {
        lapsCompleted.put(playerId, 0);
        nextCheckpointIndex.put(playerId, 0);
    }

    public synchronized void processCarProgress(String playerId, Track track, Car car, long raceElapsedMillis) {
        if (!lapsCompleted.containsKey(playerId)) {
            return;
        }
        if (isFinished(playerId)) {
            return;
        }

        int expected = nextCheckpointIndex.get(playerId);
        Checkpoint checkpoint = track.getCheckpoints().get(expected);
        if (checkpoint.contains(car.getPosition())) {
            int next = (expected + 1) % track.getCheckpoints().size();
            nextCheckpointIndex.put(playerId, next);
            eventQueue.enqueue(playerId + " reached checkpoint " + checkpoint.getOrderIndex());
            if (next == 0) {
                int laps = lapsCompleted.get(playerId) + 1;
                lapsCompleted.put(playerId, laps);
                eventQueue.enqueue(playerId + " completed lap " + laps);
                if (laps >= totalLaps) {
                    finishTimes.put(playerId, raceElapsedMillis);
                    eventQueue.enqueue(playerId + " finished race");
                }
            }
        }
    }

    public synchronized String pollNextEvent() {
        return eventQueue.dequeue();
    }

    public synchronized int getLapsCompleted(String playerId) {
        return lapsCompleted.getOrDefault(playerId, 0);
    }

    public synchronized boolean isFinished(String playerId) {
        return finishTimes.containsKey(playerId);
    }

    public synchronized long getFinishTime(String playerId) {
        return finishTimes.getOrDefault(playerId, Long.MAX_VALUE);
    }

    public int getTotalLaps() {
        return totalLaps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RaceManager)) return false;
        RaceManager that = (RaceManager) o;
        return totalLaps == that.totalLaps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalLaps);
    }
}
