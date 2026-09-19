# LAN Racing Manager

LAN Racing Manager is a Java (JDK 11+) Swing-based real-time 2D multiplayer racing game prototype for LAN environments.

## Implemented modules

- Real-time car control (W/A/S/D, Space nitro, R reset)
- Basic physics (speed, acceleration, direction, friction)
- Track + checkpoints + lap validation + race result sorting
- Vehicle types: Speedster, Muscle, Balanced
- Power-ups: nitro, shield, repair, teleport
- DSA package: graph (BFS/DFS/Dijkstra), queue, stack, priority queue, hash table
- Networking package: TCP lobby/race control + ready-gated race start + UDP input streaming with server-authoritative state snapshots
- Swing GUI: Main menu, lobby, game panel, HUD, results table

## Build and test

```bash
mvn test
```

## Run

```bash
mvn exec:java
```

Use **Host Game** on one machine and **Join Game** from another machine on the same LAN (or localhost for local testing).
