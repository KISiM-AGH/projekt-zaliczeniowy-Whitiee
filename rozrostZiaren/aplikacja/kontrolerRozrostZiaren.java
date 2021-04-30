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
    TextField liczbaZiarenField, odleglośćPromieńField, xField, yField;
    @FXML
    ChoiceBox<String> choiceBoxTypSąsiedztwa;
    @FXML
    ChoiceBox<String> choiceBoxWarunkiBrzegowe;
    @FXML
    ChoiceBox<String> choiceBoxZarodkowanie;
    @FXML
    Button startButton, stopButton, wypełnijButton, resetButton;
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

        String[] opcjeZarodkowania = new String[]{"Losowe", "Jednorodne", "Z promieniem"};
        choiceBoxZarodkowanie.setItems(FXCollections.observableArrayList(opcjeZarodkowania));
        choiceBoxZarodkowanie.setValue("Losowe");
        zType = losowe;
        choiceBoxZarodkowanie.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    zType = losowe;
                    odleglośćPromieńField.setDisable(true);
                    break;
                case 1:
                    zType = jednorodne;
                    odleglośćPromieńField.setDisable(false);
                    break;
                case 2:
                    zType = zPromieniem;
                    odleglośćPromieńField.setDisable(false);
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

        gridSzerokość = 580;
        gridWysokość = 580;
        model = new modelRozrostZiaren(gridWysokość, gridSzerokość, sType, wType);
        model.setZarodkowanie(zType);
        cleanCanvas();

        stopButton.setDisable(true);
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

            if (zType == losowe) {
                numberOfAddedGrains = model.wypełnijLosowo(grainAmount);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount)
                    System.out.println("Nie ma wystarczająco dużo miejsca aby dodać " + grainAmount + " dodatkowych ziaren." +
                            "\nUdało dodać się " + numberOfAddedGrains + " ziaren");
            } else if (zType == jednorodne) {
                numberOfAddedGrains = model.wypełnijJednorodnie(grainAmount, distance_radius);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount)
                    System.out.println("Nie ma wystarczająco dużo miejsca aby dodać " + grainAmount +
                            " dodatkowych ziaren, przy odległości między ziarnami równej " + distance_radius +
                            "\nUdało dodać się " + numberOfAddedGrains + " ziaren");
            } else if (zType == zPromieniem) {
                numberOfAddedGrains = model.wypełnijZPromieniem(grainAmount, distance_radius);
                odświeżCanvas();
                if (numberOfAddedGrains < grainAmount)
                    System.out.println("Nie udało dodać się " + grainAmount + " ziaren." +
                            "\nDodano " + numberOfAddedGrains + " ziaren.");
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

    //start symulacji
    public void start(ActionEvent actionEvent) {
        startButton.setDisable(true);
        stopButton.setDisable(false);
        System.gc();

        painter = new obrazRozrostZiaren(canvas2D, model, gc, this);
        thread = new Thread(painter);
        thread.setDaemon(true);
        thread.start();
    }

    //stop symulacji
    public void stop(ActionEvent actionEvent) {
        startButton.setDisable(false);

        if (painter.isPaused())
            painter.resume();
        else
            painter.pause();
    }

    //ustawienia do resetu symulacji
    public void resetButtons() {
        startButton.setDisable(false);
        stopButton.setDisable(true);

        model.setZarodkowanie(zType);
        ustawienia.typZarodkowania = zType;

        painter.stop();
    }
}
