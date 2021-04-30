package model;

import aplikacja.kontrolerRozrostZiaren;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.concurrent.TimeUnit;
import static model.ziarno.State;

public class obrazRozrostZiaren implements Runnable {
    private Canvas canvas2D;
    private modelRozrostZiaren model;
    private GraphicsContext gc;
    private volatile boolean running;
    private volatile boolean paused;
    private final Object pauseLock = new Object();
    private kontrolerRozrostZiaren cgg;

    public obrazRozrostZiaren(Canvas canvas2D, modelRozrostZiaren model, GraphicsContext gc, kontrolerRozrostZiaren kontrolerRozrostZIaren) {
        this.canvas2D = canvas2D;
        this.model = model;
        this.gc = gc;
        running = true;
        paused = false;
        cgg = kontrolerRozrostZIaren;
    }

    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        if (model != null) gc.fillRect(0, 0, model.getGridSzerokość() * ustawienia.wysokośćZiarna, model.getGridWysokość() * ustawienia.szerokośćZiarna);
        else gc.fillRect(0, 0, canvas2D.getHeight() * ustawienia.wysokośćZiarna, canvas2D.getWidth() * ustawienia.szerokośćZiarna);
    }

    private int getLiczbaPustychZiaren(ziarno[][] tab) {
        return model.getLiczbaPustychZiaren();
    }

    private void paint() {
        int wysokość = ustawienia.wysokośćZiarna;
        int szerokość = ustawienia.szerokośćZiarna;
        try {
            Platform.runLater(() -> {
                ziarno[][] tab = model.getResult(model.getGrid());

                if (getLiczbaPustychZiaren(tab) == 0) {
                    stop();
                    cgg.resetButtons();
                    model.setGridEnergy(model.getGrid());
                }
                cleanCanvas();

                for (int i = 0; i < model.getGridWysokość(); i++) {
                    for (int j = 0; j < model.getGridSzerokość(); j++) {
                        if (tab[i][j].getState() == State.ZIARNO) {
                            gc.setFill(((modelRozrostZiaren.typZiarna) model.getListaZiaren().get(tab[i][j].getId() - 1)).getKolorZiarna());
                            gc.fillRect(j * wysokość, i * szerokość, wysokość, szerokość);
                        }
                    }
                }
            });
            TimeUnit.MILLISECONDS.sleep(ustawienia.prędkośćAnimacji);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                if (!running)
                    break;
                if (paused) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running)
                        break;
                }
            }
            paint();
        }
    }

    public void stop() {
        running = false;
        resume();
    }


    public void pause() {
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }
}
