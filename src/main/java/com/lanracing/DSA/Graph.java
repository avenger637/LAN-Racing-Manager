package com.lanracing.DSA;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Graph {
    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, List<GraphEdge>> adjacency = new HashMap<>();

    public void addNode(GraphNode node) {
        nodes.put(node.getId(), node);
        adjacency.computeIfAbsent(node.getId(), k -> new ArrayList<>());
    }

    public void addUndirectedEdge(String fromId, String toId, double weight) {
        GraphNode from = nodes.get(fromId);
        GraphNode to = nodes.get(toId);
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both nodes must exist before adding an edge");
        }
        adjacency.get(fromId).add(new GraphEdge(from, to, weight));
        adjacency.get(toId).add(new GraphEdge(to, from, weight));
    }

    public List<String> bfs(String startId) {
        if (!nodes.containsKey(startId)) {
            return Collections.emptyList();
        }
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.offer(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            order.add(current);
            for (GraphEdge edge : adjacency.getOrDefault(current, Collections.emptyList())) {
                String next = edge.getTo().getId();
                if (visited.add(next)) {
                    queue.offer(next);
                }
            }
        }
        return order;
    }

    public List<String> dfs(String startId) {
        if (!nodes.containsKey(startId)) {
            return Collections.emptyList();
        }
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        dfsRec(startId, visited, order);
        return order;
    }

    private void dfsRec(String current, Set<String> visited, List<String> order) {
        visited.add(current);
        order.add(current);
        for (GraphEdge edge : adjacency.getOrDefault(current, Collections.emptyList())) {
            String next = edge.getTo().getId();
            if (!visited.contains(next)) {
                dfsRec(next, visited, order);
            }
        }
    }

    public Map<String, Double> dijkstra(String startId) {
        if (!nodes.containsKey(startId)) {
            return Collections.emptyMap();
        }
        Map<String, Double> distance = new HashMap<>();
        for (String nodeId : nodes.keySet()) {
            distance.put(nodeId, Double.POSITIVE_INFINITY);
        }
        distance.put(startId, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>((a, b) -> Double.compare(distance.get(a), distance.get(b)));
        pq.offer(startId);

        while (!pq.isEmpty()) {
            String current = pq.poll();
            double currentDist = distance.get(current);
            for (GraphEdge edge : adjacency.getOrDefault(current, Collections.emptyList())) {
                String next = edge.getTo().getId();
                double newDist = currentDist + edge.getWeight();
                if (newDist < distance.get(next)) {
                    distance.put(next, newDist);
                    pq.offer(next);
                }
            }
        }
        return distance;
    }
}
