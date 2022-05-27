package at.fhv.sysarch.lab4.game;

import at.fhv.sysarch.lab4.CoordinateConverter;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Rotation;
import org.dyn4j.geometry.Vector2;

import java.util.Optional;

public class Cue {
    private final double startX;
    private final double startY;
    private final CoordinateConverter converter;
    private double endX;
    private double endY;

    public Cue(double startX, double startY) {
        this.startX = startX;
        this.startY = startY;
        this.endY = startY;
        this.endX = startX;
        this.converter = CoordinateConverter.getInstance();
    }

    public void setEnd(double x, double y) {
        this.endX = x;
        this.endY = y;
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public Vector2 getShotForce() {
        Vector2 start = new Vector2(
                this.converter.screenToPhysicsX(startX),
                this.converter.screenToPhysicsY(startY)
        );
        Vector2 end = new Vector2(
                this.converter.screenToPhysicsX(endX),
                this.converter.screenToPhysicsY(endY)
        );

        return start.subtract(end);
    }

    public Optional<Ray> getShotRay() {
        Vector2 start = new Vector2(
                this.converter.screenToPhysicsX(startX),
                this.converter.screenToPhysicsY(startY)
        );

        Vector2 direction = getShotForce();

        if (!direction.isZero()) {
            return Optional.of(new Ray(start, direction));
        }

        return Optional.empty();
    }
}