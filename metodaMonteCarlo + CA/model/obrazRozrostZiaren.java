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
    private int licznik;
    private boolean granice;
    private boolean graniceDone;

    public obrazRozrostZiaren(Canvas canvas2D, modelRozrostZiaren model, GraphicsContext gc, kontrolerRozrostZiaren kontrolerRozrostZIaren, boolean granice) {
        this.canvas2D = canvas2D;
        this.model = model;
        this.gc = gc;
        running = true;
        paused = false;
        cgg = kontrolerRozrostZIaren;
        this.granice=granice;
        graniceDone=false;
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
                if(!granice) {
                    ziarno[][] tab = model.getResult(model.getGrid());
                    //stop kiedy caly obszar wypelniony ziarnami (nie dotyczy MonteCarlo)
                    if (getLiczbaPustychZiaren(tab) == 0 && ustawienia.typZarodkowania != modelRozrostZiaren.zarodkowanie.MonteCarlo) {
                        stop();
                        cgg.resetButtons();
                        model.setGridEnergia(model.getGrid());
                    }
                    //stop kiedy licznik osiagnie wartosc zadanej liczby krokow (MonteCarlo)
                    else if (ustawienia.typZarodkowania == modelRozrostZiaren.zarodkowanie.MonteCarlo && ustawienia.liczbaKrokowSymulacji == licznik) {
                        stop();
                        cgg.resetButtons();
                        model.setGridEnergia(model.getGrid());
                    //zwiększenie licznika (MonteCarlo)
                    } else if (ustawienia.typZarodkowania == modelRozrostZiaren.zarodkowanie.MonteCarlo) {
                        licznik++;
                        ustawienia.liczbaWykonanychKrokowSymulacji=licznik;
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
                }
                //rysowanie granic
                else if(!graniceDone){
                    ziarno[][] tab = model.getResult(model.getGrid());
                    cleanCanvas();
                    for (int i = 0; i < model.getGridWysokość(); i++) {
                        for (int j = 0; j < model.getGridSzerokość(); j++) {
                            if (tab[i][j].getEnergia()!=0) {
                                gc.setFill(Color.color(0, 1/tab[i][j].getEnergia(), 0));
                            }
                            else
                                gc.setFill(Color.color(0, 1, 1));
                            gc.fillRect(j * wysokość, i * szerokość, wysokość, szerokość);

                        }
                    }
                    graniceDone=true;
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
