package com.lanracing.DSA;

import com.lanracing.Utility.Vector2D;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphTest {

    @Test
    void bfsDfsAndDijkstraShouldWork() {
        Graph graph = new Graph();
        graph.addNode(new GraphNode("A", new Vector2D(0, 0)));
        graph.addNode(new GraphNode("B", new Vector2D(1, 0)));
        graph.addNode(new GraphNode("C", new Vector2D(2, 0)));
        graph.addNode(new GraphNode("D", new Vector2D(3, 0)));

        graph.addUndirectedEdge("A", "B", 2);
        graph.addUndirectedEdge("B", "C", 3);
        graph.addUndirectedEdge("A", "D", 10);
        graph.addUndirectedEdge("C", "D", 1);

        List<String> bfs = graph.bfs("A");
        List<String> dfs = graph.dfs("A");
        Map<String, Double> distance = graph.dijkstra("A");

        assertEquals("A", bfs.get(0));
        assertTrue(dfs.contains("D"));
        assertEquals(6.0, distance.get("D"));
    }
}
