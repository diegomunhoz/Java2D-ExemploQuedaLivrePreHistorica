package br.com.diegomunhoz.core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;

// Esta classe é a mesma do exemplo da semana 1.
// Veja os comentários pertinentes nos fontes daquela semana.
abstract public class Game implements WindowListener {

    private JFrame mainWindow;
    private boolean active;
    private BufferStrategy bufferStrategy;

    public Game() {
        mainWindow = new JFrame(
                "Abrindo o Jogo - Desenvolvimento de Jogos Digitais em Java");
        mainWindow.setSize(800, 600);
        mainWindow.addWindowListener(this);
        active = false;
    }

    public JFrame getMainWindow() {
        return mainWindow;
    }

    public void terminate() {
        active = false;
    }

    public void run() {
        active = true;
        load();
        while (active) {
            update();
            render();
        }
        unload();
    }

    public void load() {
        mainWindow.setUndecorated(true);
        mainWindow.setIgnoreRepaint(true);
        mainWindow.setLocation(100, 100);
        mainWindow.setVisible(true);
        mainWindow.createBufferStrategy(2);
        bufferStrategy = mainWindow.getBufferStrategy();
        onLoad();
    }

    public void unload() {
        onUnload();
        bufferStrategy.dispose();
        mainWindow.dispose();
    }

    public void update() {
        onUpdate();
        Thread.yield();
    }

    public void render() {
        Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
        onRender(g);
        g.dispose();
        bufferStrategy.show();
    }

    abstract public void onLoad();

    abstract public void onUnload();

    abstract public void onUpdate();

    abstract public void onRender(Graphics2D g);

    public int getWidth() {
        return mainWindow.getWidth();
    }

    public int getHeight() {
        return mainWindow.getHeight();
    }

    public void windowClosing(WindowEvent e) {
        terminate();
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
