package com.lanracing.GUI;

import com.lanracing.Game.Car;
import com.lanracing.Game.Game;
import com.lanracing.Game.PowerUp;
import com.lanracing.Utility.Constants;
import com.lanracing.Utility.InputState;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GamePanel extends JPanel {
    private final Game game;
    private final String localPlayerId;
    private final HUD hud = new HUD();

    public GamePanel(Game game, String localPlayerId) {
        this.game = game;
        this.localPlayerId = localPlayerId;
        setBackground(new Color(20, 20, 20));
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
        setFocusable(true);

        initKeyboard();
        Timer timer = new Timer((int) (Constants.FIXED_TIMESTEP * 1000), e -> {
            game.update(Constants.FIXED_TIMESTEP);
            repaint();
        });
        timer.start();
    }

    private void initKeyboard() {
        InputState input = game.getInputState(localPlayerId);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (input == null) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        input.accelerate = true;
                        break;
                    case KeyEvent.VK_S:
                        input.brake = true;
                        break;
                    case KeyEvent.VK_A:
                        input.turnLeft = true;
                        break;
                    case KeyEvent.VK_D:
                        input.turnRight = true;
                        break;
                    case KeyEvent.VK_SPACE:
                        input.nitro = true;
                        break;
                    case KeyEvent.VK_R:
                        input.reset = true;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (input == null) {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        input.accelerate = false;
                        break;
                    case KeyEvent.VK_S:
                        input.brake = false;
                        break;
                    case KeyEvent.VK_A:
                        input.turnLeft = false;
                        break;
                    case KeyEvent.VK_D:
                        input.turnRight = false;
                        break;
                    case KeyEvent.VK_SPACE:
                        input.nitro = false;
                        break;
                    default:
                        break;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        game.getTrack().draw(g2d);

        for (PowerUp powerUp : game.getPowerUps()) {
            if (!powerUp.isActive()) {
                continue;
            }
            g2d.setColor(Color.MAGENTA);
            int x = (int) powerUp.getPosition().x - 8;
            int y = (int) powerUp.getPosition().y - 8;
            g2d.fillOval(x, y, 16, 16);
        }

        for (Car car : game.getGameState().getCarsByPlayerId().values()) {
            car.draw(g2d);
        }

        Car localCar = game.getGameState().getCarsByPlayerId().get(localPlayerId);
        long elapsed = game.getGameState().isRaceStarted() ? (System.currentTimeMillis() - game.getRaceStartMillis()) : 0;
        hud.draw(g2d, localCar, game.getRaceManager(), elapsed);

        g2d.dispose();
    }
}
