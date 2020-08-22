package client.sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Window extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Chatter");
        primaryStage.setMinHeight(200);
        primaryStage.setMinWidth(300);
        primaryStage.setScene(new Scene(root, 600, 700));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
