package com.lanracing.Game;

import com.lanracing.Utility.Vector2D;

public final class CollisionDetection {
    private CollisionDetection() {}

    public static void enforceTrackBounds(Car car, Track track) {
        Vector2D p = car.getPosition();
        if (track.isDriveable(p)) {
            return;
        }

        double minX = track.getOuterBounds().x;
        double maxX = track.getOuterBounds().x + track.getOuterBounds().width;
        double minY = track.getOuterBounds().y;
        double maxY = track.getOuterBounds().y + track.getOuterBounds().height;

        double clampedX = Math.max(minX + 10, Math.min(maxX - 10, p.x));
        double clampedY = Math.max(minY + 10, Math.min(maxY - 10, p.y));

        Vector2D adjusted = new Vector2D(clampedX, clampedY);
        if (track.getInnerBounds().contains(adjusted.x, adjusted.y)) {
            adjusted = track.defaultSpawn();
        }

        car.setPosition(adjusted);
        car.setVelocity(car.getVelocity().scale(-0.2));
        car.damage(2);
    }
}
