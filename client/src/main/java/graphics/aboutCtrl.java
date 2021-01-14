package graphics;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class aboutCtrl {
    public void back(ActionEvent actionEvent) {
        FXMLLoader loader;
        Parent pane;

        loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_disconnected.fxml"));

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml: main_menu_disconnected.fxml");
            return;
        }

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }
}
