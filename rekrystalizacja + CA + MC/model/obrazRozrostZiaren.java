package model;

import aplikacja.kontrolerRozrostZiaren;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.concurrent.TimeUnit;
import static model.ziarno.State;
import static model.ziarno.StateRekrystalizacja.ZREKRYSTALIZOWANY;

public class obrazRozrostZiaren implements Runnable {
    private Canvas canvas2D;
    private modelRozrostZiaren model;
    private GraphicsContext gc;
    private volatile boolean running;
    private volatile boolean paused;
    private final Object pauseLock = new Object();
    private kontrolerRozrostZiaren cgg;
    private int licznik;
    private boolean granice, dyslokacje;
    private boolean graniceDone, dyslokacjeDone;

    public obrazRozrostZiaren(Canvas canvas2D, modelRozrostZiaren model, GraphicsContext gc, kontrolerRozrostZiaren kontrolerRozrostZIaren, boolean granice, boolean dyslokacje) {
        this.canvas2D = canvas2D;
        this.model = model;
        this.gc = gc;
        running = true;
        paused = false;
        cgg = kontrolerRozrostZIaren;
        this.granice=granice;
        graniceDone=false;
        this.dyslokacje=dyslokacje;
        dyslokacjeDone=false;
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
                if(!granice && !dyslokacje) {
                    ziarno[][] tab;
                    tab = model.getResult(model.getGrid());
                        //stop dla zarodkowania poza MC i DRX
                        if (getLiczbaPustychZiaren(tab) == 0 && ustawienia.typZarodkowania != modelRozrostZiaren.zarodkowanie.MonteCarlo&& ustawienia.typZarodkowania != modelRozrostZiaren.zarodkowanie.rekrystalizacja) {
                            stop();
                            cgg.resetButtons();
                            model.setGridEnergia(model.getGrid());
                        }
                        //stop dla MC
                        else if (ustawienia.typZarodkowania == modelRozrostZiaren.zarodkowanie.MonteCarlo && ustawienia.liczbaKrokowSymulacji == licznik) {
                            stop();
                            cgg.resetButtons();
                            model.setGridEnergia(model.getGrid());
                        }
                        //zwiększenie licznika (MonteCarlo)
                        else if (ustawienia.typZarodkowania == modelRozrostZiaren.zarodkowanie.MonteCarlo) {
                            licznik++;
                            ustawienia.liczbaWykonanychKrokowSymulacji = licznik;
                        }
                        //stop dla DRX
                        else if (ustawienia.typZarodkowania== modelRozrostZiaren.zarodkowanie.rekrystalizacja && model.aktualnyCzas>=ustawienia.limitCzasowy) {
                            stop();
                            cgg.resetButtons();
                            model.setGridEnergia(model.getGrid());
                        }
                        //zwiekszanie czasu DRX
                        else if(ustawienia.typZarodkowania== modelRozrostZiaren.zarodkowanie.rekrystalizacja){
                        model.aktualnyCzas+=ustawienia.krokCzasowy;
                        System.out.println(model.aktualnyCzas);
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
                else if(granice && !graniceDone){
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

                else if(dyslokacje && !dyslokacjeDone){
                    ziarno[][] tab = model.getResult(model.getGrid());
                    cleanCanvas();
                    for (int i = 0; i < model.getGridWysokość(); i++) {
                        for (int j = 0; j < model.getGridSzerokość(); j++) {
                            obliczLiczbęZrekrystalizowanychSąsiadów (tab, i , j);
                        }
                    }
                    for (int i = 0; i < model.getGridWysokość(); i++) {
                        for (int j = 0; j < model.getGridSzerokość(); j++) {
                            if (tab[i][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY) {
                                double kolor = 0.2;
                                for (int a = 0; a < tab[i][j].liczbaZrekrystalizowanychSasiadow; a++)
                                    kolor+=0.2;
                                gc.setFill(Color.color(0, kolor, 0));
                            }
                            else
                                gc.setFill(Color.color(0, 1, 1));
                            gc.fillRect(j * wysokość, i * szerokość, wysokość, szerokość);

                        }
                    }
                    dyslokacjeDone=true;
                }

            });
            TimeUnit.MILLISECONDS.sleep(ustawienia.prędkośćAnimacji);
        } catch (InterruptedException ignored) {
        }
    }

    private void obliczLiczbęZrekrystalizowanychSąsiadów( ziarno[][] tab, int i, int j){
        tab[i][j].liczbaZrekrystalizowanychSasiadow=0;
        int iplusjeden = i+1, jplusjeden = j+1, iminusjeden = i-1, jminusjeden = j-1;
        if(i == 0)
            iminusjeden=0;
        else if (i==model.getGridWysokość()-1)
            iplusjeden=model.getGridWysokość()-1;
        if(j == 0)
            jminusjeden=0;
        else if (j==model.getGridSzerokość()-1)
            jplusjeden= model.getGridSzerokość()-1;
        if (tab[iplusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            tab[i][j].liczbaZrekrystalizowanychSasiadow+=1;
        }
        if(tab[i][jplusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            tab[i][j].liczbaZrekrystalizowanychSasiadow+=1;
        }
        if(tab[iminusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            tab[i][j].liczbaZrekrystalizowanychSasiadow+=1;
        }
        if(tab[i][jminusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            tab[i][j].liczbaZrekrystalizowanychSasiadow+=1;
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
