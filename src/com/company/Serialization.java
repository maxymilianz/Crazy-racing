package com.company;

import java.io.*;
import java.util.Hashtable;

/**
 * Created by Lenovo on 18.05.2017.
 */
public class Serialization {
    private Hashtable<Game.Player, Integer> scores;
    private Menu menu;

    public Serialization(Hashtable<Game.Player, Integer> scores, Menu menu) {
        this.scores = scores;
        this.menu = menu;
    }

    public void action(Menu.Mode mode, Menu.Text filename) {
        try {
            if (mode == Menu.Mode.SAVE)
                serialize(filename);
            else
                deserialize(filename);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void serialize(Menu.Text filename) throws Exception {
        FileOutputStream fileOut = new FileOutputStream(filename.toString());
        ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
        objOut.writeObject(scores);
        objOut.close();
        fileOut.close();
    }

    private void deserialize(Menu.Text filename) throws Exception {
        File file = new File(filename.toString());

        if (file.exists()) {
            FileInputStream fileIn = new FileInputStream(filename.toString());
            ObjectInputStream objIn = new ObjectInputStream(fileIn);
            scores = (Hashtable<Game.Player, Integer>) objIn.readObject();
            objIn.close();
            fileIn.close();
            menu.getGame().setScores(scores);
        }
    }
}
