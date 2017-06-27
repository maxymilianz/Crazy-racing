package com.company;

import javafx.geometry.Point2D;

import java.awt.image.BufferedImage;

/**
 * Created by Lenovo on 08.05.2017.
 */
public class PhysicalObject {
    int x, y;
    double angle;

    BufferedImage image;

    private Textures.Image type;

    public PhysicalObject() { }

    public PhysicalObject(int x, int y, double angle, BufferedImage image, Textures.Image type) {
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.image = image;
        this.type = type;
    }

    public int getCenterX() {
        return x + image.getWidth() / 2;
    }

    public int getCenterY() {
        return y + image.getHeight() / 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Textures.Image getType() {
        return type;
    }

    public Point2D[] corners() {
        Point2D c = new Point2D(getCenterX(), getCenterY());
        int margin = type.equals(Textures.Image.GRASS) ? 28 : type.equals(Textures.Image.SAND) ? 4 : 50;

        Point2D ul = c;
        Point2D ur = c.add(margin, 0);
        Point2D dr = c.add(0, margin);
        Point2D dl = c.add(margin, margin);

        Point2D[] res = {ul, dl, dr, ur};
        return res;
    }
}
