package graphics;

import javafx.application.Application;
import javafx.stage.Stage;

//TODO: --module-path="/usr/lib/jvm/javafx-sdk-11.0.2/lib" --add-modules=javafx.control

public class Launcher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Handler h = new Handler(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
