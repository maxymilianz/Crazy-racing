package com.company;

import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by Lenovo on 08.05.2017.
 */
public class Vehicle extends PhysicalObject {
    public class Collision {
        Point2D cross, p00, p01, p10, p11;
        PhysicalObject o;

        public Collision(Point2D cross, Point2D p00, Point2D p01, Point2D p10, Point2D p11, PhysicalObject o) {
            this.cross = cross;
            this.p00 = p00;
            this.p01 = p01;
            this.p10 = p10;
            this.p11 = p11;
            this.o = o;
        }
    }

    private final byte maxRevSpeed = -10, brakePower = 1, handbrakePower = 3;
    private final short speedConst = 10, angleConst = 10000, movingAngleConst = 180;     // consts w/o well-defined semantics
    private final double nativeDec = 0.2, nativeDriftDec = 0.4;

    private boolean mapDir, collided = false, onGas = false, finished = false, drowned = false;     // like dir in Map, onGas used ONLY for drift, so not updated for other purposes
    private byte dir = 1, lastTurnDir = 0;       // 1 or -1 (lastTurnDir may be 0 too)
    private int mass, power, nitroPower, maxSpeed;
    private int currentCheckpoint = 0, lap = 0, score = 0;        // score after race
    private int halfWidth, halfHeight;
    private double acc, nitroAcc, speed, movingAngle, deltaAngle, adhesion;

    private LinkedList<Point> checkpoints = new LinkedList<>();

    private Map map;

    public Vehicle(int mass, int power, int nitroPower, int maxSpeed, int x, int y, double angle, BufferedImage image, Map map) {
        this.mass = mass;
        this.power = power;
        acc = (double) power / mass;
        this.nitroPower = nitroPower;
        nitroAcc = nitroPower / mass;
        this.maxSpeed = maxSpeed;

        this.x = x;
        this.y = y;
        this.angle = angle;
        movingAngle = angle;

        mapDir = angle == 0 || angle == -Math.PI / 2;

        this.image = image;
        this.map = map;

        halfWidth = image.getWidth() / 2;
        halfHeight = image.getHeight() / 2;

        initializeCheckpoints();
    }

    private void initializeCheckpoints() {
        for (Point p : map.getCheckpoints())
            checkpoints.add(p);
    }

    public void update() {
        checkIfOnTrack();
        checkCheckpoints();

        if (collided)
            changeAngle();
        if (angle != movingAngle)
            recover();

        correctSpeed();
        brake(nativeDec);
        x += Math.round(speed * Math.cos(movingAngle));
        y += Math.round(speed * Math.sin(movingAngle));
    }

    public void respawn() {
        Point checkpoint = map.getCheckpoints()[currentCheckpoint == 0 ? map.getCheckpoints().length - 1 : currentCheckpoint - 1];
        x = checkpoint.x * Textures.currentTrackSize;
        y = checkpoint.y * Textures.currentTrackSize;

        speed = 0;
        movingAngle = angle;
    }

    private void checkCheckpoints() {
        if (checkpoints.isEmpty())
            return;

        Point checkpoint = checkpoints.getFirst();

        if (currentCheckpoint < map.getCheckpoints().length &&
                checkpoint.getX() == (getCenterX() + halfWidth) / Textures.currentTrackSize &&
                checkpoint.getY() == (getCenterY() + halfHeight) / Textures.currentTrackSize) {
            checkpoints.removeFirst();

            currentCheckpoint++;
            if (currentCheckpoint == map.getCheckpoints().length)
                currentCheckpoint = 0;

            if (checkpoints.isEmpty()) {
                lap++;

                if (lap == map.getLaps())
                    end();
                else
                    initializeCheckpoints();
            }
        }
    }

    public void end() {
        score = 4 - map.getFinished();      // 4 is nr of players
        map.incFinished();
        finished = true;
    }

    public void gas() {
        if (speed == 0)
            dir = 1;

        if (dir == 1)
            accelerate(acc);
        else
            brake(brakePower);
    }

    public void rev() {
        if (speed == 0)
            dir = -1;

        if (dir == -1)
            accelerate(acc);
        else
            brake(brakePower);
    }

    public void handBrake() {
        if (speed < 5)
            brake(handbrakePower);
        else {
            brake(nativeDriftDec);
            if (onGas)
                movingAngle -= lastTurnDir * Math.PI / 90;
            lastTurnDir = 0;
            onGas = false;
        }
    }

    public void nitro() {
        accelerate(nitroAcc);
    }

    private void accelerate(double acc) {
        onGas = true;
        speed += acc * dir * adhesion;

        if (speed > maxSpeed)
            speed = maxSpeed;
        else if (speed < maxRevSpeed)
            speed = maxRevSpeed;
    }

    private void brake(double force) {
        if (speed != 0) {
            byte oldDir = dir;
            speed -= dir * force * adhesion;
            updateDir();

            if (dir != oldDir) {
                speed = 0;
                updateDir();
            }
        }
    }

    private void updateDir() {
        dir = speed >= 0 ? (byte) 1 : (byte) -1;
    }

    public void turn(byte dir) {
        lastTurnDir = dir;

        if (speed != 0) {
            double delta = Math.PI / 45 * dir * this.dir / (Math.abs(speed) > 4 ? 1 : 5 - Math.abs(speed)) * adhesion;
            angle += delta;
            movingAngle += delta;
        }
    }

    public Collision checkForCollision(PhysicalObject object) {
        Point2D[] thisCorners = corners();
        Point2D[] corners = object.corners();

        for (int i = 0; i < thisCorners.length; i++) {
            Point2D snd = i < thisCorners.length - 1 ? thisCorners[i + 1] : thisCorners[0];
            Function f = new Function(thisCorners[i], snd);

            for (int j = 0; j < corners.length; j++) {
                Point2D snd1 = j < corners.length - 1 ? corners[j + 1] : corners[0];
                Function f1 = new Function(corners[j], snd1);
                Point2D cross = f.cross(f1);

                if (cross != null && between(cross, thisCorners[i], snd) && between(cross, corners[j], snd1))
                    return new Collision(cross, thisCorners[i], snd, corners[j], snd1, object);
            }
        }

        return null;
    }

    public void collision(Collision c) {
        if (c == null)
            return;

        if (c.o instanceof Vehicle) {
            collided = true;
            ((Vehicle) c.o).collided = true;

            double newMovingAngle;
            double energySum = (energy() + ((Vehicle) c.o).energy());

            if (energySum != 0) {
                double newMovingAngle0 = (movingAngle * energy() + ((Vehicle) c.o).movingAngle * ((Vehicle) c.o).energy()) / 2 / energySum;
                double newMovingAngle1 = newMovingAngle0 + Math.PI;
                newMovingAngle = absAnglesDiff(movingAngle, newMovingAngle0) < absAnglesDiff(movingAngle, newMovingAngle1) ? newMovingAngle0 : newMovingAngle1;
            }
            else
                newMovingAngle = movingAngle;

            double deltaSpeed = (speed * mass + ((Vehicle) c.o).speed * ((Vehicle) c.o).mass) / 2 / (mass + ((Vehicle) c.o).mass);
            double perp0 = newMovingAngle - Math.PI / 2;
            double perp1 = newMovingAngle + Math.PI / 2;

            while (checkForCollision(c.o) != null) {
                if (absAnglesDiff(movingAngle, perp0) < absAnglesDiff(movingAngle, perp1)) {
                    goBack(perp0);
                    ((Vehicle) c.o).goBack(perp1);

                    speed = deltaSpeed * Math.cos(absAnglesDiff(newMovingAngle, perp0)) / speedConst;
                    ((Vehicle) c.o).speed = deltaSpeed * Math.cos(absAnglesDiff(newMovingAngle, perp1)) / speedConst;
                }
                else {
                    goBack(perp1);
                    ((Vehicle) c.o).goBack(perp0);

                    speed += deltaSpeed * Math.cos(absAnglesDiff(newMovingAngle, perp1)) / 10;
                    ((Vehicle) c.o).speed += deltaSpeed * Math.cos(absAnglesDiff(newMovingAngle, perp0)) / 10;
                }
            }

            movingAngle = newMovingAngle;
            ((Vehicle) c.o).movingAngle = newMovingAngle;

            deltaAngle = (Math.sqrt(Math.pow(c.cross.getX() - c.p00.getX(), 2) + Math.pow(c.cross.getY() - c.p00.getY(), 2)) +
                    Math.sqrt(Math.pow(c.cross.getX() - c.p01.getX(), 2) + Math.pow(c.cross.getY() - c.p01.getY(), 2))) / angleConst;
            ((Vehicle) c.o).deltaAngle = (Math.sqrt(Math.pow(c.cross.getX() - c.p10.getX(), 2) + Math.pow(c.cross.getY() - c.p10.getY(), 2)) +
                    Math.sqrt(Math.pow(c.cross.getX() - c.p11.getX(), 2) + Math.pow(c.cross.getY() - c.p11.getY(), 2))) / angleConst;
        }
        else if (c.o.getType() == Textures.Image.GRASS)
            brake(0.55 * acc);
        else {
            while (checkForCollision(c.o) != null)
                goBack(movingAngle + (speed < 0 ? Math.PI : 0));

            speed = 0;
        }
    }

    private void checkIfOnTrack() {
        int x = (getCenterX() + image.getWidth() / 2) / Textures.currentTrackSize, y = (getCenterY() + image.getHeight() / 2) / Textures.currentTrackSize;
        Point2D p = new Point2D(getCenterX() - x * Textures.currentTrackSize + image.getWidth() / 2, getCenterY() - y * Textures.currentTrackSize + image.getHeight() / 2);
        Textures.Image current = Textures.Image.EMPTY;

        try {
            current = map.getMap()[y][x];
        }
        catch (ArrayIndexOutOfBoundsException e) { }

        Textures.Image bg = Game.bg;

        if (!Map.onTile(p, current) && bg == Textures.Image.WATER)
            drowned = true;

        adhesion = Map.onTile(p, current) ? 1 : bg == Textures.Image.GRASS ? 0.65 : bg == Textures.Image.SAND ? 0.45 : 0.55;
    }

    private int nextTurn() {        // returns nr of pixels to next turn, >0 - left, <0 - right
        // FOR AI
        return 0;
    }

    private double energy() {
        return mass * speed * speed;
    }

    private void goBack(double angle) {
        x -= Math.round(Math.cos(angle));
        y -= Math.round(Math.sin(angle));
    }

    private void correctSpeed() {
        if (speed > maxSpeed)
            speed = maxSpeed;
        else if (speed < maxRevSpeed)
            speed = maxRevSpeed;

        brake(Math.sin(absAnglesDiff(angle, movingAngle)) * nativeDriftDec);
    }

    private void recover() {
        angle %= 2 * Math.PI;
        movingAngle %= 2 * Math.PI;

        if (angle < 0)
            angle += 2 * Math.PI;
        if (movingAngle < 0)
            movingAngle += 2 * Math.PI;

        movingAngle += Math.PI / movingAngleConst * (angle > movingAngle ? 1 : -1) * (Math.abs(angle - movingAngle) < Math.PI ? 1 : -1) * adhesion;

        if (absAnglesDiff(angle, movingAngle) < Math.PI / movingAngleConst || speed == 0)
            movingAngle = angle;
    }

    private double absAnglesDiff(double angle0, double angle1) {
        angle0 %= 2 * Math.PI;
        angle1 %= 2 * Math.PI;
        double abs = Math.abs(angle0 - angle1);

        while (abs > Math.PI) {
            if (angle0 > angle1)
                angle0 -= 2 * Math.PI;
            else
                angle1 -= 2 * Math.PI;

            abs = Math.abs(angle0 - angle1);
        }

        return abs;
    }

    private void changeAngle() {
        angle += deltaAngle * adhesion;
        deltaAngle -= Math.PI / 180 * adhesion;

        if (deltaAngle <= 0)
            collided = false;
    }

    private boolean between(Point2D center, Point2D p0, Point2D p1) {
        return ((center.getX() <= p0.getX() && center.getX() >= p1.getX()) || (center.getX() >= p0.getX() && center.getX() <= p1.getX())) &&
                ((center.getY() <= p0.getY() && center.getY() >= p1.getY()) || (center.getY() >= p0.getY() && center.getY() <= p1.getY()));
    }

    public Point2D[] corners() {
        double sinus = Math.sin(angle + Math.PI / 2), cosinus = Math.cos(angle + Math.PI / 2);
        Point2D c = new Point2D(getCenterX(), getCenterY());
        Matrix2x2 rotate = new Matrix2x2(cosinus, -sinus, sinus, cosinus);

        Point2D ul = rotate.apply(new Point2D(x + 4, y + 1).subtract(c)).add(c);
        Point2D ur = rotate.apply(new Point2D(x + image.getWidth() - 4, y + 1).subtract(c)).add(c);
        Point2D dr = rotate.apply(new Point2D(x + image.getWidth() - 4, y + image.getHeight() - 1).subtract(c)).add(c);
        Point2D dl = rotate.apply(new Point2D(x + 4, y + image.getHeight() - 1).subtract(c)).add(c);

        Point2D[] res = {ul, dl, dr, ur};
        return res;
    }

    public int getCurrentCheckpoint() {
        return currentCheckpoint;
    }

    public LinkedList<Point> getCheckpoints() {
        return checkpoints;
    }

    public int getScore() {
        return score;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isDrowned() {
        return drowned;
    }
}
