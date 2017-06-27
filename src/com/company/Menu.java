package com.company;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by Lenovo on 18.05.2017.
 */
public class Menu extends JPanel {
    public enum Mode {
        MAIN, HS, DIFFICULTY, SAVE, LOAD
    }

    public enum Text {
        TITLE,

        SP, MP, HS, QUIT, BACK,
        SP_C, MP_C, HS_C, QUIT_C, BACK_C,        // clicked

        DIFFICULTY,

        EASY, MEDIUM, HARD, EXTREME,
        EASY_C, MEDIUM_C, HARD_C,

        SAVE_ICON, LOAD_ICON,

        SAVE, LOAD,

        SLOT0, SLOT1, SLOT2,
        SLOT0_C, SLOT1_C, SLOT2_C,

        PLAYER0, PLAYER1,
        CPU0, CPU1, CPU2
    }

    public class Input implements MouseListener {
        Point click = new Point();
        Point press = new Point();

        public void resetClick() {
            click = new Point();
        }

        public void resetPress() {
            press = new Point();
        }

        public void mouseEntered(MouseEvent e) {

        }

        public void mouseClicked(MouseEvent e) {
            click.setLocation(e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {

        }

        public void mouseExited(MouseEvent e) {

        }

        public void mousePressed(MouseEvent e) {
            press.setLocation(e.getX(), e.getY());
        }

        public Point getClick() {
            return click;
        }

        public Point getPress() {
            return press;
        }
    }

    private final int margin = 15, hsMargin = 200;      // for save and load images

    private boolean multi = false;
    private int x = 0, y = 0;       // bg coordinates

    private Mode mode = Mode.MAIN;

    private Text[] all = {Text.TITLE, Text.SP, Text.MP, Text.HS, Text.QUIT, Text.DIFFICULTY,
            Text.EASY, Text.MEDIUM, Text.HARD, Text.EXTREME, Text.BACK, Text.SAVE, Text.LOAD, Text.SLOT0, Text.SLOT1, Text.SLOT2};

    private Image bg;

    private LinkedList<Pair<Image, Point>> hs = new LinkedList<>();

    private LinkedList<Pair<Text, Point>> hsPnts = new LinkedList<>();
    private LinkedList<Pair<Text, Point>> serializationPnts = new LinkedList<>();

    private Hashtable<Mode, Text[]> modeToTextTable = new Hashtable<>();
    private Hashtable<Text, Text> toDraw = new Hashtable<>();
    private Hashtable<Text, Text> clicked = new Hashtable<>();
    private Hashtable<Text, Integer> yOffsets = new Hashtable<>();

    private Hashtable<Text, Image> texts = new Hashtable<>();
    private Hashtable<Integer, Image> digits = new Hashtable<>();

    private Input input = new Input();

    private Game game;
    private Serialization serialization;

    public Menu(Game game) {
        this.game = game;
        serialization = new Serialization(game.getScores(), this);
        bg = game.getTextures().getImagesE().get(Textures.Image.BG);

        initializeClicked();
        initializeYOffsets();
        initializeModeToTextTable();

        try {
            initializeImages();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        initializeHsPoints();
        initializeSerializationPoints();

        this.addMouseListener(input);
    }

    public void update() {
        resetToDraw();
        updateToDraw(inBounds(input.getPress()));
        action(inBounds(input.getClick()));
        updateCoordinates();
    }

    private void initializeModeToTextTable() {
        modeToTextTable.put(Mode.MAIN, new Text[]{Text.TITLE, Text.SP, Text.MP, Text.HS, Text.QUIT});
        modeToTextTable.put(Mode.HS, new Text[]{Text.BACK});
        modeToTextTable.put(Mode.DIFFICULTY, new Text[]{Text.DIFFICULTY, Text.EASY, Text.MEDIUM, Text.HARD, Text.EXTREME});
        modeToTextTable.put(Mode.SAVE, new Text[]{Text.SAVE, Text.SLOT0, Text.SLOT1, Text.SLOT2, Text.BACK});
        modeToTextTable.put(Mode.LOAD, new Text[]{Text.LOAD, Text.SLOT0, Text.SLOT1, Text.SLOT2, Text.BACK});
    }

    private void initializeHsPoints() {
        hsPnts.add(new Pair<>(Text.PLAYER0, new Point(hsMargin, 50)));
        hsPnts.add(new Pair<>(Text.PLAYER1, new Point(hsMargin, 140)));

        hsPnts.add(new Pair<>(Text.CPU0, new Point(hsMargin, 230)));
        hsPnts.add(new Pair<>(Text.CPU1, new Point(hsMargin, 320)));
        hsPnts.add(new Pair<>(Text.CPU2, new Point(hsMargin, 410)));
    }

    private void initializeSerializationPoints() {
        Image load = texts.get(Text.LOAD_ICON);

        serializationPnts.add(new Pair<>(Text.SAVE_ICON,
                new Point(margin, game.y() - texts.get(Text.SAVE_ICON).getHeight(null) - margin)));
        serializationPnts.add(new Pair<>(Text.LOAD_ICON,
                new Point(game.x() - load.getWidth(null) - margin, game.y() - load.getHeight(null) - margin)));
    }

    private void initializeClicked() {
        clicked.put(Text.SP, Text.SP_C);
        clicked.put(Text.MP, Text.MP_C);
        clicked.put(Text.HS, Text.HS_C);
        clicked.put(Text.QUIT, Text.QUIT_C);

        clicked.put(Text.BACK, Text.BACK_C);

        clicked.put(Text.EASY, Text.EASY_C);
        clicked.put(Text.MEDIUM, Text.MEDIUM_C);
        clicked.put(Text.HARD, Text.HARD_C);

        clicked.put(Text.SLOT0, Text.SLOT0_C);
        clicked.put(Text.SLOT1, Text.SLOT1_C);
        clicked.put(Text.SLOT2, Text.SLOT2_C);
    }

    public void resetMode() {
        mode = Mode.MAIN;
    }

    public void reset() {
        input.resetClick();
        input.resetPress();
    }

    private void action(Text t) {
        if (t != null)
            reset();

        if (t == Text.SP) {
            multi = false;
            mode = Mode.DIFFICULTY;
        }
        else if (t == Text.MP) {
            multi = true;
            mode = Mode.DIFFICULTY;
        }
        else if (t == Text.HS) {
            initializeHs();
            mode = Mode.HS;
        }
        else if (t == Text.QUIT)
            game.quit();
        else if (t == Text.BACK)
            mode = Mode.MAIN;
        else if (t == Text.EASY)
            game.newGame(multi, 0);
        else if (t == Text.MEDIUM)
            game.newGame(multi, 1);
        else if (t == Text.HARD)
            game.newGame(multi, 2);
        else if (t == Text.EXTREME)
            game.newGame(multi, 3);
        else if (t == Text.SAVE_ICON)
            mode = Mode.SAVE;
        else if (t == Text.LOAD_ICON)
            mode = Mode.LOAD;
        else if (t == Text.SLOT0 || t == Text.SLOT1 || t == Text.SLOT2)
            serialization.action(mode, t);
    }

    private Text inBounds(Point p) {
        for (Text t : modeToTextTable.get(mode)) {
            Image temp = texts.get(t);
            int margin = (game.x() - temp.getWidth(null)) / 2;

            if (p.x >= margin && p.x < game.x() - margin && p.y >= yOffsets.get(t) && p.y < yOffsets.get(t) + temp.getHeight(null))
                return t;
        }

        if (mode != Mode.MAIN)
            return null;

        for (Pair<Text, Point> pair : serializationPnts) {
            Text t = pair.getKey();
            Point point = pair.getValue();
            Image image = texts.get(t);

            if (p.x >= point.x && p.x < point.x + image.getWidth(null) && p.y >= point.y && p.y < point.y + image.getHeight(null))
                return t;
        }

        return null;
    }

    private void resetToDraw() {
        for (Text t : all)
            if (toDraw.get(t) != t)
                toDraw.put(t, t);
    }

    private void updateToDraw(Text t) {
        if (t != null)
            toDraw.put(t, clicked.getOrDefault(t, t));
    }

    public void paint(Graphics g) {
        g.drawImage(bg, x, y, null);
        draw(g, modeToTextTable.get(mode));
    }

    private void draw(Graphics g, Text[] textsTable) {
        for (Text t : textsTable) {
            Image temp = texts.get(toDraw.get(t));
            g.drawImage(temp, (game.x() - temp.getWidth(null)) / 2, yOffsets.get(t), null);
        }

        if (mode == Mode.MAIN)
            for (Pair<Text, Point> p : serializationPnts) {
                Point point = p.getValue();
                g.drawImage(texts.get(p.getKey()), point.x, point.y, null);
            }
        else if (mode == Mode.HS) {
            for (Pair<Image, Point> pair : hs) {
                Point p = pair.getValue();
                g.drawImage(pair.getKey(), p.x, p.y, null);
            }

            for (Pair<Text, Point> p : hsPnts) {
                Text t = p.getKey();
                Image temp = texts.get(t);
                Point point = p.getValue();
                g.drawImage(temp, point.x, point.y, null);
            }
        }
    }

    private void initializeYOffsets() {
        yOffsets.put(Text.TITLE, 20);

        yOffsets.put(Text.SP, 170);
        yOffsets.put(Text.MP, 270);
        yOffsets.put(Text.HS, 370);
        yOffsets.put(Text.QUIT, 470);

        yOffsets.put(Text.BACK, 470);

        yOffsets.put(Text.SP_C, 170);
        yOffsets.put(Text.MP_C, 270);
        yOffsets.put(Text.HS_C, 370);
        yOffsets.put(Text.QUIT_C, 470);

        yOffsets.put(Text.BACK_C, 470);

        yOffsets.put(Text.DIFFICULTY, 20);

        yOffsets.put(Text.EASY, 170);
        yOffsets.put(Text.MEDIUM, 270);
        yOffsets.put(Text.HARD, 370);
        yOffsets.put(Text.EXTREME, 470);

        yOffsets.put(Text.EASY_C, 170);
        yOffsets.put(Text.MEDIUM_C, 270);
        yOffsets.put(Text.HARD_C, 370);

        yOffsets.put(Text.SAVE, 20);
        yOffsets.put(Text.LOAD, 20);

        yOffsets.put(Text.SLOT0, 170);
        yOffsets.put(Text.SLOT1, 270);
        yOffsets.put(Text.SLOT2, 370);

        yOffsets.put(Text.SLOT0_C, 170);
        yOffsets.put(Text.SLOT1_C, 270);
        yOffsets.put(Text.SLOT2_C, 370);
    }

    private LinkedList<Image> number(int n) {
        LinkedList<Image> images = new LinkedList<>();

        if (n == 0) {
            images.add(digits.get(0));
            return images;
        }

        while (n != 0) {
            images.add(digits.get(n % 10));
            n /= 10;
        }

        return images;
    }

    private void initializeHs() {
        int y = 50;
        Hashtable<Game.Player, Integer> scores = game.getScores();
        hs = new LinkedList<>();

        for (Game.Player p : game.getPlayers()) {
            int score = scores.getOrDefault(p, 0);
            LinkedList<Image> numbers = number(score);
            int x = game.x() - hsMargin - digits.get(score % 10).getWidth(null);

            for (Image i : numbers) {
                hs.add(new Pair<>(i, new Point(x, y)));
                x -= i.getWidth(null);
            }

            y += 90;
        }
    }

    public void updateCoordinates() {
        if (x > game.x() - bg.getWidth(null) && y == 0)
            x--;
        else if (x == game.x() - bg.getWidth(null) && y > game.y() - bg.getHeight(null))
            y--;
        else if (x < 0 && y == game.y() - bg.getHeight(null))
            x++;
        else
            y++;
    }

    private void initializeImages() throws Exception {
        bg = ImageIO.read(new File("res/menu/trackM.png"));
        texts.put(Text.SAVE_ICON, ImageIO.read(new File("res/serialization/save.png")));
        texts.put(Text.LOAD_ICON, ImageIO.read(new File("res/serialization/load.png")));

        texts.put(Text.TITLE, ImageIO.read(new File("res/menu/title.png")));

        texts.put(Text.SP, ImageIO.read(new File("res/menu/sp.png")));
        texts.put(Text.MP, ImageIO.read(new File("res/menu/mp.png")));
        texts.put(Text.HS, ImageIO.read(new File("res/menu/hs.png")));
        texts.put(Text.QUIT, ImageIO.read(new File("res/menu/quit.png")));
        texts.put(Text.BACK, ImageIO.read(new File("res/menu/back.png")));

        texts.put(Text.SP_C, ImageIO.read(new File("res/menu/spC.png")));
        texts.put(Text.MP_C, ImageIO.read(new File("res/menu/mpC.png")));
        texts.put(Text.HS_C, ImageIO.read(new File("res/menu/hsC.png")));
        texts.put(Text.QUIT_C, ImageIO.read(new File("res/menu/quitC.png")));
        texts.put(Text.BACK_C, ImageIO.read(new File("res/menu/backC.png")));

        digits.put(0, ImageIO.read(new File("res/menu/digits/0.png")));
        digits.put(1, ImageIO.read(new File("res/menu/digits/1.png")));
        digits.put(2, ImageIO.read(new File("res/menu/digits/2.png")));
        digits.put(3, ImageIO.read(new File("res/menu/digits/3.png")));
        digits.put(4, ImageIO.read(new File("res/menu/digits/4.png")));
        digits.put(5, ImageIO.read(new File("res/menu/digits/5.png")));
        digits.put(6, ImageIO.read(new File("res/menu/digits/6.png")));
        digits.put(7, ImageIO.read(new File("res/menu/digits/7.png")));
        digits.put(8, ImageIO.read(new File("res/menu/digits/8.png")));
        digits.put(9, ImageIO.read(new File("res/menu/digits/9.png")));

        texts.put(Text.DIFFICULTY, ImageIO.read(new File("res/menu/difficulties/difficulty.png")));

        texts.put(Text.EASY, ImageIO.read(new File("res/menu/difficulties/easy.png")));
        texts.put(Text.MEDIUM, ImageIO.read(new File("res/menu/difficulties/medium.png")));
        texts.put(Text.HARD, ImageIO.read(new File("res/menu/difficulties/hard.png")));
        texts.put(Text.EXTREME, ImageIO.read(new File("res/menu/difficulties/extreme.png")));

        texts.put(Text.EASY_C, ImageIO.read(new File("res/menu/difficulties/easyC.png")));
        texts.put(Text.MEDIUM_C, ImageIO.read(new File("res/menu/difficulties/mediumC.png")));
        texts.put(Text.HARD_C, ImageIO.read(new File("res/menu/difficulties/hardC.png")));

        texts.put(Text.SAVE, ImageIO.read(new File("res/menu/serialization/save.png")));
        texts.put(Text.LOAD, ImageIO.read(new File("res/menu/serialization/load.png")));

        texts.put(Text.SLOT0, ImageIO.read(new File("res/menu/serialization/slot0.png")));
        texts.put(Text.SLOT1, ImageIO.read(new File("res/menu/serialization/slot1.png")));
        texts.put(Text.SLOT2, ImageIO.read(new File("res/menu/serialization/slot2.png")));

        texts.put(Text.SLOT0_C, ImageIO.read(new File("res/menu/serialization/slot0C.png")));
        texts.put(Text.SLOT1_C, ImageIO.read(new File("res/menu/serialization/slot1C.png")));
        texts.put(Text.SLOT2_C, ImageIO.read(new File("res/menu/serialization/slot2C.png")));

        texts.put(Text.PLAYER0, ImageIO.read(new File("res/menu/highscores/player0.png")));
        texts.put(Text.PLAYER1, ImageIO.read(new File("res/menu/highscores/player1.png")));

        texts.put(Text.CPU0, ImageIO.read(new File("res/menu/highscores/cpu0.png")));
        texts.put(Text.CPU1, ImageIO.read(new File("res/menu/highscores/cpu1.png")));
        texts.put(Text.CPU2, ImageIO.read(new File("res/menu/highscores/cpu2.png")));
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Game getGame() {
        return game;
    }
}
