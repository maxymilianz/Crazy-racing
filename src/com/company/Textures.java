package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

/**
 * Created by Lenovo on 16.05.2017.
 */
public class Textures {
    public enum Image {
        EMPTY,

        ARROW, MARKER,

        GRASS, SAND, SNOW, WATER,       // bgs

        DL, DR, H, UL, UR, V,       // track

        RED, GREEN, BLUE, YELLOW,       // cars

        DT, DT2, DT3,       // deadTrees
        F, F2, F3, F4, F5, F6, F7, F8, F9,      // foliage
        P, P2, P3, P4, P5, P6,      // palmtrees
        T, T2, T3, T4, T5, T6, T7, T8, T9,      // trees
        TS,      // treeStrange

        PAUSE, PAUSE_MULTI,

        BG,      // menu bg

        DROWNED, DROWNED_M, GAME, LEVEL     // finished
    }

    public static int currentBgSize, currentTrackSize;

    private Image[] bgsE = {Image.GRASS, Image.SAND, Image.SNOW, Image.WATER};

    private Hashtable<Boolean, Hashtable<Integer, java.awt.Image>> ends = new Hashtable<>();
    private Hashtable<Image, Image[]> treesE = new Hashtable<>();
    private Hashtable<Image, BufferedImage> imagesE = new Hashtable<>();

    public Textures() {
        try {
            initializeImages();
            initializeEnds();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        initializeTrees();
    }

    private void initializeEnds() throws Exception {
        Hashtable<Integer, java.awt.Image> temp0 = new Hashtable<>();
        Hashtable<Integer, java.awt.Image> temp1 = new Hashtable<>();

        temp0.put(1, ImageIO.read(new File("res/menu/end/1st.png")));
        temp0.put(2, ImageIO.read(new File("res/menu/end/2nd.png")));
        temp0.put(3, ImageIO.read(new File("res/menu/end/3rd.png")));
        temp0.put(4, ImageIO.read(new File("res/menu/end/4th.png")));

        temp1.put(1, ImageIO.read(new File("res/menu/end/1stM.png")));
        temp1.put(2, ImageIO.read(new File("res/menu/end/2ndM.png")));
        temp1.put(3, ImageIO.read(new File("res/menu/end/3rdM.png")));
        temp1.put(4, ImageIO.read(new File("res/menu/end/4thM.png")));

        ends.put(false, temp0);
        ends.put(true, temp1);
    }

    private void initializeTrees() {
        Image[] grassyE = {Image.F, Image.F2, Image.F3, Image.F4, Image.F5, Image.F6, Image.F7, Image.F8, Image.F9};        // slow down
        treesE.put(Image.GRASS, grassyE);

        Image[] sandyE = {Image.DT, Image.DT2, Image.DT3, Image.P, Image.P2, Image.P3, Image.P4, Image.P5, Image.P6, Image.TS};     // stop at center
        treesE.put(Image.SAND, sandyE);

        Image[] snowyE = {Image.T, Image.T2, Image.T3, Image.T4, Image.T5, Image.T6, Image.T7, Image.T8, Image.T9};     // stop at bounds
        treesE.put(Image.SNOW, snowyE);
    }

    private void initializeImages() throws Exception {
        imagesE.put(Image.ARROW, ImageIO.read(new File("res/arrow.png")));      // arrow
        imagesE.put(Image.MARKER, ImageIO.read(new File("res/marker.png")));        // marker

        imagesE.put(Image.GRASS, ImageIO.read(new File("res/bgs/grass.png")));       // bgs
        imagesE.put(Image.SAND, ImageIO.read(new File("res/bgs/sand.png")));
        imagesE.put(Image.SNOW, ImageIO.read(new File("res/bgs/snow.png")));
        imagesE.put(Image.WATER, ImageIO.read(new File("res/bgs/water.png")));

        imagesE.put(Image.DL, ImageIO.read(new File("res/track/DL.png")));      // track
        imagesE.put(Image.DR, ImageIO.read(new File("res/track/DR.png")));
        imagesE.put(Image.H, ImageIO.read(new File("res/track/H.png")));
        imagesE.put(Image.UL, ImageIO.read(new File("res/track/UL.png")));
        imagesE.put(Image.UR, ImageIO.read(new File("res/track/UR.png")));
        imagesE.put(Image.V, ImageIO.read(new File("res/track/V.png")));

        imagesE.put(Image.RED, ImageIO.read(new File("res/cars/red.png")));      // cars
        imagesE.put(Image.GREEN, ImageIO.read(new File("res/cars/green.png")));
        imagesE.put(Image.BLUE, ImageIO.read(new File("res/cars/blue.png")));
        imagesE.put(Image.YELLOW, ImageIO.read(new File("res/cars/yellow.png")));

        imagesE.put(Image.DT, ImageIO.read(new File("res/trees/deadtree1_0.png")));        // trees
        imagesE.put(Image.DT2, ImageIO.read(new File("res/trees/deadtree2.png")));
        imagesE.put(Image.DT3, ImageIO.read(new File("res/trees/deadtree3.png")));
        imagesE.put(Image.F, ImageIO.read(new File("res/trees/foliage1.png")));
        imagesE.put(Image.F2, ImageIO.read(new File("res/trees/foliage2_0.png")));
        imagesE.put(Image.F3, ImageIO.read(new File("res/trees/foliage3_0.png")));
        imagesE.put(Image.F4, ImageIO.read(new File("res/trees/foliage4.png")));
        imagesE.put(Image.F5, ImageIO.read(new File("res/trees/foliage5.png")));
        imagesE.put(Image.F6, ImageIO.read(new File("res/trees/foliage6.png")));
        imagesE.put(Image.F7, ImageIO.read(new File("res/trees/foliage7.png")));
        imagesE.put(Image.F8, ImageIO.read(new File("res/trees/foliage8.png")));
        imagesE.put(Image.F9, ImageIO.read(new File("res/trees/foliage9.png")));
        imagesE.put(Image.P, ImageIO.read(new File("res/trees/palmtree1.png")));
        imagesE.put(Image.P2, ImageIO.read(new File("res/trees/palmtree2.png")));
        imagesE.put(Image.P3, ImageIO.read(new File("res/trees/palmtree3.png")));
        imagesE.put(Image.P4, ImageIO.read(new File("res/trees/palmtree4.png")));
        imagesE.put(Image.P5, ImageIO.read(new File("res/trees/palmtree5.png")));
        imagesE.put(Image.P6, ImageIO.read(new File("res/trees/palmtree6.png")));
        imagesE.put(Image.T, ImageIO.read(new File("res/trees/tree1_4.png")));
        imagesE.put(Image.T2, ImageIO.read(new File("res/trees/tree2_1.png")));
        imagesE.put(Image.T3, ImageIO.read(new File("res/trees/tree3.png")));
        imagesE.put(Image.T4, ImageIO.read(new File("res/trees/tree4.png")));
        imagesE.put(Image.T5, ImageIO.read(new File("res/trees/tree5.png")));
        imagesE.put(Image.T6, ImageIO.read(new File("res/trees/tree6.png")));
        imagesE.put(Image.T7, ImageIO.read(new File("res/trees/tree7.png")));
        imagesE.put(Image.T8, ImageIO.read(new File("res/trees/tree8.png")));
        imagesE.put(Image.T9, ImageIO.read(new File("res/trees/tree9.png")));
        imagesE.put(Image.TS, ImageIO.read(new File("res/trees/tree-strange.png")));

        imagesE.put(Image.PAUSE, ImageIO.read(new File("res/menu/pause.png")));
        imagesE.put(Image.PAUSE_MULTI, ImageIO.read(new File("res/menu/pauseMulti.png")));

        imagesE.put(Image.BG, ImageIO.read(new File("res/menu/trackM.png")));

        imagesE.put(Image.DROWNED, ImageIO.read(new File("res/menu/end/drowned.png")));
        imagesE.put(Image.DROWNED_M, ImageIO.read(new File("res/menu/end/drownedM.png")));
        imagesE.put(Image.GAME, ImageIO.read(new File("res/menu/end/game.png")));
        imagesE.put(Image.LEVEL, ImageIO.read(new File("res/menu/end/level.png")));
    }

    public Image[] getBgsE() {
        return bgsE;
    }

    public Hashtable<Image, BufferedImage> getImagesE() {
        return imagesE;
    }

    public Hashtable<Image, Image[]> getTreesE() {
        return treesE;
    }

    public Hashtable<Boolean, Hashtable<Integer, java.awt.Image>> getEnds() {
        return ends;
    }
}
