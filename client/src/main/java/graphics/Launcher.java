package graphics;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Was supposed to be starting class, but doesn't work from jar due to it extending Application
 */
public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Handler h = new Handler(primaryStage);
    }

    public static void launch_gui(String[] args) {
        launch(args);
    }
}
