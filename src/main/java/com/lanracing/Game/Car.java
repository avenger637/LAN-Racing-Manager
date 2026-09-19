package com.lanracing.Game;

import com.lanracing.Utility.InputState;
import com.lanracing.Utility.Vector2D;

import java.awt.Color;
import java.awt.Graphics2D;

public class Car {
    public enum VehicleType {
        SPEEDSTER(340, 180, 220, Color.CYAN),
        MUSCLE(285, 240, 200, Color.RED),
        BALANCED(310, 210, 210, Color.GREEN);

        final double maxSpeed;
        final double acceleration;
        final double turnRateDeg;
        final Color color;

        VehicleType(double maxSpeed, double acceleration, double turnRateDeg, Color color) {
            this.maxSpeed = maxSpeed;
            this.acceleration = acceleration;
            this.turnRateDeg = turnRateDeg;
            this.color = color;
        }
    }

    private final String playerId;
    private final VehicleType vehicleType;
    private Vector2D position;
    private Vector2D velocity = new Vector2D();
    private double directionDeg;
    private double health = 100;
    private double nitroMeter = 100;
    private boolean shieldActive;
    private int lastInputSequence;

    public Car(String playerId, VehicleType vehicleType, Vector2D spawnPosition, double directionDeg) {
        this.playerId = playerId;
        this.vehicleType = vehicleType;
        this.position = spawnPosition;
        this.directionDeg = directionDeg;
    }

    public void applyInput(InputState input, double dt) {
        if (input == null) {
            return;
        }
        if (input.turnLeft) {
            directionDeg -= vehicleType.turnRateDeg * dt;
        }
        if (input.turnRight) {
            directionDeg += vehicleType.turnRateDeg * dt;
        }

        double forward = 0;
        if (input.accelerate) {
            forward += vehicleType.acceleration;
        }
        if (input.brake) {
            forward -= vehicleType.acceleration * 0.7;
        }
        if (input.nitro && nitroMeter > 0) {
            forward += vehicleType.acceleration * 0.8;
            nitroMeter = Math.max(0, nitroMeter - (35 * dt));
        } else {
            nitroMeter = Math.min(100, nitroMeter + (12 * dt));
        }

        double radians = Math.toRadians(directionDeg);
        Vector2D accel = new Vector2D(Math.cos(radians), Math.sin(radians)).scale(forward * dt);
        velocity = velocity.add(accel);

        double max = vehicleType.maxSpeed;
        double speed = velocity.length();
        if (speed > max) {
            velocity = velocity.normalize().scale(max);
        }

        velocity = velocity.scale(0.985);
        if (velocity.length() < 1.0) {
            velocity = new Vector2D();
        }

        position = position.add(velocity.scale(dt));
    }

    public void reset(Vector2D spawnPosition) {
        this.position = spawnPosition;
        this.velocity = new Vector2D();
        this.directionDeg = 0;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(vehicleType.color);
        int width = 26;
        int height = 14;
        g2d.fillRect((int) position.x - width / 2, (int) position.y - height / 2, width, height);
    }

    public void damage(double amount) {
        if (shieldActive) {
            return;
        }
        health = Math.max(0, health - amount);
    }

    public void heal(double amount) {
        health = Math.min(100, health + amount);
    }

    public String getPlayerId() {
        return playerId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
    }

    public double getDirectionDeg() {
        return directionDeg;
    }

    public void setDirectionDeg(double directionDeg) {
        this.directionDeg = directionDeg;
    }

    public double getHealth() {
        return health;
    }

    public double getNitroMeter() {
        return nitroMeter;
    }

    public void addNitro(double amount) {
        nitroMeter = Math.min(100, nitroMeter + amount);
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public void setShieldActive(boolean shieldActive) {
        this.shieldActive = shieldActive;
    }

    public int getLastInputSequence() {
        return lastInputSequence;
    }

    public void setLastInputSequence(int lastInputSequence) {
        this.lastInputSequence = lastInputSequence;
    }
}
