package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import at.fhv.sysarch.lab4.physics.BallsCollisionListener;
import at.fhv.sysarch.lab4.physics.ObjectsRestListener;
import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;

import static at.fhv.sysarch.lab4.rendering.Renderer.SCALE;

public class Game implements BallPocketedListener, ObjectsRestListener, BallsCollisionListener {
    private final Renderer renderer;
    private final Physics physics;
    private int player1Score = 0;
    private int player2Score = 0;
    private Player currentPlayer = Player.PLAYER_ONE;
    private boolean isFoul = false;
    private boolean isWhiteBallPocketed = false;
    private double whiteBallX = Table.Constants.WIDTH * 0.25;
    private double whiteBallY = 0;
    private boolean ballsMoving = false;
    private boolean ballsTouched = false;
    private String foulMessage = "";
    private String actionMessage = "";
    private boolean regularBallPocketed = false;
    private final List<Ball> pocketedBalls = new ArrayList<>();

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        renderer.setStrikeMessage("Next Strike: " + currentPlayer.name);
        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        if (ballsMoving) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        this.renderer.setCue(
                Optional.of(new Cue(x, y))
        );
    }

    public void onMouseReleased(MouseEvent e) {
        if (ballsMoving || renderer.getCue().isEmpty()) {
            return;
        }

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
                    foulMessage = "Foul: Direct hit of a regular ball!";
                }

                whiteBallX = Ball.WHITE.getBody().getTransform().getTranslationX();
                whiteBallY = Ball.WHITE.getBody().getTransform().getTranslationY();

                hit.getBody().applyForce(cue.getShotForce().multiply(SCALE));
            }
        }

        this.renderer.setCue(Optional.empty());
    }

    public void setOnMouseDragged(MouseEvent e) {
        if (ballsMoving || renderer.getCue().isEmpty()) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        this.renderer.getCue().get().setEnd(x, y);
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billiard balls IN WORLD COORDINATES: meters
        int row = balls.size() > 14 ? 0 : 1;
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
            if (b == Ball.WHITE) {
                continue;
            }

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
        b.getBody().setLinearVelocity(0, 0);

        if (b.isWhite()) {
            isWhiteBallPocketed = true;
            isFoul = true;
            foulMessage = "Foul: White Ball Pocketed!";
            renderer.removeBall(b);
        } else {
            pocketedBalls.add(b);
            regularBallPocketed = true;
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
        ballsMoving = true;
    }

    private void switchPlayer() {
        currentPlayer = currentPlayer.getOpponent();
        renderer.setStrikeMessage("Next Strike: " + currentPlayer.name);
    }

    private void resetGame() {

        for (Ball b: pocketedBalls) {
            physics.getWorld().addBody(b.getBody());
        }

        this.placeBalls(pocketedBalls);

        pocketedBalls.clear();

    }

    @Override
    public void onStartAllObjectsRest() {
        ballsMoving = false;

        if (pocketedBalls.size() >= 14) {
            resetGame();
        }

        if (!ballsTouched) {
            foulMessage = foulMessage.equals("") ? "Foul: White ball did not touch any other ball(s)!" : foulMessage;
            isFoul = true;
        }

        if (isFoul) {
            actionMessage = currentPlayer.getName() + " committed a foul, switching players.";
            updatePlayerScore(-1);
            switchPlayer();
            isFoul = false;
        } else if (!regularBallPocketed) {
            actionMessage = currentPlayer.getName() + " did not pocket a ball, switching players.";
            switchPlayer();
        }

        if (isWhiteBallPocketed) {
            renderer.addBall(Ball.WHITE);
            resetWhiteBall();
            isWhiteBallPocketed = false;
        }

        renderer.setActionMessage(actionMessage);
        actionMessage = "";
        renderer.setFoulMessage(foulMessage);
        foulMessage = "";

        regularBallPocketed = false;
        ballsTouched = false;
    }

    private void resetWhiteBall() {
        Ball.WHITE.setPosition(whiteBallX, whiteBallY);
    }

    @Override
    public void onBallsCollide(Ball b1, Ball b2) {
        if (b1.isWhite() || b2.isWhite()) {
            ballsTouched = true;
        }
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