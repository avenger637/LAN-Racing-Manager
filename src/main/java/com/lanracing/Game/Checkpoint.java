package com.lanracing.Game;

import com.lanracing.Utility.Vector2D;

public class Checkpoint {
    private final int orderIndex;
    private final Vector2D center;
    private final double radius;

    public Checkpoint(int orderIndex, Vector2D center, double radius) {
        this.orderIndex = orderIndex;
        this.center = center;
        this.radius = radius;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public boolean contains(Vector2D point) {
        return center.subtract(point).length() <= radius;
    }

    public Vector2D getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }
}
