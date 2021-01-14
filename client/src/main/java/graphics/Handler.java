package graphics;

import game.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Handler {
    private static Stage primaryStage;
    private static Client client;

    public Handler(Stage primaryStage) throws Exception{
        setPrimaryStage(primaryStage);

        client = new Client();
        client.start();
        //TODO: join threads

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_disconnected.fxml"));
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/template.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Checkers");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }

    private static void setPrimaryStage(Stage p) {
        primaryStage = p;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    public static Client getClient() {
        return client;
    }
}
