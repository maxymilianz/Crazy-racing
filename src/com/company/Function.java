package com.company;

import javafx.geometry.Point2D;

/**
 * Created by Lenovo on 28.05.2017.
 */
public class Function {
    private boolean vertical = false;
    private double a, b, x;

    public Function(Point2D p1, Point2D p2) {
        double divider = p2.getX() - p1.getX();

        if (divider != 0) {
            double maybeA = (p2.getY() - p1.getY()) / divider;

            if (Double.isFinite(maybeA)) {
                a = maybeA;
                b = p1.getY() - a * p1.getX();
            }
            else {
                vertical = true;
                x = p1.getX();
            }
        }
        else {
            a = 0;
            b = p1.getY();
        }
    }

    public Point2D cross(Function f) {
        if (a == 0 && f.a == 0 && b == f.b)
            return new Point2D(0, b);
        else if (vertical && f.vertical && x == f.x)
            return new Point2D(x, 0);
        else if (vertical)
            return new Point2D(x, f.value(x));
        else if (f.vertical)
            return new Point2D(f.x, value(f.x));
        else {
            double divider = a - f.a;
            double maybeX = (f.b - b) / (divider != 0 ? divider : Double.MIN_VALUE);
            double x = Double.isFinite(maybeX) ? maybeX : Double.MAX_VALUE;

            return new Point2D(x, a * x + b);
        }
    }

    private double value(double arg) {
        return a * arg + b;
    }
}
