package com.company;

import javafx.geometry.Point2D;

import java.awt.*;

/**
 * Created by Lenovo on 14.05.2017.
 */
public class Map {
    private boolean dir;        // default (false) is starting to right or up
    private int startX, startY, laps, finished = 0;       // coordinates on map[][], finished is how many players finished

    private Point[] checkpoints;

    private Textures.Image[][] map;

    public Map(boolean dir, int startX, int startY, int laps, Point[] checkpoints, Textures.Image[][] map) {
        this.dir = dir;
        this.startX = startX;
        this.startY = startY;
        this.laps = laps;
        this.checkpoints = checkpoints;
        this.map = map;
    }

    public static boolean onTile(Point2D p, Textures.Image tile) {
        if (tile.equals(Textures.Image.EMPTY))
            return false;

        double angle = tile.equals(Textures.Image.DL) || tile.equals(Textures.Image.H) ? 0 : tile.equals(Textures.Image.DR) || tile.equals(Textures.Image.V) ? Math.PI / 2 : tile.equals(Textures.Image.UL) ? 3 * Math.PI / 2 : Math.PI;
        double sinus = Math.sin(angle), cosinus = Math.cos(angle);
        Matrix2x2 rotate = new Matrix2x2(cosinus, -sinus, sinus, cosinus);
        Point2D c = new Point2D(Textures.currentTrackSize / 2, Textures.currentTrackSize / 2);
        p = rotate.apply(p.subtract(c)).add(c);

        return ((tile.equals(Textures.Image.H) || tile.equals(Textures.Image.V)) && p.getY() > 16 && p.getY() < Textures.currentTrackSize - 16) ||
                p.distance(Textures.currentTrackSize, 0) > 15 && p.distance(0, Textures.currentTrackSize) < Textures.currentTrackSize - 17;
    }

    public boolean isDir() {
        return dir;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public Textures.Image[][] getMap() {
        return map;
    }

    public Point[] getCheckpoints() {
        return checkpoints;
    }

    public int getLaps() {
        return laps;
    }

    public void incFinished() {
        finished++;
    }

    public void resetFinished() {
        finished = 0;
    }

    public int getFinished() {
        return finished;
    }

    public void print() {
        System.out.print("  ");

        for (int i = 0; i < map[0].length; i++)
            System.out.print(i % 10 + " ");

        System.out.println();

        for (int i = 0; i < map.length; i++) {
            System.out.print(i + " ");

            for (int j = 0; j < map[0].length; j++)
                System.out.print((map[i][j].equals(Textures.Image.EMPTY) ? 0 : 1) + " ");

            System.out.println();
        }
    }
}
