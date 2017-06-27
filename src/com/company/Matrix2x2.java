package com.company;

import javafx.geometry.Point2D;

/**
 * Created by Lenovo on 23.05.2017.
 */

/*
    a b
    c d
 */

public class Matrix2x2 {
    private double a, b, c, d;

    public Matrix2x2(double a, double b, double c, double d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public Point2D apply(Point2D p) {
        return new Point2D(a * p.getX() + b * p.getY(), c * p.getX() + d * p.getY());
    }
}
