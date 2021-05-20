package model;

import java.util.*;

import aplikacja.kontrolerRozrostZiaren;
import javafx.scene.paint.Color;

import static model.modelRozrostZiaren.warunkiBrzegowe.*;
import static model.modelRozrostZiaren.zarodkowanie.*;
import static model.ziarno.State.*;
import static model.ziarno.StateRekrystalizacja.*;
import static model.ziarno.Dostępność.*;

public class modelRozrostZiaren {
    private ziarno[][] grid;
    private int gridWysokość, gridSzerokość;
    private typSąsiedztwa typSąsiedztwa;
    private warunkiBrzegowe warunkiBrzegowe;
    private zarodkowanie zarodkowanie;
    private List<typZiarna> listaZiaren;
    private int liczbaPustychZiaren;
    private int liczbaDostępnychZiaren;
    private double pulaDyslokacji;
    private double poprzedniaPulaDyslokacji;
    public int aktualnyCzas;
    public List <Double> listaGestosciDyslokacji;

    public enum warunkiBrzegowe {
        Absorbujące, Periodyczne
    }

    public enum typSąsiedztwa {
        vonNeuman, Moore, pentagonalneLosowe, hexagonalneLosowe
    }

    public enum zarodkowanie {
        losowe, jednorodne, zPromieniem, MonteCarlo, rekrystalizacja
    }

    public class typZiarna {
        Color kolorZiarna;

        public typZiarna(double r, double g, double b) {
            kolorZiarna = Color.color(r, g, b);
        }

        public Color getKolorZiarna() {
            return kolorZiarna;
        }
    }

    private class Współrzędne {
        public int x, y;

        public Współrzędne(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public modelRozrostZiaren(int wysokość, int szerokość, typSąsiedztwa typ, warunkiBrzegowe warunkiBrzegowe) {
        typSąsiedztwa = typ;
        this.warunkiBrzegowe = warunkiBrzegowe;
        gridWysokość = wysokość;
        gridSzerokość = szerokość;
        stwórzGrid();
        listaZiaren = new ArrayList<>();
        aktualnyCzas = 0;
        pulaDyslokacji = 0;
        listaGestosciDyslokacji = new ArrayList<>();
    }

    public void stwórzGrid() {
        grid = new ziarno[gridWysokość][gridSzerokość];
        liczbaPustychZiaren = gridWysokość * gridSzerokość;
        liczbaDostępnychZiaren = gridWysokość * gridSzerokość;

        for (int i = 0; i < gridWysokość; i++)
            for (int j = 0; j < gridSzerokość; j++)
                grid[i][j] = new ziarno();
    }

    public void setZarodkowanie(zarodkowanie zarodkowanie) {
        this.zarodkowanie = zarodkowanie;
    }

    public void setTypSąsiedztwa(typSąsiedztwa typSąsiedztwa) {
        this.typSąsiedztwa = typSąsiedztwa;
    }

    private void setPromień(int x, int y, int promień) {
        Współrzędne startCell = new Współrzędne(x, y);
        startCell = getLewo(startCell.x, startCell.y, promień);
        startCell = getGóra(startCell.x, startCell.y, promień);

        Współrzędne tmpCell = new Współrzędne(startCell.x, startCell.y);
        grid[tmpCell.x][tmpCell.y].setDostępność(NIEDOSTĘPNE);

        for (int i = 0; i < 2 * promień + 1; i++) {
            for (int j = 0; j < 2 * promień + 1; j++) {
                tmpCell = getPrawo(tmpCell.x, tmpCell.y, 1);
                if (grid[tmpCell.x][tmpCell.y].getDostępność() == DOSTĘPNE)
                    liczbaDostępnychZiaren--;
                grid[tmpCell.x][tmpCell.y].setDostępność(NIEDOSTĘPNE);
            }
            tmpCell = getLewo(tmpCell.x, tmpCell.y, 2 * promień + 1);
            tmpCell = getDół(tmpCell.x, tmpCell.y, 1);
            grid[tmpCell.x][tmpCell.y].setDostępność(NIEDOSTĘPNE);
        }
    }

    public void setAktualnyCzas(int aktualnyCzas) {
        this.aktualnyCzas = aktualnyCzas;
    }

    private void wypełnijMapę(int id, Map<Integer, Integer> mapaZiaren) {
        if (mapaZiaren.containsKey(id)) mapaZiaren.put(id, mapaZiaren.get(id) + 1);
        else mapaZiaren.put(id, 1);
    }

    //w losowych miejscach
    public int wypełnijLosowo(int liczbaZiaren) {
        Random rand = new Random();
        int counter = 0, counterBreak = 0, limitBreak = 1000;

        int i = 0, limiter = liczbaZiaren;
        if (listaZiaren.size() > 0) {
            limiter = listaZiaren.size() + liczbaZiaren;
            i = listaZiaren.size();
        }

        for (; i < limiter; i++) {
            if (getLiczbaPustychZiaren() == 0)
                break;

            listaZiaren.add(new typZiarna(rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5));
            int x = rand.nextInt(gridWysokość);
            int y = rand.nextInt(gridSzerokość);
            if (grid[x][y].getState() != ZIARNO) {
                counter++;
                grid[x][y].setState(ZIARNO);
                grid[x][y].setId(i + 1);
                liczbaPustychZiaren--;
            } else {
                i--;
                counterBreak++;
            }
            if (counterBreak == limitBreak)
                break;
        }
        return counter;
    }

    //co stałą odległość
    public int wypełnijJednorodnie(int liczbaZiaren, int odległość) {
        reset();
        Random rand = new Random();
        int maxOnHeight = (int) Math.ceil(gridWysokość / (odległość + 1.0));// - 1;
        int maxOnWidth = (int) Math.ceil(gridSzerokość / (odległość + 1.0));// - 1;
        int maxGrainNumber = maxOnHeight * maxOnWidth;
        int x = 0, y = 0;
        int counter = 1, i = 0;

        for (; i < liczbaZiaren; i++) {
            if (getLiczbaPustychZiaren() == 0 || i == maxGrainNumber)
                break;
            listaZiaren.add(new typZiarna(rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5));

            grid[x][y].setState(ZIARNO);
            grid[x][y].setId(i + 1);
            liczbaPustychZiaren--;

            x += odległość + 1;
            counter++;
            if (counter > maxOnHeight) {
                counter = 1;
                x = 0;
                y += odległość + 1;
            }
        }
        return i;
    }

    //w losowych miejscach, ale nie w promieniu innego ziarna
    public int wypełnijZPromieniem(int liczbaZiaren, int promień) {
        reset();
        Random rand = new Random();
        int i = 0, counter = 0, limit = 1000;

        for (; i < liczbaZiaren; i++) {
            if (getLiczbaDostępnychZiaren() == 0)
                break;

            int x = rand.nextInt(gridWysokość);
            int y = rand.nextInt(gridSzerokość);

            if (grid[x][y].getDostępność() == DOSTĘPNE) {
                listaZiaren.add(new typZiarna(rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5));
                grid[x][y].setState(ZIARNO);
                grid[x][y].setDostępność(NIEDOSTĘPNE);
                grid[x][y].setId(i + 1);
                setPromień(x, y, promień);
            } else {
                i--;
                if (++counter == limit)
                    break;
            }
        }
        return i;
    }

    //w miejscach, w których zostało kliknięte na obrazie
    public void dodajZiarno(int x, int y) {
        Random rand = new Random();

        if (grid[x][y].getState() == PUSTE) {
            int i = 0;
            if (listaZiaren.size() > 0)
                i = listaZiaren.size();

            listaZiaren.add(new typZiarna(rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5));
            grid[x][y].setState(ZIARNO);
            grid[x][y].setId(i + 1);
            liczbaPustychZiaren--;
        } else if (grid[x][y].getState() == ZIARNO) {

            //usunięcie z listy ziaren właśnie usunięte ziarno
            //o ile nie ma żadnego innego takiego ziarna
            grid[x][y].setState(PUSTE);
            int id = grid[x][y].getId();
            grid[x][y].setId(0);
            int idCounter = 0;

            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    if (grid[i][j].getId() == id)
                        idCounter++;
                }
            }
            if (idCounter == 0) {
                listaZiaren.remove(id - 1);
                for (int i = 0; i < gridWysokość; i++) {
                    for (int j = 0; j < gridSzerokość; j++) {
                        if (grid[i][j].getId() > id)
                            grid[i][j].setId(grid[i][j].getId() - 1);
                    }
                }
            }
            liczbaPustychZiaren++;
        }
    }

    public void wypełnijMonteCarlo(int numberOfGrains) {
        reset();
        Random rand = new Random();
        for (int i = 0; i < numberOfGrains; i++)
            listaZiaren.add(new typZiarna(rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5, rand.nextDouble()/2 + 0.5));

        for (int i = 0; i < gridWysokość; i++)
            for (int j = 0; j < gridSzerokość; j++) {
                grid[i][j].setState(ZIARNO);
                grid[i][j].setId(rand.nextInt(numberOfGrains) + 1);
                liczbaPustychZiaren--;
            }
        setGridEnergia(grid);
    }

    //lewo prawo góra dół
    private int vonNeuman(ziarno[][] frame, int iG, int i, int iD, int jL, int j, int jR) {
        Map<Integer, Integer> mapaZiaren = new HashMap<>();
        int typZiarna;

        if (iG != -1)
            if (frame[iG][j].getState() == ZIARNO) {
                typZiarna = frame[iG][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }
        if (jL != -1)
            if (frame[i][jL].getState() == ZIARNO) {
                typZiarna = frame[i][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }
        if (jR != -1)
            if (frame[i][jR].getState() == ZIARNO) {
                typZiarna = frame[i][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }
        if (iD != -1)
            if (frame[iD][j].getState() == ZIARNO) {
                typZiarna = frame[iD][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        return getIDMaxSąsiada(mapaZiaren);
    }

    //lewo prawo góra dół + skosy
    private int moore(ziarno[][] frame, int iG, int i, int iD, int jL, int j, int jR) {
        Map<Integer, Integer> mapaZiaren = new HashMap<>();
        int typZiarna;

        if (iG != -1 && jL != -1)
            if (frame[iG][jL].getState() == ZIARNO) {
                typZiarna = frame[iG][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iG != -1)
            if (frame[iG][j].getState() == ZIARNO) {
                typZiarna = frame[iG][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iG != -1 && jR != -1)
            if (frame[iG][jR].getState() == ZIARNO) {
                typZiarna = frame[iG][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (jL != -1)
            if (frame[i][jL].getState() == ZIARNO) {
                typZiarna = frame[i][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (jR != -1)
            if (frame[i][jR].getState() == ZIARNO) {
                typZiarna = frame[i][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iD != -1 && jL != -1)
            if (frame[iD][jL].getState() == ZIARNO) {
                typZiarna = frame[iD][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iD != -1)
            if (frame[iD][j].getState() == ZIARNO) {
                typZiarna = frame[iD][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iD != -1 && jR != -1)
            if (frame[iD][jR].getState() == ZIARNO) {
                typZiarna = frame[iD][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        return getIDMaxSąsiada(mapaZiaren);
    }

    //losowany kierunek, zapełnianie 5 komórek ( 8 - 3) bez jednej linii
    private int pentagonalne(ziarno[][] frame, int iG, int i, int iD, int jL, int j, int jR) {
        Map<Integer, Integer> mapaZiaren = new HashMap<>();
        int typZiarna;

        int U = 0, R = 1, D = 2, L = 3;
        Random rand = new Random();
        int randDirection = rand.nextInt(4);

        if ((iG != -1 && jL != -1) && (randDirection != U && randDirection != L))
            if (frame[iG][jL].getState() == ZIARNO) {
                typZiarna = frame[iG][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((iG != -1) && (randDirection != U))
            if (frame[iG][j].getState() == ZIARNO) {
                typZiarna = frame[iG][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((iG != -1 && jR != -1) && (randDirection != U && randDirection != R))
            if (frame[iG][jR].getState() == ZIARNO) {
                typZiarna = frame[iG][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((jL != -1) && (randDirection != L))
            if (frame[i][jL].getState() == ZIARNO) {
                typZiarna = frame[i][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((jR != -1) && (randDirection != R))
            if (frame[i][jR].getState() == ZIARNO) {
                typZiarna = frame[i][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((iD != -1 && jL != -1) && (randDirection != D && randDirection != L))
            if (frame[iD][jL].getState() == ZIARNO) {
                typZiarna = frame[iD][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((iD != -1) && (randDirection != D))
            if (frame[iD][j].getState() == ZIARNO) {
                typZiarna = frame[iD][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if ((iD != -1 && jR != -1) && (randDirection != D && randDirection != R))
            if (frame[iD][jR].getState() == ZIARNO) {
                typZiarna = frame[iD][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        return getIDMaxSąsiada(mapaZiaren);
    }

    //losowany kierunek, zapełnianie 6 komórek (8 - 1 - 1), bez dwóch rogów
    private int hexagonalne(ziarno[][] frame, int iG, int i, int iD, int jL, int j, int jR) {
        Map<Integer, Integer> mapaZiaren = new HashMap<>();
        int typZiarna;

        if (iG != -1)
            if (frame[iG][j].getState() == ZIARNO) {
                typZiarna = frame[iG][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (jL != -1)
            if (frame[i][jL].getState() == ZIARNO) {
                typZiarna = frame[i][jL].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (jR != -1)
            if (frame[i][jR].getState() == ZIARNO) {
                typZiarna = frame[i][jR].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        if (iD != -1)
            if (frame[iD][j].getState() == ZIARNO) {
                typZiarna = frame[iD][j].getId();
                wypełnijMapę(typZiarna, mapaZiaren);
            }

        Random rand = new Random();
        int los = rand.nextInt(2);
        if ((los == 0)) {
            if (iG != -1 && jL != -1)
                if (frame[iG][jL].getState() == ZIARNO) {
                    typZiarna = frame[iG][jL].getId();
                    wypełnijMapę(typZiarna, mapaZiaren);
                }

            if (iD != -1 && jR != -1)
                if (frame[iD][jR].getState() == ZIARNO) {
                    typZiarna = frame[iD][jR].getId();
                    wypełnijMapę(typZiarna, mapaZiaren);
                }
        } else if ((los == 1)) {
            if (iG != -1 && jR != -1)
                if (frame[iG][jR].getState() == ZIARNO) {
                    typZiarna = frame[iG][jR].getId();
                    wypełnijMapę(typZiarna, mapaZiaren);
                }

            if (iD != -1 && jL != -1)
                if (frame[iD][jL].getState() == ZIARNO) {
                    typZiarna = frame[iD][jL].getId();
                    wypełnijMapę(typZiarna, mapaZiaren);
                }
        }

        return getIDMaxSąsiada(mapaZiaren);
    }

    private int getIDMaxSąsiada(Map<Integer, Integer> mapaZiaren) {
        Map.Entry<Integer, Integer> maxEntry = null;

        for (Map.Entry<Integer, Integer> entry : mapaZiaren.entrySet())
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
                maxEntry = entry;

        int id = 0;
        if (maxEntry != null) {
            int max = maxEntry.getValue();

            List<Integer> listMax = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : mapaZiaren.entrySet())
                if (entry.getValue() == max)
                    listMax.add(entry.getKey());

            Random rand = new Random();
            int randWinner = rand.nextInt(listMax.size());
            id = listMax.get(randWinner);
        }

        return id;
    }

    private int sprawdźSąsiadów(ziarno[][] frame, int wysokość, int szerokość) {
        int result = 0;

        int iG, i, iD, jL, j, jR;
        i = wysokość;
        j = szerokość;

        if (wysokość == 0)
            if (warunkiBrzegowe == Absorbujące) iG = -1;
            else iG = gridWysokość - 1;
        else iG = wysokość - 1;

        if (wysokość == gridWysokość - 1)
            if (warunkiBrzegowe == Absorbujące) iD = -1;
            else iD = 0;
        else iD = wysokość + 1;

        if (szerokość == 0)
            if (warunkiBrzegowe == Absorbujące) jL = -1;
            else jL = gridSzerokość - 1;
        else jL = szerokość - 1;

        if (szerokość == gridSzerokość - 1)
            if (warunkiBrzegowe == Absorbujące) jR = -1;
            else jR = 0;
        else jR = szerokość + 1;

        switch (typSąsiedztwa) {
            case vonNeuman:
                result = vonNeuman(frame, iG, i, iD, jL, j, jR);
                break;
            case Moore:
                result = moore(frame, iG, i, iD, jL, j, jR);
                break;
            case pentagonalneLosowe:
                result = pentagonalne(frame, iG, i, iD, jL, j, jR);
                break;
            case hexagonalneLosowe:
                result = hexagonalne(frame, iG, i, iD, jL, j, jR);
                break;
        }

        return result;
    }

    private void getMapaZiaren(ziarno[][] frameGrid, Map<Integer, Integer> grainMap, int i, int j, int iG, int iD, int jL, int jR) {
        int grainType;

        if (iG != -1 && jL != -1) {
            grainType = frameGrid[iG][jL].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (iG != -1) {
            grainType = frameGrid[iG][j].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (iG != -1 && jR != -1) {
            grainType = frameGrid[iG][jR].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (jL != -1) {
            grainType = frameGrid[i][jL].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (jR != -1) {
            grainType = frameGrid[i][jR].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (iD != -1 && jL != -1) {
            grainType = frameGrid[iD][jL].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (iD != -1) {
            grainType = frameGrid[iD][j].getId();
            wypełnijMapę(grainType, grainMap);
        }

        if (iD != -1 && jR != -1) {
            grainType = frameGrid[iD][jR].getId();
            wypełnijMapę(grainType, grainMap);
        }
    }

    //zależne od warunków brzegowych do funkcji setPromień
    private Współrzędne getGóra(int x, int y, int range) {
        int cx = x, cy = y;

        for (int i = 0; i < range; i++) {
            if (cx == 0)
                if (warunkiBrzegowe == Absorbujące)
                    break;
                else
                    cx = gridWysokość - 1;
            else cx--;
        }

        return new Współrzędne(cx, cy);
    }
    private Współrzędne getDół(int x, int y, int range) {
        int cx = x, cy = y;

        for (int i = 0; i < range; i++) {
            if (cx == gridWysokość - 1)
                if (warunkiBrzegowe == Absorbujące) break;
                else cx = 0;
            else cx++;
        }

        return new Współrzędne(cx, cy);
    }
    private Współrzędne getLewo(int x, int y, int range) {
        int cx = x, cy = y;

        for (int i = 0; i < range; i++) {
            if (cy == 0)
                if (warunkiBrzegowe == Absorbujące) break;
                else cy = gridSzerokość - 1;
            else cy--;
        }

        return new Współrzędne(cx, cy);
    }
    private Współrzędne getPrawo(int x, int y, int range) {
        int cx = x, cy = y;

        for (int i = 0; i < range; i++) {
            if (cy == gridSzerokość - 1)
                if (warunkiBrzegowe == Absorbujące) break;
                else cy = 0;
            else cy++;
        }

        return new Współrzędne(cx, cy);
    }

    public int getGridWysokość() {
        return gridWysokość;
    }
    public int getGridSzerokość() {
        return gridSzerokość;
    }
    public int getLiczbaPustychZiaren() {
        return liczbaPustychZiaren;
    }
    public int getLiczbaDostępnychZiaren() {
        return liczbaDostępnychZiaren;
    }
    public List getListaZiaren() {
        return listaZiaren;
    }

    public void reset() {
        listaZiaren = new ArrayList<>();
        stwórzGrid();
    }

    //Monte-Carlo
    //dla wszystkich elementów siatki
    public void setGridEnergia(ziarno[][] frameGrid) {
        for (int i = 0; i < gridWysokość; i++) {
            for (int j = 0; j < gridSzerokość; j++) {
                int iG, iD, jL, jR;

                if (i == 0)
                    if (warunkiBrzegowe == Absorbujące) iG = -1;
                    else iG = gridWysokość - 1;
                else iG = i - 1;

                if (i == gridWysokość - 1)
                    if (warunkiBrzegowe == Absorbujące) iD = -1;
                    else iD = 0;
                else iD = i + 1;

                if (j == 0)
                    if (warunkiBrzegowe == Absorbujące) jL = -1;
                    else jL = gridSzerokość - 1;
                else jL = j - 1;

                if (j == gridSzerokość - 1)
                    if (warunkiBrzegowe == Absorbujące) jR = -1;
                    else jR = 0;
                else jR = j + 1;

                Map<Integer, Integer> grainMap = new HashMap<>();
                getMapaZiaren(frameGrid, grainMap, i, j, iG, iD, jL, jR);

                frameGrid[i][j].setEnergia(obliczEnergie(grainMap, grid[i][j].getId()));
            }
        }
    }
    //dla pojedynczego ziarna
    private int obliczEnergie(Map<Integer, Integer> grainMap, int grainID) {
        int energy = 0;

        for (Map.Entry<Integer, Integer> entry : grainMap.entrySet())
            if (entry.getKey() != grainID)
                energy += entry.getValue();

        return energy;
    }
    private int sprawdzEnergie(ziarno[][] frame, int height, int width) {
        int iG, i, iD, jL, j, jR;
        i = height;
        j = width;

        if (height == 0)
            if (warunkiBrzegowe == Absorbujące) iG = -1;
            else iG = gridWysokość - 1;
        else iG = height - 1;

        if (height == gridWysokość - 1)
            if (warunkiBrzegowe == Absorbujące) iD = -1;
            else iD = 0;
        else iD = height + 1;

        if (width == 0)
            if (warunkiBrzegowe == Absorbujące) jL = -1;
            else jL = gridSzerokość - 1;
        else jL = width - 1;

        if (width == gridSzerokość - 1)
            if (warunkiBrzegowe == Absorbujące) jR = -1;
            else jR = 0;
        else jR = width + 1;

        Map<Integer, Integer> grainMap = new HashMap<>();
        getMapaZiaren(frame, grainMap, i, j, iG, iD, jL, jR);

        int energiaWcześniej = obliczEnergie(grainMap, frame[i][j].getId());

        Random rand = new Random();
        int noweID = 0;
        List<Integer> valuesAsArray = new ArrayList<Integer>(grainMap.values());
        int randID = valuesAsArray.get(rand.nextInt(valuesAsArray.size()));
        for (Integer key : grainMap.keySet()) {
            if (randID == grainMap.get(key)) {
                noweID = key;
            }
        }
        int nowaEnergia = obliczEnergie(grainMap, noweID);
        double dE, prawdopodobienstwo, progAkceptacji;
        progAkceptacji = rand.nextDouble();
        dE = nowaEnergia - energiaWcześniej;
        if (dE <= 0)
            prawdopodobienstwo = 1;
        else {
            prawdopodobienstwo = Math.exp(-(dE / ustawienia.kt));
        }
        if (prawdopodobienstwo > progAkceptacji) {
            frame[i][j].setEnergia(nowaEnergia);
            return noweID;
        } else
            return frame[i][j].getId();
    }

    //Rekrystalizacja dynamiczna
    public double obliczPuleDyslokacji() {
        double roznicaPulDyslokacji;
        double sredniaDyslokacjaNaKomorke;
        if (aktualnyCzas == 0) {
            pulaDyslokacji = 1;
            poprzedniaPulaDyslokacji = 1;
        } else
            pulaDyslokacji = (ustawienia.A / ustawienia.B) + ((1 - (ustawienia.A / ustawienia.B)) * Math.exp(-ustawienia.B * aktualnyCzas/1000));
        listaGestosciDyslokacji.add(pulaDyslokacji);
        roznicaPulDyslokacji = pulaDyslokacji - poprzedniaPulaDyslokacji;
        poprzedniaPulaDyslokacji = pulaDyslokacji;
        sredniaDyslokacjaNaKomorke = roznicaPulDyslokacji / (gridSzerokość * gridWysokość);
        return sredniaDyslokacjaNaKomorke;
    }
    public boolean rozłóżPozostałąPulę(ziarno[][] frame, double paczka, int i, int j) {
        double prawdopod;
        Random rand = new Random();
        int iplusjeden = i+1, jplusjeden = j+1, iminusjeden = i-1, jminusjeden = j-1;
        if(i == 0)
            iminusjeden=0;
        else if (i==gridWysokość-1)
            iplusjeden=gridWysokość-1;
        if(j == 0)
            jminusjeden=0;
        else if (j==gridSzerokość-1)
            jplusjeden=gridSzerokość-1;
        if (frame[i][j].getId() != frame[iplusjeden][j].getId() || frame[i][j].getId() != frame[i][jplusjeden].getId() || frame[i][j].getId() != frame[iminusjeden][j].getId() || frame[i][j].getId() != frame[i][jminusjeden].getId())
            prawdopod = ustawienia.prawdOtrzPaczNaGr / 100;
        else
            prawdopod = 1 - ustawienia.prawdOtrzPaczNaGr / 100;

        double wylosowanePrawdopod = rand.nextDouble();

        if (wylosowanePrawdopod <= prawdopod) {
            frame[i][j].setGestoscDyslokacji(frame[i][j].getGestoscDyslokacji() + paczka);
            return true;
        }
        return false;
    }
    public ziarno dodajNoweZiarno(int x, int y){
        Random rand = new Random();
        int i = 0;
        if (listaZiaren.size() > 0)
            i = listaZiaren.size();
        typZiarna tak = new typZiarna(rand.nextDouble(),rand.nextDouble(),rand.nextDouble());
        listaZiaren.add(tak);
        grid[x][y].setState(ZIARNO);
        grid[x][y].setId(i + 1);
        grid[x][y].setGestoscDyslokacji(0);
        return grid[x][y];
    }
    public ziarno dodajZrekrystalizowaneZiarno(ziarno[][] frame, int i, int j){
        int iplusjeden = i+1, jplusjeden = j+1, iminusjeden = i-1, jminusjeden = j-1;
        int a = 0;
        if (listaZiaren.size() > 0)
            a = listaZiaren.size();
        if(i == 0)
            iminusjeden=0;
        else if (i==gridWysokość-1)
            iplusjeden=gridWysokość-1;
        if(j == 0)
            jminusjeden=0;
        else if (j==gridWysokość-1)
            jplusjeden=gridWysokość-1;

        if (frame[iplusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            int id = frame[iplusjeden][j].getId();
            Color kolor = listaZiaren.get(id - 1).getKolorZiarna();
            listaZiaren.add(new typZiarna(kolor.getRed(),kolor.getGreen(),kolor.getBlue()));
        } else if(frame[i][jplusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            int id = frame[i][jplusjeden].getId();
            Color kolor = listaZiaren.get(id - 1).getKolorZiarna();
            listaZiaren.add(new typZiarna(kolor.getRed(),kolor.getGreen(),kolor.getBlue()));
        } else if(frame[iminusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            int id = frame[iminusjeden][j].getId();
            Color kolor = listaZiaren.get(id - 1).getKolorZiarna();
            listaZiaren.add(new typZiarna(kolor.getRed(),kolor.getGreen(),kolor.getBlue()));
        } else if(frame[i][jminusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY){
            int id = frame[i][jminusjeden].getId();
            Color kolor = listaZiaren.get(id - 1).getKolorZiarna();
            listaZiaren.add(new typZiarna(kolor.getRed(),kolor.getGreen(),kolor.getBlue()));
        }
        grid[i][j].setState(ZIARNO);
        grid[i][j].setId(a + 1);
        grid[i][j].setGestoscDyslokacji(0);
        return grid[i][j];
    }
    public boolean sprawdźInfoSąsiadów(ziarno[][] frame, int i, int j){
        int iplusjeden = i+1, jplusjeden = j+1, iminusjeden = i-1, jminusjeden = j-1;
        if(i == 0)
            iminusjeden=0;
        else if (i==gridWysokość-1)
            iplusjeden=gridWysokość-1;
        if(j == 0)
            jminusjeden=0;
        else if (j==gridSzerokość-1)
            jplusjeden=gridSzerokość-1;
        if (frame[iplusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY ||  frame[i][jplusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY || frame[iminusjeden][j].getStateRekrystalizacja()==ZREKRYSTALIZOWANY ||  frame[i][jminusjeden].getStateRekrystalizacja()==ZREKRYSTALIZOWANY)
            if(frame[i][j].getGestoscDyslokacji()>frame[iplusjeden][j].getGestoscDyslokacji() && frame[i][j].getGestoscDyslokacji()>frame[i][jplusjeden].getGestoscDyslokacji() &&  frame[i][j].getGestoscDyslokacji()>frame[iminusjeden][j].getGestoscDyslokacji() && frame[i][j].getGestoscDyslokacji()>frame[i][jminusjeden].getGestoscDyslokacji())
                return true;
        return false;
    }

    public ziarno[][] getGrid() {
        return grid;
    }
    private ziarno[][] getTmp() {
        ziarno[][] tmp = new ziarno[gridWysokość][gridSzerokość];
        for (int i = 0; i < gridWysokość; i++)
            for (int j = 0; j < gridSzerokość; j++)
                tmp[i][j] = new ziarno();

        return tmp;
    }

    public ziarno[][] getResult(ziarno[][] frame) {
        ziarno[][] tmp = getTmp();
        if (zarodkowanie != MonteCarlo && zarodkowanie != rekrystalizacja) {
            aktualnyCzas=0;
            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    if (frame[i][j].getState() == PUSTE) {
                        tmp[i][j].setId(sprawdźSąsiadów(frame, i, j));
                        if (tmp[i][j].getId() != 0) {
                            tmp[i][j].setState(ZIARNO);
                            tmp[i][j].setStateRekrystalizacja(NIEZREKRYSTALIZOWANY);
                            liczbaPustychZiaren--;
                        }
                    } else if (frame[i][j].getState() == ZIARNO) {
                        tmp[i][j].setState(ZIARNO);
                        tmp[i][j].setStateRekrystalizacja(NIEZREKRYSTALIZOWANY);
                        tmp[i][j].setId(frame[i][j].getId());
                    }
                }
            }
        }
        else if (zarodkowanie == MonteCarlo) {
            aktualnyCzas=0;
            setGridEnergia(frame);
            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    if (frame[i][j].getEnergia() != 0) {
                        tmp[i][j].setId(sprawdzEnergie(frame, i, j));
                        tmp[i][j].setEnergia(frame[i][j].getEnergia());
                        tmp[i][j].setState(ZIARNO);
                        tmp[i][j].setStateRekrystalizacja(NIEZREKRYSTALIZOWANY);
                    } else {
                        tmp[i][j].setState(ZIARNO);
                        tmp[i][j].setStateRekrystalizacja(NIEZREKRYSTALIZOWANY);
                        tmp[i][j].setId(frame[i][j].getId());
                        tmp[i][j].setEnergia(tmp[i][j].getEnergia());
                    }
                }
            }
        }
        else if (zarodkowanie==rekrystalizacja) {
            Random rand = new Random();
            double pozostałaPula = 0, paczka , sredniaDyslokacjaNaKomorke = obliczPuleDyslokacji();
            double wartośćGranicznaDyslokacjiNaKomórkę = 4.21584E+12 / (gridWysokość * gridSzerokość);

            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    tmp[i][j].setState(ZIARNO);
                    tmp[i][j].setId(frame[i][j].getId());
                    tmp[i][j].setStateRekrystalizacja(frame[i][j].getStateRekrystalizacja());
                    if(aktualnyCzas==0) {
                        tmp[i][j].setStateRekrystalizacja(NIEZREKRYSTALIZOWANY);
                    }
                }
            }

            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    tmp[i][j].setGestoscDyslokacji(tmp[i][j].getGestoscDyslokacji() + sredniaDyslokacjaNaKomorke * ustawienia.procentDyslokacji / 100);
                    pozostałaPula += sredniaDyslokacjaNaKomorke * (1 - ustawienia.procentDyslokacji / 100);
                }
            }

            paczka = pozostałaPula / (100 / ustawienia.rozmiarPaczki);

            while (pozostałaPula > 0) {
                int i = rand.nextInt(gridWysokość);
                int j = rand.nextInt(gridSzerokość);
                if (rozłóżPozostałąPulę(tmp, paczka, i, j))
                    pozostałaPula -= paczka;
            }

            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    int iplusjeden = i+1, jplusjeden = j+1, iminusjeden = i-1, jminusjeden = j-1;
                    if(i == 0)
                        iminusjeden=0;
                    else if (i==gridWysokość-1)
                        iplusjeden=gridWysokość-1;
                    if(j == 0)
                        jminusjeden=0;
                    else if (j==gridSzerokość-1)
                        jplusjeden=gridSzerokość-1;
                    if ((tmp[i][j].getId() != tmp[iplusjeden][j].getId() || tmp[i][j].getId() != tmp[i][jplusjeden].getId() || tmp[i][j].getId() != tmp[iminusjeden][j].getId() || tmp[i][j].getId() != tmp[i][jminusjeden].getId()) && tmp[i][j].getGestoscDyslokacji()>wartośćGranicznaDyslokacjiNaKomórkę) {
                        tmp[i][j] = dodajNoweZiarno(i, j);
                        tmp[i][j].setStateRekrystalizacja(ZREKRYSTALIZOWANY);
                    }
                }
            }

            for (int i = 0; i < gridWysokość; i++) {
                for (int j = 0; j < gridSzerokość; j++) {
                    if(sprawdźInfoSąsiadów(tmp, i, j)){
                        tmp[i][j] = dodajZrekrystalizowaneZiarno(tmp, i, j);
                        tmp[i][j].setStateRekrystalizacja(ZREKRYSTALIZOWANY);
                    }
                }
            }
        }

        for (int i = 0; i < gridWysokość; i++)
            System.arraycopy(tmp[i], 0, grid[i], 0, gridSzerokość);

        return getGrid();
    }
}