package graphics;

import game.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main class containing primary stage of GUI.
 */
public class Handler {
    /** Primary stage */
    private static Stage primaryStage;
    /** Instance of class, that handles communication and game logic */
    private static Client client;

    /**
     * Constructor
     * @param primaryStage passed down from Application extension
     * @throws Exception
     */
    public Handler(Stage primaryStage) throws Exception{
        setPrimaryStage(primaryStage);

        client = new Client();
        client.start();
        //TODO: join threads ?

        File f = new File("./.last_connection");
        FXMLLoader loader;

        if(f.exists()) {
            loader = new FXMLLoader(getClass().getResource("/fxml_res/reconnection_repeat.fxml"));
        } else {
            loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_disconnected.fxml"));
        }

        Parent root = loader.load();

        primaryStage.setTitle("Draughts");
        primaryStage.setScene(new Scene(root, 1280, 720));
        primaryStage.show();
    }

    /**
     * Used to set primary stage for instance
     * @param p primary stage
     */
    private static void setPrimaryStage(Stage p) {
        primaryStage = p;
    }

    /**
     *
     * @return Stage - primary stage of application
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Sets scene on primary stage
     * @param scene to be set
     */
    public static void setScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    /**
     * @return Client class instance
     */
    public static Client getClient() {
        return client;
    }
}
