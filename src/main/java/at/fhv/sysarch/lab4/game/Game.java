package at.fhv.sysarch.lab4.game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.CoordinateConverter;
import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import at.fhv.sysarch.lab4.physics.ObjectsRestListener;
import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.collision.narrowphase.Raycast;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

import static at.fhv.sysarch.lab4.rendering.Renderer.SCALE;

public class Game implements BallPocketedListener, ObjectsRestListener {
    private final Renderer renderer;
    private final Physics physics;
    private CoordinateConverter converter;
    private int player1Score = 0;
    private int player2Score = 0;
    private Player currentPlayer = Player.PLAYER_ONE;
    private boolean isFoul = false;
    private boolean isWhiteBallPocketed = false;
    private double whiteBallX = Table.Constants.WIDTH * 0.25;
    private double whiteBallY = 0;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.initWorld();
        this.converter = CoordinateConverter.getInstance();
    }

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        Cue cue = new Cue(x, y);
        this.renderer.setCue(Optional.of(cue));
    }

    public void onMouseReleased(MouseEvent e) {
        Cue cue = this.renderer.getCue().get();
        Optional<Ray> ray = cue.getShotRay();

        if (ray.isPresent()) {
            ArrayList<RaycastResult> results = new ArrayList<>();
            boolean result = this.physics.getWorld().raycast(ray.get(), 0.1, false, false, results);

            if (result && results.get(0).getBody().getUserData() instanceof Ball) {
                RaycastResult hit = results.get(0);
                Body body = hit.getBody();
                Ball ball = (Ball) body.getUserData();

                if (!ball.isWhite()) {
                    isFoul = true;
                }
                hit.getBody().applyForce(cue.getShotForce().multiply(SCALE));
            }
        }

        this.renderer.setCue(Optional.empty());
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        this.renderer.getCue().get().setEnd(x, y);
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2 * Ball.Constants.RADIUS * 2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();

        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
            physics.getWorld().addBody(b.getBody());
        }


        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);

        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }

    @Override
    public boolean onBallPocketed(Ball b) {


        if (b.isWhite()) {
            isWhiteBallPocketed = true;
            isFoul = true;
        } else {
            renderer.removeBall(b);
            physics.getWorld().removeBody(b.getBody());
            updatePlayerScore(1);
        }

        return true;
    }

    private void updatePlayerScore(int score) {
        if (currentPlayer == Player.PLAYER_ONE) {
            player1Score += score;
            renderer.setPlayer1Score(player1Score);
        } else {
            player2Score += score;
            renderer.setPlayer2Score(player2Score);
        }

    }

    @Override
    public void onEndAllObjectsRest() {

    }

    @Override
    public void onStartAllObjectsRest() {

        if (isFoul) {
            updatePlayerScore(-1);
            currentPlayer = currentPlayer.getOpponent();
            isFoul = false;
        }

        if (isWhiteBallPocketed) {
            System.out.println("White ball pocketed");
            resetWhiteBall();
            isWhiteBallPocketed = false;
        }

        whiteBallX = Ball.WHITE.getBody().getTransform().getTranslationX();
        whiteBallY = Ball.WHITE.getBody().getTransform().getTranslationY();
    }

    private void resetWhiteBall() {
        Ball.WHITE.getBody().setLinearVelocity(0, 0);
        Ball.WHITE.setPosition(whiteBallX, whiteBallY);
    }

    public enum Player {
        PLAYER_ONE("Player 1"),
        PLAYER_TWO("Player 2");

        private final String name;

        Player(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Player getOpponent() {
            return this == PLAYER_ONE ? PLAYER_TWO : PLAYER_ONE;
        }
    }
}