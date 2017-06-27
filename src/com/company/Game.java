package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Lenovo on 08.05.2017.
 */
public class Game extends JFrame implements Runnable {
    public enum Control {
        FIN_LVL, FIN_GAME, FIN_DIFF, ALL_DROWNED, IN_MENU
    }

    public enum Player {
        PLAYER0, PLAYER1,
        CPU0, CPU1, CPU2
    }

    private final int x = 1280, y = 600, margin = 500, fps = 60, delta = 1000 / fps, nrOfObjects = 70;       // margin is space between end of track and end of trees

    private boolean multiplayer = false, paused = false;
    private int level = 1, difficulty = 0;

    public static Textures.Image bg;

    private Container container;
    private Thread thread;

    private Control control = Control.IN_MENU;
    private Camera fst, snd;
    private Vehicle fstPlayer, sndPlayer;       // maybe should be in list
    private Input input;
    private Textures textures = new Textures();
    private Menu menu;

    private Player[] players = {Player.PLAYER0, Player.PLAYER1, Player.CPU0, Player.CPU1, Player.CPU2};
    public static Map[] maps;       // D - down, U - upper, R - right, L - left, H - horizontal, V - vertical


    private LinkedList<Camera> cameras = new LinkedList<>();
    private LinkedList<Vehicle> vehicles = new LinkedList<>();
    private LinkedList<PhysicalObject> objects = new LinkedList<>();
    private LinkedList<AI> ais = new LinkedList<>();

    private Hashtable<Player, Integer> scores = new Hashtable<>();      // player -> score
    private Hashtable<Player, Vehicle> playersToVehicles = new Hashtable<>();

    public Game() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Crazy racing");
        setFocusable(true);

        container = getContentPane();
        container.setPreferredSize(new Dimension(x, y));
        container.setLayout(new BoxLayout(container, BoxLayout.LINE_AXIS));

        initializeMaps();

        menu = new Menu(this);
        container.add(menu);

        thread = new Thread(this);
        thread.start();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeLevel(int difficulty) {
        this.difficulty = difficulty;
        bg = textures.getBgsE()[difficulty];

        Textures.currentBgSize = textures.getImagesE().get(bg).getWidth();
        Textures.currentTrackSize = textures.getImagesE().get(Textures.Image.H).getHeight();

        if (!bg.equals(Textures.Image.WATER))
            initializeObjects();
    }

    // OFFSET DEPENDENT ON SCORE WOULD BE A USEFUL FEATURE FOR PLAYING WITH MORE PEOPLE (IN PARTICULAR - AI)

    private void single(int difficulty) {
        control = null;
        container.remove(menu);

        initializeLevel(difficulty);

        int startX = maps[level].getStartX(), startY = maps[level].getStartY();
        int tempX = startX * Textures.currentTrackSize + 25, tempY = startY * Textures.currentTrackSize;
        double vehiclesAngle = (maps[level].getMap()[startY][startX].equals(Textures.Image.H) ? 0 : -Math.PI / 2) + (maps[level].isDir() ? 0 : Math.PI);

        fstPlayer = new Vehicle(1000, 500, 1000, 30, tempX + 137, tempY + 100, vehiclesAngle, textures.getImagesE().get(Textures.Image.RED), maps[level]);
        playersToVehicles.put(Player.PLAYER0, fstPlayer);
        vehicles.add(fstPlayer);

        fst = new Camera(x, y, bg, maps[level], menu, textures, fstPlayer, vehicles, objects);
        cameras.add(fst);
        container.add(fst);
        container.validate();

        input = new Input(fstPlayer);
        addKeyListener(input);

        Vehicle temp = new Vehicle(1000, 500, 1000, 30, tempX + 137, tempY, vehiclesAngle, textures.getImagesE().get(Textures.Image.BLUE), maps[level]);
        playersToVehicles.put(Player.CPU0, temp);
        vehicles.add(temp);
        ais.add(new AI(temp));

        temp = new Vehicle(1000, 500, 1000, 30, tempX, tempY + 100, vehiclesAngle, textures.getImagesE().get(Textures.Image.GREEN), maps[level]);
        playersToVehicles.put(Player.CPU1, temp);
        vehicles.add(temp);
        ais.add(new AI(temp));

        temp = new Vehicle(1000, 500, 1000, 30, tempX, tempY, vehiclesAngle, textures.getImagesE().get(Textures.Image.YELLOW), maps[level]);
        playersToVehicles.put(Player.CPU2, temp);
        vehicles.add(temp);
        ais.add(new AI(temp));
    }

    private void multi(int difficulty) {
        control = null;
        container.remove(menu);

        initializeLevel(difficulty);

        int startX = maps[level].getStartX(), startY = maps[level].getStartY();
        int tempX = startX * Textures.currentTrackSize + 25, tempY = startY * Textures.currentTrackSize;
        double vehiclesAngle = (maps[level].getMap()[startY][startX].equals(Textures.Image.H) ? 0 : -Math.PI / 2) +
                (maps[level].isDir() ? 0 : Math.PI);

        fstPlayer = new Vehicle(1000, 500, 1000, 30, tempX + 137, tempY + 100, vehiclesAngle,
                textures.getImagesE().get(Textures.Image.RED), maps[level]);
        playersToVehicles.put(Player.PLAYER0, fstPlayer);
        vehicles.add(fstPlayer);

        sndPlayer = new Vehicle(1000, 500, 1000, 30, tempX + 137, tempY,        // 137 is arbitrary
                (maps[level].getMap()[startY][startX].equals(Textures.Image.H) ? 0 : -Math.PI / 2) +
                        (maps[level].isDir() ? 0 : Math.PI), textures.getImagesE().get(Textures.Image.BLUE), maps[level]);
        playersToVehicles.put(Player.PLAYER1, sndPlayer);
        vehicles.add(sndPlayer);

        fst = new Camera(x / 2, y, bg, maps[level], menu, textures, fstPlayer, vehicles, objects);
        cameras.add(fst);
        container.add(fst);

        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setSize(0, y);        // not sure why, but changing width does nothing
        separator.setMaximumSize(new Dimension(0, y));
        container.add(separator);

        snd = new Camera(x / 2, y, bg, maps[level], menu, textures, sndPlayer, vehicles, objects);
        cameras.add(snd);
        container.add(snd);

        container.validate();

        input = new Input(fstPlayer, sndPlayer);
        addKeyListener(input);

        Vehicle temp = new Vehicle(1000, 500, 1000, 30, tempX, tempY, vehiclesAngle, textures.getImagesE().get(Textures.Image.GREEN), maps[level]);
        playersToVehicles.put(Player.CPU0, temp);
        vehicles.add(temp);
        ais.add(new AI(vehicles.getLast()));

        temp = new Vehicle(1000, 500, 1000, 30, tempX, tempY + 100, vehiclesAngle, textures.getImagesE().get(Textures.Image.YELLOW), maps[level]);
        playersToVehicles.put(Player.CPU1, temp);
        vehicles.add(temp);
        ais.add(new AI(vehicles.getLast()));
    }

    public void quit() {
        System.exit(0);
    }

    public void newGame(boolean multi, int difficulty) {
        multiplayer = multi;
        toMenu();

        if (!multi)
            single(difficulty);
        else
            multi(difficulty);
    }

    public void run() {
        while (true) {
            long time = System.currentTimeMillis();

            if (control == Control.IN_MENU)
                menu.update();
            else
                updateGame();

            try {
                repaint();
            }
            catch (ConcurrentModificationException e) { }

            try {
                Thread.sleep(delta - (System.currentTimeMillis() - time));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pause() {
        paused = true;

        for (Camera c : cameras)
            c.pause();
    }

    private void resume() {
        paused = false;

        for (Camera c : cameras)
            c.resume();
    }

    private void toMenu() {
        paused = false;

        cameras = new LinkedList<>();
        vehicles = new LinkedList<>();
        objects = new LinkedList<>();
        ais = new LinkedList<>();

        control = Control.IN_MENU;
        menu.reset();
        menu.resetMode();

        container.removeAll();
        container.invalidate();
        container.removeKeyListener(input);
        container.add(menu);
        container.validate();
    }

    private void saveScore() {
        for (Player p : players) {
            Vehicle v = playersToVehicles.get(p);
            scores.put(p, scores.getOrDefault(p, 0) + (v != null ? v.getScore() : 0));
        }
    }

    private void nextDifficulty() {
        maps[level].resetFinished();

        if (difficulty < textures.getBgsE().length - 1) {
            difficulty++;
            return;
        }

        difficulty = 0;
        control = Control.FIN_LVL;
    }

    private void nextLevel() {
        maps[level].resetFinished();

        if (level < maps.length - 1) {
            level++;
            return;
        }

        control = Control.FIN_GAME;
    }

    private void resetGame() {
        level = 1;      // level 0 is for testing
        difficulty = 0;

        saveScore();
        toMenu();
    }

    private void endGame() {
        toMenu();
        control = Control.FIN_GAME;
        remove(menu);
        add(new Camera(x, y, control, textures, menu, textures.getImagesE().get(Textures.Image.BG)));
        validate();
        addKeyListener(new Input(null));
        input.setFst(false);
    }

    private void endLevel() {
        toMenu();
        control = Control.FIN_LVL;
        remove(menu);
        add(new Camera(x, y, control, textures, menu, textures.getImagesE().get(Textures.Image.BG)));
        validate();
        addKeyListener(new Input(null));
        input.setFst(false);
    }

    private void updateGame() {
        Input.Control ctrl = input.check();

        if (ctrl != null) {
            if (ctrl == Input.Control.ESC) {
                if (control == Control.FIN_DIFF) {
                    saveScore();
                    nextDifficulty();

                    if (difficulty != 0)
                        newGame(multiplayer, difficulty);
                    else
                        endLevel();
                }
                else if (control == Control.FIN_LVL) {
                    saveScore();
                    nextLevel();

                    if (level != 1)
                        newGame(multiplayer, difficulty);
                    else
                        endGame();
                }
                else if (control == Control.FIN_GAME)
                    resetGame();
                else if (control == Control.ALL_DROWNED) {
                    saveScore();
                    newGame(multiplayer, difficulty);
                }
                else if (paused)
                    resume();
                else
                    pause();
            }
            else {
                if (paused || control == Control.FIN_DIFF || control == Control.FIN_LVL || control == Control.ALL_DROWNED) {
                    saveScore();
                    toMenu();
                }
            }

            return;
        }

        if (fstPlayer.isFinished()) {
            fst.end(multiplayer, maps[level].getFinished());
            input.setFst(false);
        }
        else if (fstPlayer.isDrowned()) {
            fst.drown(multiplayer);
            input.setFst(false);
        }

        if (multiplayer) {
            if (sndPlayer.isFinished()) {
                snd.end(multiplayer, maps[level].getFinished());
                input.setSnd(false);
            }
            else if (sndPlayer.isDrowned()) {
                snd.drown(multiplayer);
                input.setSnd(false);
            }
        }

        if (control != Control.FIN_LVL && control != Control.FIN_GAME) {
            if (fstPlayer.isFinished() && (!multiplayer || sndPlayer.isFinished()))
                control = Control.FIN_DIFF;
            else if (fstPlayer.isDrowned() && (!multiplayer || sndPlayer.isDrowned()))
                control = Control.ALL_DROWNED;
        }

        if (!paused) {
            for (AI i : ais)
                i.update();

            move();
            checkForCollision();

            for (Camera c : cameras)
                c.correctGrass();
        }
    }

    private void move() {
        for (Vehicle i : vehicles)
            i.update();
    }

    private void checkForCollision() {
        for (Vehicle i : vehicles) {
            for (Vehicle j : vehicles) {
                if (i.equals(j))
                    break;

                i.collision(i.checkForCollision(j));
            }

            for (PhysicalObject j : objects)
                i.collision(i.checkForCollision(j));
        }
    }

    private void initializeObjects() {
        for (short i = 0; i < nrOfObjects; i++) {
            Random r = new Random();
            boolean collides = true;
            int x = 0, y = 0;
            Textures.Image[] trees = textures.getTreesE().get(bg);
            BufferedImage img = textures.getImagesE().get(trees[r.nextInt(trees.length)]);

            while (collides) {
                collides = false;
                x = (r.nextInt(2 * margin + maps[level].getMap()[0].length * Textures.currentTrackSize)) - margin;
                y = (r.nextInt(2 * margin + maps[level].getMap().length * Textures.currentTrackSize)) - margin;

                for (PhysicalObject o : objects)
                    if (Math.abs(x - o.getX()) < 300 && Math.abs(y - o.getY()) < 300)
                        collides = true;

                try {
                    if (!maps[level].getMap()[(y + img.getHeight()) / Textures.currentTrackSize][(x + img.getWidth()) / Textures.currentTrackSize].equals(Textures.Image.EMPTY))
                        collides = true;
                }
                catch (ArrayIndexOutOfBoundsException e) { }
            }

            objects.add(new PhysicalObject(x, y, r.nextDouble() % (2 * Math.PI), img, bg));
        }
    }

    private void initializeMaps() {
        Textures.Image[][] temp = {{Textures.Image.DR, Textures.Image.H, Textures.Image.DL}, {Textures.Image.V, Textures.Image.EMPTY, Textures.Image.V}, {Textures.Image.UR, Textures.Image.H, Textures.Image.UL}};
        Point[] checkpoints = {new Point(0, 0), new Point(0, 2), new Point(2, 0), new Point(2, 2)};

        Textures.Image[][] temp1 = {{Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.DL},
                            {Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.UL, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.V},
                            {Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.UL, Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.DL, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.V},
                            {Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.UL, Textures.Image.DR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.UL, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.UR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.UL},
                            {Textures.Image.UR, Textures.Image.H, Textures.Image.H, Textures.Image.H, Textures.Image.DL, Textures.Image.EMPTY, Textures.Image.V, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY},
                            {Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.UR, Textures.Image.H, Textures.Image.UL, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY, Textures.Image.EMPTY}};
        Point[] checkpoints1 = {new Point(5, 3), new Point(10, 2), new Point(20, 1), new Point(23, 0), new Point(23, 3), new Point(14, 2),
            new Point(11, 2), new Point(6, 3), new Point(6, 5), new Point(4, 4), new Point(0, 4)};

        maps = new Map[]{new Map(false, 1, 2, 10, checkpoints, temp), new Map(true, 1, 3, 1, checkpoints1, temp1)};
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Hashtable<Player, Integer> getScores() {
        return scores;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Textures getTextures() {
        return textures;
    }

    public void setScores(Hashtable<Player, Integer> scores) {
        this.scores = scores;
    }
}
