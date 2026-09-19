package com.lanracing.Game;

import com.lanracing.Utility.Vector2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Track {
    private final Rectangle2D.Double outerBounds;
    private final Rectangle2D.Double innerBounds;
    private final Line2D.Double startFinishLine;
    private final List<Checkpoint> checkpoints;

    public Track(Rectangle2D.Double outerBounds,
                 Rectangle2D.Double innerBounds,
                 Line2D.Double startFinishLine,
                 List<Checkpoint> checkpoints) {
        this.outerBounds = outerBounds;
        this.innerBounds = innerBounds;
        this.startFinishLine = startFinishLine;
        this.checkpoints = checkpoints;
    }

    public static Track defaultTrack() {
        Rectangle2D.Double outer = new Rectangle2D.Double(80, 80, 900, 520);
        Rectangle2D.Double inner = new Rectangle2D.Double(300, 220, 460, 240);
        Line2D.Double line = new Line2D.Double(140, 330, 250, 330);

        List<Checkpoint> cps = new ArrayList<>();
        cps.add(new Checkpoint(0, new Vector2D(540, 120), 55));
        cps.add(new Checkpoint(1, new Vector2D(940, 330), 55));
        cps.add(new Checkpoint(2, new Vector2D(540, 560), 55));
        cps.add(new Checkpoint(3, new Vector2D(120, 330), 55));

        return new Track(outer, inner, line, cps);
    }

    public boolean isDriveable(Vector2D point) {
        return outerBounds.contains(point.x, point.y) && !innerBounds.contains(point.x, point.y);
    }

    public Vector2D defaultSpawn() {
        return new Vector2D(180, 330);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(70, 70, 70));
        g2d.fill(outerBounds);
        g2d.setColor(new Color(34, 139, 34));
        g2d.fill(innerBounds);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(6));
        g2d.draw(startFinishLine);

        for (Checkpoint checkpoint : checkpoints) {
            g2d.setColor(new Color(255, 255, 0, 110));
            int r = (int) checkpoint.getRadius();
            int x = (int) checkpoint.getCenter().x - r;
            int y = (int) checkpoint.getCenter().y - r;
            g2d.fillOval(x, y, r * 2, r * 2);
        }
    }

    public Rectangle2D.Double getOuterBounds() {
        return outerBounds;
    }

    public Rectangle2D.Double getInnerBounds() {
        return innerBounds;
    }

    public Line2D.Double getStartFinishLine() {
        return startFinishLine;
    }

    public List<Checkpoint> getCheckpoints() {
        return Collections.unmodifiableList(checkpoints);
    }
}
