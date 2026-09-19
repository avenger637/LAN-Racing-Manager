package com.lanracing.Game;

import com.lanracing.Utility.Vector2D;

public class PowerUp {
    public enum Type {
        NITRO,
        SHIELD,
        REPAIR,
        TELEPORT
    }

    private final Type type;
    private final Vector2D position;
    private boolean active = true;

    public PowerUp(Type type, Vector2D position) {
        this.type = type;
        this.position = position;
    }

    public void applyTo(Car car, Track track) {
        if (!active) {
            return;
        }
        switch (type) {
            case NITRO:
                car.addNitro(40);
                break;
            case SHIELD:
                car.setShieldActive(true);
                break;
            case REPAIR:
                car.heal(35);
                break;
            case TELEPORT:
                car.setPosition(track.defaultSpawn());
                car.setVelocity(new Vector2D());
                break;
            default:
                break;
        }
        active = false;
    }

    public boolean canCollect(Car car) {
        return active && car.getPosition().subtract(position).length() <= 22;
    }

    public Type getType() {
        return type;
    }

    public Vector2D getPosition() {
        return position;
    }

    public boolean isActive() {
        return active;
    }
}
