package graphics;

import game.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import network.Instruction;
import network.TcpConnection;

import java.io.IOException;

public class MainMenuCtrl {
    private boolean connected = false;
    private Client client;

    //disconnected
    public Button connectBtn;

    //connected
    public Button playBtn;
    public Button disconnectBtn;

    //common
    public Button quitBtn;
    public Button aboutBtn;
    public AnchorPane mainMenuPane;

    @FXML
    public void initialize() {
        mainMenuPane.widthProperty().addListener(change -> resizeWindow());
        mainMenuPane.heightProperty().addListener(change -> resizeWindow());

        client = Handler.getClient();
        connected = client.getConnection() != null;

//        System.out.println("Initialized MainContorler with game ID: " + client.getGame().getId());
    }

    private void resizeWindow() {
        double width = mainMenuPane.getWidth();
        double height = mainMenuPane.getHeight();

        double buttonHeight = height * 0.05;
        if(buttonHeight < 36) buttonHeight = 36; //it looks bad when its smaller

        double buttonHeightOffset = buttonHeight * 0.5;
        double inc = height / (mainMenuPane.getChildren().size() + 1);

        for(int i = 0; i < mainMenuPane.getChildren().size(); i++) {
            Node n = mainMenuPane.getChildren().get(i);

            AnchorPane.setLeftAnchor(n, width * 0.25);
            AnchorPane.setRightAnchor(n, width * 0.25);
            AnchorPane.setTopAnchor(n, inc * (i + 1) - buttonHeightOffset);
            AnchorPane.setBottomAnchor(n, height - (inc * (i + 1)) - buttonHeightOffset);
        }
    }

    public void connect(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/connection_picker.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            return;
        }

        ((ConnectionPickerCtrl) loader.getController()).setClient(client);
//        client.setPickerCtrl(loader.getController());

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }

    public void play(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/gameboard.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            e.printStackTrace();
            return;
        }

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));

//        client.getGame().setGBCtrl(loader.getController());
    }

    public void disconnect(ActionEvent actionEvent) {
        //TODO: connection handling

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_disconnected.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            return;
        }
        
        client.setInstruction(Instruction.DISCONNECT);

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));

        connected = false;
    }

    public void about(ActionEvent actionEvent) {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/about.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_connect_v2.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            return;
        }
//
//        System.out.println("Main menu is " + (connected ? "connected" : "disconnected"));
//
//        ((aboutCtrl) loader.getController()).setConnected(connected);

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }

    public void setMainMenuScene(TcpConnection connection) {
        if(connected) {

        } else {

        }
    }

    public void quit(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void createLobby(ActionEvent actionEvent) {

    }

    public void joinLobby(ActionEvent actionEvent) {

    }
}
