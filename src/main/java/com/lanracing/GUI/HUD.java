package com.lanracing.GUI;

import com.lanracing.Game.Car;
import com.lanracing.Game.RaceManager;

import java.awt.Color;
import java.awt.Graphics2D;

public class HUD {
    public void draw(Graphics2D g2d, Car car, RaceManager raceManager, long elapsedMillis) {
        if (car == null) {
            return;
        }

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRoundRect(10, 10, 290, 130, 15, 15);

        g2d.setColor(Color.WHITE);
        g2d.drawString("Speed: " + String.format("%.1f", car.getVelocity().length()), 22, 34);
        g2d.drawString("Nitro: " + String.format("%.0f", car.getNitroMeter()), 22, 56);
        g2d.drawString("Health: " + String.format("%.0f", car.getHealth()), 22, 78);
        g2d.drawString("Lap: " + raceManager.getLapsCompleted(car.getPlayerId()) + "/" + raceManager.getTotalLaps(), 22, 100);
        g2d.drawString("Time: " + (elapsedMillis / 1000.0) + "s", 22, 122);
    }
}
