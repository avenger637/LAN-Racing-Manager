package com.lanracing.DSA;

public class GraphEdge {
    private final GraphNode from;
    private final GraphNode to;
    private final double weight;

    public GraphEdge(GraphNode from, GraphNode to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public GraphNode getFrom() {
        return from;
    }

    public GraphNode getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }
}
