package aplikacja;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import model.ustawienia;
import static model.modelRozrostZiaren.*;
import model.modelRozrostZiaren;
import model.ziarno;
import model.obrazRozrostZiaren;
import java.net.URL;
import java.util.ResourceBundle;
import static model.modelRozrostZiaren.warunkiBrzegowe.*;
import static model.modelRozrostZiaren.typSąsiedztwa.*;
import static model.modelRozrostZiaren.zarodkowanie.*;

public class kontrolerRozrostZiaren implements Initializable {
    @FXML
    ScrollPane scrollPane;
    @FXML
    AnchorPane pane;
    @FXML
    TextField liczbaZiarenField, odleglośćPromieńField, xField, yField, krokiField, ktField;
    @FXML
    ChoiceBox<String> choiceBoxTypSąsiedztwa;
    @FXML
    ChoiceBox<String> choiceBoxWarunkiBrzegowe;
    @FXML
    ChoiceBox<String> choiceBoxZarodkowanie;
    @FXML
    Button startButton, stopButton, wypełnijButton, resetButton, graniceButton;
    @FXML
    Canvas canvas2D;

    private GraphicsContext gc;
    private modelRozrostZiaren model;
    private int gridWysokość, gridSzerokość;
    private obrazRozrostZiaren painter;
    private Thread thread;
    private typSąsiedztwa sType;
    private warunkiBrzegowe wType;
    private zarodkowanie zType;
    private boolean wznow;

    //początkowe ustawienia, ustawienie opcji pisania tylko liczb w polach tekstowych, wypełnienie rozwijanych list opcjami
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas2D.getGraphicsContext2D();

        String[] opcjeWarunkówBrzegowych = new String[]{"Absorbujące", "Periodyczne"};
        choiceBoxWarunkiBrzegowe.setItems(FXCollections.observableArrayList(opcjeWarunkówBrzegowych));
        choiceBoxWarunkiBrzegowe.setValue("Absorbujące");
        wType = warunkiBrzegowe.Absorbujące;
        choiceBoxWarunkiBrzegowe.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    wType = Absorbujące;
                    break;
                case 1:
                    wType = Periodyczne;
                    break;
            }
            model = new modelRozrostZiaren(gridWysokość, gridSzerokość, sType, wType);
            cleanCanvas();
        });

        String[] opcjeSąsiedztwa = new String[]{"von Neumann", "Moore", "Pentagonalne losowe", "Hexagonalne"};
        choiceBoxTypSąsiedztwa.setItems(FXCollections.observableArrayList(opcjeSąsiedztwa));
        choiceBoxTypSąsiedztwa.setValue("von Neumann");
        sType = typSąsiedztwa.vonNeuman;
        choiceBoxTypSąsiedztwa.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    sType = vonNeuman;
                    break;
                case 1:
                    sType = Moore;
                    break;
                case 2:
                    sType = pentagonalneLosowe;
                    break;
                case 3:
                    sType = hexagonalneLosowe;
                    break;
            }

            if (model != null)
                model.setTypSąsiedztwa(sType);
        });

        String[] opcjeZarodkowania = new String[]{"Losowe", "Jednorodne", "Z promieniem", "Monte Carlo"};
        choiceBoxZarodkowanie.setItems(FXCollections.observableArrayList(opcjeZarodkowania));
        choiceBoxZarodkowanie.setValue("Losowe");
        zType = losowe;
        choiceBoxZarodkowanie.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    zType = losowe;
                    odleglośćPromieńField.setDisable(true);
                    krokiField.setDisable(true);
                    ktField.setDisable(true);
                    graniceButton.setDisable(true);
                    break;
                case 1:
                    zType = jednorodne;
                    odleglośćPromieńField.setDisable(false);
                    krokiField.setDisable(true);
                    ktField.setDisable(true);
                    graniceButton.setDisable(true);
                    break;
                case 2:
                    zType = zPromieniem;
                    odleglośćPromieńField.setDisable(false);
                    krokiField.setDisable(true);
                    ktField.setDisable(true);
                    graniceButton.setDisable(true);
                    break;
                case 3:
                    zType = MonteCarlo;
                    odleglośćPromieńField.setDisable(true);
                    krokiField.setDisable(false);
                    ktField.setDisable(false);
                    graniceButton.setDisable(false);
                    break;
            }
            if (model != null) {
                model.setZarodkowanie(zType);
                ustawienia.typZarodkowania = zType;
            }
        });

        //wpisane wartosci musza byc liczbowe (minus tez odpada)
        liczbaZiarenField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    liczbaZiarenField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });
        odleglośćPromieńField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    odleglośćPromieńField.setText(oldValue);
                }
            } catch (Exception ignored) {
            }
        });
        xField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    xField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });
        yField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*"))
                    yField.setText(oldValue);
            } catch (Exception ignored) {
            }
        });
        krokiField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    krokiField.setText(oldValue);
                }
            } catch (Exception ignored) {
            }
        });
        /*ktField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.matches("\\d*")) {
                    ktField.setText(oldValue);
                }
            } catch (Exception ignored) {
            }
        });*/
        gridSzerokość = 580;
        gridWysokość = 580;
        model = new modelRozrostZiaren(gridWysokość, gridSzerokość, sType, wType);
        model.setZarodkowanie(zType);
        cleanCanvas();
        stopButton.setDisable(true);
        krokiField.setDisable(true);
        ktField.setDisable(true);
        graniceButton.setDisable(true);
        odleglośćPromieńField.setDisable(true);
        ustawienia.prędkośćAnimacji = 1000;
    }

    //wyczyszczenie obrazu
    private void cleanCanvas() {
        gc.setFill(Color.WHITE);
        gc.clearRect(0, 0, canvas2D.getHeight(), canvas2D.getWidth());
        if (model != null) gc.fillRect(0, 0, model.getGridSzerokość(), model.getGridWysokość());
        else gc.fillRect(0, 0, canvas2D.getHeight(), canvas2D.getWidth());
    }
    //odświeżanie obrazu
    private void odświeżCanvas() {
        Platform.runLater(() -> {
            ziarno[][] tab;

            tab = model.getGrid();
            cleanCanvas();

            int height = ustawienia.wysokośćZiarna;
            int width = ustawienia.szerokośćZiarna;
            for (int i = 0; i < model.getGridWysokość(); i++) {
                for (int j = 0; j < model.getGridSzerokość(); j++) {
                    if (tab[i][j].getState() == ziarno.State.ZIARNO) {
                        gc.setFill(((modelRozrostZiaren.typZiarna) model.getListaZiaren().get(tab[i][j].getId() - 1)).getKolorZiarna());
                        gc.fillRect(j * height, i * width, height, width);
                    }
                }
            }
        });
    }
    //wypełnienie obrazu
    public void wypełnijCanvas(ActionEvent actionEvent) {
        int grainAmount, distance_radius;
        try {
            grainAmount = Integer.parseInt(liczbaZiarenField.getText());
        } catch (NumberFormatException e) {
            grainAmount = 1;
        }
        try {
            distance_radius = Integer.parseInt(odleglośćPromieńField.getText());
            if (distance_radius < 1) distance_radius = 1;
        } catch (NumberFormatException e) {
            distance_radius = 1;
        }
        if (model != null) {
            int numberOfAddedGrains;
            Alert a = new Alert(Alert.AlertType.WARNING);
            if (zType == losowe) {
                numberOfAddedGrains = model.wypełnijLosowo(grainAmount);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount) {
                    a.setContentText("Nie udało dodać się " + grainAmount + " ziaren." +
                            "\nDodano " + numberOfAddedGrains + " ziaren");
                    a.show();
                }
            } else if (zType == jednorodne) {
                numberOfAddedGrains = model.wypełnijJednorodnie(grainAmount, distance_radius);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount) {
                    a.setContentText("Nie udało dodać się " + grainAmount + " ziaren, przy zadanej odległości " +
                            "\nDodano " + numberOfAddedGrains + " ziaren");
                    a.show();
                }
            } else if (zType == zPromieniem) {
                numberOfAddedGrains = model.wypełnijZPromieniem(grainAmount, distance_radius);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount) {
                    a.setContentText("Nie udało dodać się " + grainAmount + " ziaren." +
                            "\nDodano " + numberOfAddedGrains + " ziaren.");
                    a.show();
                }
            } else if (zType == MonteCarlo) {
                model.wypełnijMonteCarlo(grainAmount);
                odświeżCanvas();
            }
        }
    }

    //ustawienie wymiarów obrazu
    public void ustaw(ActionEvent actionEvent) {
        int x, y;
        try {
            x = Integer.parseInt(xField.getText());
        } catch (NumberFormatException e) {
            x = 1;
        }
        try {
            y = Integer.parseInt(yField.getText());
            if (y < 1) y = 1;
        } catch (NumberFormatException e) {
            y = 1;
        }
        if ( x > 2000) {
            System.out.println("Nie udało dodać się stworzyc okna o wymiarach " + x + "x" + y +
                    "\nStworzenie okna o wymiarach 2000x"+y);
            x=2000;
        }
        if ( y > 2000) {
            System.out.println("Nie udało dodać się stworzyc okna o wymiarach " + x + "x" + y +
                    "\nStworzenie okna o wymiarach" + x+ "x2000");
            y=2000;
        }
        gridSzerokość = x;
        gridWysokość = y;
        model = new modelRozrostZiaren(gridWysokość, gridSzerokość, sType, wType);
        model.setZarodkowanie(zType);
        cleanCanvas();
    }
    //rozmieszczenie ziarna
    @FXML
    private void dodajZiarnoDoCanvas(MouseEvent mouseEvent) {
        int x0 = 1, y0 = 30; //współrzędne początka canvasa w okienku
        //współrzędne w okienku
        int x = (int) mouseEvent.getSceneX() - x0 + ((int) scrollPane.getHvalue() + (int) scrollPane.getHvalue() / 26);
        int y = (int) mouseEvent.getSceneY() - y0 + ((int) scrollPane.getVvalue() + (int) scrollPane.getVvalue() / 26);

        //pozycja komórki w oknie
        int canvasX = x;
        int canvasY = y;

        //pozycja komórki w siatce
        int gridX = canvasX;
        int gridY = canvasY;

        if (!(gridX > model.getGridSzerokość() - 1 || gridX < 0 || gridY > model.getGridWysokość() - 1 || gridY < 0)) {
            model.dodajZiarno(gridY, gridX);
            odświeżCanvas();
        }
    }

    //reset rozmieszczenia ziarna
    public void reset(ActionEvent actionEvent) {
        model.reset();
        cleanCanvas();
    }

    public void pokazGranice(ActionEvent actionEvent) {
        System.gc();
        painter = new obrazRozrostZiaren(canvas2D, model, gc, this, true);
        thread = new Thread(painter);
        thread.setDaemon(true);
        thread.start();
        graniceButton.setDisable(true);
        stopButton.setDisable(true);
    }

    //start symulacji
    public void start(ActionEvent actionEvent) {
        if(ustawienia.typZarodkowania==MonteCarlo) {
            int kroki;
            double kt;
            Alert a = new Alert(Alert.AlertType.WARNING);
            try {
                kroki = Integer.parseInt(krokiField.getText());
                if (kroki < 1) kroki = 1;
            } catch (NumberFormatException e) {
                kroki = 1;

                a.setContentText("Nie podano poprawnej liczby krokow, liczba krokow ustawiona na 1");
                a.show();
            }
            try {
                kt = Double.parseDouble(ktField.getText());
                if (kt < 0.1){
                    kt = 0.1;
                    a.setContentText("Podano za małą wartość kt, kt ustawiona na 0.1");
                    a.show();
                }
                else if (kt > 6){
                    kt = 6;
                    a.setContentText("Podano za dużą wartość kt, kt ustawiona na 6");
                    a.show();
                }
            } catch (NumberFormatException e) {
                kt = 0.1;
                a.setContentText("Nie podano poprawnej wartości kt, kt ustawiona na 0.1");
                a.show();
            }

            if (!wznow)
                ustawienia.liczbaWykonanychKrokowSymulacji = 0;
            ustawienia.liczbaKrokowSymulacji = kroki - ustawienia.liczbaWykonanychKrokowSymulacji;
            ustawienia.kt=kt;
        }
        startButton.setDisable(true);
        stopButton.setDisable(false);
        graniceButton.setDisable(true);
        System.gc();
        painter = new obrazRozrostZiaren(canvas2D, model, gc, this, false);
        thread = new Thread(painter);
        thread.setDaemon(true);
        thread.start();
    }

    //stop symulacji
    public void stop(ActionEvent actionEvent) {
        wznow=true;
        startButton.setDisable(false);
        stopButton.setDisable(true);
        if (ustawienia.typZarodkowania==MonteCarlo)
            graniceButton.setDisable(false);
        painter.pause();
    }

    //ustawienia do resetu symulacji
    public void resetButtons() {
        wznow=false;
        startButton.setDisable(false);
        stopButton.setDisable(true);

        model.setZarodkowanie(zType);
        ustawienia.typZarodkowania = zType;

        if(ustawienia.typZarodkowania==MonteCarlo)
            graniceButton.setDisable(false);
        painter.stop();
    }
}
