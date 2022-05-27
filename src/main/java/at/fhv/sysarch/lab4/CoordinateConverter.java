package at.fhv.sysarch.lab4;

import static at.fhv.sysarch.lab4.rendering.Renderer.SCALE;

public class CoordinateConverter {
    private static CoordinateConverter instance = null;
    private double centerX;
    private double centerY;

    public static CoordinateConverter getInstance(){
        if (instance == null){
            instance = new CoordinateConverter();
        }
        return instance;
    }

    public CoordinateConverter() {
    }

    public double screenToPhysicsX(double screenX) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE

        double pX = screenX - centerX;
        pX = pX / SCALE;

        return pX;
    }

    public double screenToPhysicsY(double screenY) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE

        double pY = screenY - centerY;
        pY = pY / SCALE;

        return pY;
    }

    public double physicsToScreenX(double physicsX) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE
        double pX = physicsX * SCALE;
        pX = pX + centerX;

        return pX;
    }

    public double physicsToScreenY(double physicsY) {
        // screen has origin (0/0) top left corner,
        // physics has origin (0/0) center of the screen
        // and physics is scaled by factor SCALE
        double pY = physicsY * SCALE;
        pY = pY + centerY;

        return pY;
    }

    public CoordinateConverter setSceneWidth(double sceneWidth){
        this.centerX = (double) sceneWidth * 0.5;
        return instance;
    }

    public CoordinateConverter setSceneHeight(double sceneHeight){
        this.centerY = (double) sceneHeight * 0.5;
        return instance;
    }
}
