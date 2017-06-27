package com.company;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Hashtable;

/**
 * Created by Lenovo on 08.05.2017.
 */
public class Input implements KeyListener {
    public enum Control {
        ESC, MENU
    }

    private final int controlDelay = 40;       // ms

    private boolean fst = true, snd = true;
    private long controlTime = -controlDelay, oldControlTime;

    private Hashtable<String, Boolean> keys = new Hashtable<>();

    private Vehicle fstPlayer, sndPlayer;

    public Input(Vehicle fstPlayer) {
        this.fstPlayer = fstPlayer;
        initFst();
    }

    public Input(Vehicle fstPlayer, Vehicle sndPlayer) {
        this.fstPlayer = fstPlayer;
        this.sndPlayer = sndPlayer;

        keys.put(KeyEvent.getKeyText(KeyEvent.VK_ESCAPE), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_M), false);

        initFst();
        initSnd();
    }

    private void initFst() {
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_W), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_S), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_A), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_D), false);

        keys.put(KeyEvent.getKeyText(KeyEvent.VK_SPACE), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_SHIFT), false);

        keys.put(KeyEvent.getKeyText(KeyEvent.VK_R), false);
    }

    private void initSnd() {
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_UP), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_DOWN), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_LEFT), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_RIGHT), false);

        keys.put(KeyEvent.getKeyText(KeyEvent.VK_CONTROL), false);
        keys.put(KeyEvent.getKeyText(KeyEvent.VK_ENTER), false);

        keys.put(KeyEvent.getKeyText(KeyEvent.VK_SLASH), false);
    }

    private void checkFst() {
        if (keys.get("W"))
            fstPlayer.gas();
        if (keys.get("S"))
            fstPlayer.rev();

        if (keys.get("Space"))
            fstPlayer.handBrake();
        if (keys.get("Shift"))
            fstPlayer.nitro();

        if (keys.get("A"))
            fstPlayer.turn((byte) -1);
        if (keys.get("D"))
            fstPlayer.turn((byte) 1);

        if (keys.get("R"))
            fstPlayer.respawn();
    }

    private void checkSnd() {
        if (keys.get("Up"))
            sndPlayer.gas();
        if (keys.get("Down"))
            sndPlayer.rev();

        if (keys.get("Ctrl"))
            sndPlayer.handBrake();
        if (keys.get("Enter"))
            sndPlayer.nitro();

        if (keys.get("Left"))
            sndPlayer.turn((byte) -1);
        if (keys.get("Right"))
            sndPlayer.turn((byte) 1);

        if (keys.get("Slash"))
            sndPlayer.respawn();
    }

    public Control check() {        // returns clicked control key
        if (keys.getOrDefault("Escape", false)) {
            oldControlTime = controlTime;
            controlTime = System.currentTimeMillis();
            return (controlTime - oldControlTime) >= controlDelay ? Control.ESC : null;
        }
        if (keys.getOrDefault("M", false)) {
            oldControlTime = controlTime;
            controlTime = System.currentTimeMillis();
            return (controlTime - oldControlTime) >= controlDelay ? Control.MENU : null;
        }

        if (fst)
            checkFst();

        if (snd && sndPlayer != null)
            checkSnd();

        return null;
    }

    public void keyPressed(KeyEvent e) {
        keys.put(KeyEvent.getKeyText(e.getKeyCode()), true);
    }

    public void keyReleased(KeyEvent e) {
        keys.put(KeyEvent.getKeyText(e.getKeyCode()), false);
    }

    public void keyTyped(KeyEvent e) { }

    public void setFst(boolean fst) {
        this.fst = fst;
    }

    public void setSnd(boolean snd) {
        this.snd = snd;
    }
}
