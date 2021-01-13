package graphics;

import game.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class OverlordCtrl {
    protected Client client;

    public void setClient(Client client, StatusBar status) {
        this.client = client;

        client.setStatusBar(status);
//        client.updateStatusElements();
    }
//    public void setClient(Client client, StatusBarCtrl status) {
//        this.client = client;
//
//        client.setStatusBarCtrl(status);
//        client.updateStatusElements();
//    }

    public void genericSetScene(String fxmlName) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/" + fxmlName));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load FXML: " + fxmlName);
            e.printStackTrace();
            return;
        }

        ((CtrlNecessities) loader.getController()).setClient(client);
        client.setCurrentCtrl(loader.getController());

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }
}
