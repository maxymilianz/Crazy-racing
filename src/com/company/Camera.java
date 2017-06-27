package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;

/**
 * Created by Lenovo on 15.05.2017.
 */
public class Camera extends JPanel {
    private final int x, y;

    private boolean paused = false;
    private int bgX = 0, bgY = 0;

    private Textures.Image bg;
    private Map map;
    private Menu menu;
    private Textures textures;
    private Vehicle vehicle;

    private Image pauseBg, pauseText, controlImage;
    private BufferedImage arrow, marker;

    private LinkedList<Vehicle> vehicles = new LinkedList<>();
    private LinkedList<PhysicalObject> objects = new LinkedList<>();

    public Camera(int x, int y, Game.Control control, Textures textures, Menu menu, Image pauseBg) {
        this.x = x;
        this.y = y;
        this.textures = textures;
        this.menu = menu;
        this.pauseBg = pauseBg;

        if (control == Game.Control.FIN_LVL)
            controlImage = textures.getImagesE().get(Textures.Image.LEVEL);
        else if (control == Game.Control.FIN_GAME)
            controlImage = textures.getImagesE().get(Textures.Image.GAME);
    }

    public Camera(int x, int y, Textures.Image bg, Map map, Menu menu, Textures textures, Vehicle vehicle, LinkedList<Vehicle> vehicles, LinkedList<PhysicalObject> objects) {
        this.x = x;
        this.y = y;
        this.bg = bg;
        this.map = map;
        this.menu = menu;
        this.textures = textures;
        this.vehicle = vehicle;
        this.vehicles = vehicles;
        this.objects = objects;

        pauseBg = textures.getImagesE().get(Textures.Image.BG);
        pauseText = textures.getImagesE().get(x == 1280 ? Textures.Image.PAUSE : Textures.Image.PAUSE_MULTI);
        arrow = textures.getImagesE().get(Textures.Image.ARROW);
        marker = textures.getImagesE().get(Textures.Image.MARKER);
    }

    public void correctGrass() {
        if (bgX < -2 * Textures.currentBgSize - 1)
            bgX += Textures.currentBgSize;
        else if (bgX > -Textures.currentBgSize)
            bgX -= Textures.currentBgSize;

        if (bgY < -2 * Textures.currentBgSize - 1)
            bgY += Textures.currentBgSize;
        else if (bgY > -Textures.currentBgSize)
            bgY -= Textures.currentBgSize;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    private void updatePauseBgCoordinates() {
        menu.updateCoordinates();
    }

    public void end(boolean multi, int pos) {
        controlImage = textures.getEnds().get(multi).get(pos);
    }

    public void drown(boolean multi) {
        controlImage = textures.getImagesE().get(multi ? Textures.Image.DROWNED_M : Textures.Image.DROWNED);
    }

    public void paint(Graphics g) throws ConcurrentModificationException {
        if (paused) {       // pause menu
            g.drawImage(pauseBg, menu.x(), menu.y(), null);
            updatePauseBgCoordinates();
            g.drawImage(pauseText, 0, 0, null);
            return;
        }
        else if (controlImage != null) {       // control screens
            g.drawImage(pauseBg, menu.x(), menu.y(), null);
            updatePauseBgCoordinates();
            g.drawImage(controlImage, 0, 0, null);
            return;
        }

        int diffX = x / 2 - vehicle.getX() - vehicle.getImage().getWidth(), diffY = y / 2 - vehicle.getY() - vehicle.getImage().getHeight();

        for (int i = bgX; i + (diffX % Textures.currentBgSize) < x; i += Textures.currentBgSize)        // bg
            for (int j = bgY; j + (diffY % Textures.currentBgSize) < y; j += Textures.currentBgSize)
                g.drawImage(textures.getImagesE().get(bg), i + (diffX % Textures.currentBgSize), j + (diffY % Textures.currentBgSize), null);

        int tempX, tempY;

        for (short i = 0; i < map.getMap().length; i++)     // track
            for (short j = 0; j < map.getMap()[i].length; j++) {
                tempX = j * Textures.currentTrackSize + diffX;
                tempY = i * Textures.currentTrackSize + diffY;

                if (tempX < x && tempX + Textures.currentTrackSize >= 0 && tempY < y && tempY + Textures.currentTrackSize >= 0)
                    g.drawImage(textures.getImagesE().get(map.getMap()[i][j]), tempX, tempY, null);
            }

        for (Vehicle i : vehicles)        // vehicles
            paintPhysicalObject(diffX, diffY, g, i);

        for (PhysicalObject i : objects)      // physical objects
            paintPhysicalObject(diffX, diffY, g, i);

        for (Point p : vehicle.getCheckpoints())      // markers
            g.drawImage(marker, p.x * Textures.currentTrackSize + Textures.currentTrackSize / 2 - marker.getWidth() + diffX,
                    p.y * Textures.currentTrackSize + Textures.currentTrackSize / 2 - marker.getHeight() + diffY, null);

        Map current = map;
        Point p = current.getCheckpoints()[vehicle.getCurrentCheckpoint() < current.getCheckpoints().length ? vehicle.getCurrentCheckpoint() : 0];     // arrow
        tempX = (int) ((p.x + 0.25) * Textures.currentTrackSize);
        tempY = (int) ((p.y + 0.25) * Textures.currentTrackSize);
        AffineTransform at = new AffineTransform();
        double angle = Math.asin(-(vehicle.getY() - tempY) / Math.sqrt((vehicle.getX() - tempX) * (vehicle.getX() - tempX) + (vehicle.getY() - tempY) * (vehicle.getY() - tempY)));
        at.rotate(vehicle.getX() < tempX ? angle : Math.PI - angle, arrow.getWidth() / 2, arrow.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        g.drawImage(op.filter(arrow, null), x / 2 - arrow.getWidth() / 2, 0, null);
    }

    private void paintPhysicalObject(int diffX, int diffY, Graphics g, PhysicalObject o) {
        int x = o.getX() + diffX, y = o.getY() + diffY;

        if (x + 2 * o.getImage().getWidth() > 0 && x < this.x && y + 2 * o.getImage().getHeight() > 0 && y < this.y) {
            AffineTransform at = new AffineTransform();
            at.translate(o.getImage().getWidth() / 2, o.getImage().getHeight() / 2);
            at.rotate(Math.PI / 2 + o.getAngle(), o.getCenterX() - o.getX(), o.getCenterY() - o.getY());
            AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

            g.drawImage(op.filter(o.getImage(), null), x, y, null);
        }
    }
}
