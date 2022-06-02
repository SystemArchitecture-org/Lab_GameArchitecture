package at.fhv.sysarch.lab4.physics;

import at.fhv.sysarch.lab4.game.Ball;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.Step;
import org.dyn4j.dynamics.StepListener;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;
import org.dyn4j.geometry.Vector2;

public class Physics implements ContactListener, StepListener {

    private World world;
    private BallPocketedListener ballPocketedListener;
    private ObjectsRestListener objectsRestListener;
    private BallsCollisionListener ballsCollisionListener;
    private boolean objectsMoving = false;


    public Physics() {
        this.world = new World();
        this.world.setGravity(World.ZERO_GRAVITY);
        this.world.addListener(this);
    }

    public World getWorld() {
        return world;
    }

    @Override
    public void begin(Step step, World world) {
        int movingBallCount = 0;

        for (Ball ball : Ball.values()) {
            if (!ball.getBody().getLinearVelocity().isZero()){

                movingBallCount++;
            }
        }

        if (movingBallCount > 0 && !objectsMoving) {
            objectsRestListener.onEndAllObjectsRest();
            objectsMoving = true;

        } else if (movingBallCount == 0 && objectsMoving) {
            objectsRestListener.onStartAllObjectsRest();
            objectsMoving = false;
        }
    }

    @Override
    public void updatePerformed(Step step, World world) {

    }

    @Override
    public void postSolve(Step step, World world) {

    }

    @Override
    public void end(Step step, World world) {

    }

    @Override
    public void sensed(ContactPoint point) {
        System.out.println("POCKET DETECTED");
    }

    @Override
    public boolean begin(ContactPoint point) {

        if (point.getBody1().getUserData() instanceof Ball && point.getBody2().getUserData() instanceof Ball) {
            Ball ball1 = (Ball) point.getBody1().getUserData();
            Ball ball2 = (Ball) point.getBody2().getUserData();

            ballsCollisionListener.onBallsCollide(ball1, ball2);
        }

        return true;
    }

    @Override
    public void end(ContactPoint point) {

    }

    @Override
    public boolean persist(PersistedContactPoint point) {
        if (point.isSensor()) {
            Body ball;
            Body pocket;

            if (point.getBody1().getUserData() instanceof Ball) {
                ball = point.getBody1();
                pocket = point.getBody2();
            } else {
                ball = point.getBody2();
                pocket = point.getBody1();
            }

            if (isBallPocketed(ball, pocket, point)) {
                ballPocketedListener.onBallPocketed((Ball) ball.getUserData());
            }
        }

        return true;
    }

    private boolean isBallPocketed(Body ball, Body pocket, PersistedContactPoint point) {
        // World coordinates of ball
        Vector2 ballPosition = ball.getTransform().getTranslation();

        // Pocket position (relative to table)
        Vector2 pocketPosition = pocket.getTransform().getTranslation();
        Vector2 pocketCenter = point.getFixture2().getShape().getCenter();

        // World coordinates of pocket
        Vector2 pocketInWorld = pocketPosition.add(pocketCenter);

        Vector2 difference = ballPosition.difference(pocketInWorld);
        double magnitudeDifference = difference.getMagnitude();

        return magnitudeDifference <= Ball.Constants.RADIUS;
    }

    public void setBallPocketedListener(BallPocketedListener ballPocketedListener) {
        this.ballPocketedListener = ballPocketedListener;
    }

    public void setObjectsRestListener(ObjectsRestListener objectsRestListener) {
        this.objectsRestListener = objectsRestListener;
    }

    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }

    @Override
    public void postSolve(SolvedContactPoint point) {

    }


    public void setBallsCollisionListener(BallsCollisionListener ballsCollisionListener) {
        this.ballsCollisionListener = ballsCollisionListener;
    }
}
