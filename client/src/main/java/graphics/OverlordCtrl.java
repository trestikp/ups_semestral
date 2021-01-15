package graphics;

import game.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Is extended by all controllers that need communication with server. Server to set client communication instance
 * and supplies genericSetScene which is used for almost all scene transitions.
 */
public class OverlordCtrl {
    /**
     * Client instance
     */
    protected Client client;

    /**
     * Parent set client. All controllers have to overwrite this method because of CtrlNecessities. Called inside
     * @param client instance
     * @param status bar instance
     */
    public void setClient(Client client, StatusBar status) {
        this.client = client;

        client.setStatusBar(status);
    }

    /**
     * Generic setScene method used in almost all scene transitions. Sets client instance and current controller
     * inside said client instance
     * @param fxmlName name of .fxml file to be set as scene
     */
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
