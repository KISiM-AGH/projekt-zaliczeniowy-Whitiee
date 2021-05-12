package aplikacja;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ustawienia;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ustawienia.Stage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/aplikacja/widokRozrostZiaren.fxml"));
        primaryStage.setTitle("CA - Julia Fajer");
        primaryStage.setScene(new Scene(root, 800, 620));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
