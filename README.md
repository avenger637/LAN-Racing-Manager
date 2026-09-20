# LAN Racing Manager

LAN Racing Manager is a Java (JDK 11+) Swing-based real-time 2D multiplayer racing game prototype for LAN environments.

## Semester minimum scope (implemented baseline)

- 2–4 players (`Constants.MIN_PLAYERS=2`, `Constants.MAX_PLAYERS=4`)
- One racing track with checkpoints and 3 laps
- One vehicle type (`STANDARD`)
- WASD controls with continuous movement physics
- Basic collision handling
- LAN multiplayer
- Client-server architecture
- TCP communication for lobby/race control
- UDP movement updates with basic synchronization
- Graph-based track analysis support (BFS and Dijkstra)
- Queue and Priority Queue implementations
- Sorted leaderboard/result display

## Optional/extension features

- Nitro/shield/repair/teleport power-ups (disabled by default via `Constants.ENABLE_OPTIONAL_FEATURES`)
- Reset and nitro key paths in input handling
- Additional data structures/algorithms (DFS, stack, hash table)

## Build and test

```bash
mvn test
```

## Run

```bash
mvn exec:java
```

Use **Host Game** on one machine and **Join Game** from another machine on the same LAN (or localhost for local testing).
