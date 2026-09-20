package com.lanracing.DSA;

import com.lanracing.Utility.Vector2D;

public class GraphNode {
    private final String id;
    private final Vector2D position;

    public GraphNode(String id, Vector2D position) {
        this.id = id;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public Vector2D getPosition() {
        return position;
    }
}
