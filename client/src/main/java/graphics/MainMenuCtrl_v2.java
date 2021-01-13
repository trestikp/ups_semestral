package graphics;

import game.Action;
import game.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Ellipse;
import network.Instruction;
import network.TcpConnection;

import java.io.IOException;

//public class MainMenuCtrl_v2 implements CtrlNecessities {
public class MainMenuCtrl_v2 extends OverlordCtrl implements CtrlNecessities {
//    private Client client;


//    public HBox status;
//    @FXML
//    private StatusBarCtrl statusController;

    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    public Button quickPlayBtn;
    public Button joinLobbyBtn;
    public Button createLobbyBtn;
    public Button disconnectBtn;
    public Button quitBtn;

    //disconnected buttons
    public Button connect;
    public Button aboutBtn;


    private Button[] btnArray = new Button[]{quickPlayBtn, joinLobbyBtn, createLobbyBtn, disconnectBtn, quitBtn};

    public VBox mainMenuPane;

    @FXML
    public void initialize() {
//        mainMenuPane.widthProperty().addListener(change -> resizeWindow());
//        mainMenuPane.heightProperty().addListener(change -> resizeWindow());

        client = Handler.getClient();

//        setClient(client, statusController);

        setClient(client);

//        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
//                            clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
//        setClient(client, status);
    }

    private void resizeWindow() {
        double width = mainMenuPane.getWidth();
        double height = mainMenuPane.getHeight();

        double buttonHeight = height * 0.05;
        double buttonWidth = width * 0.25;

        if(buttonHeight < quickPlayBtn.getPrefHeight()) buttonHeight = 36;
        if(buttonWidth < quickPlayBtn.getPrefWidth()) buttonWidth = 300;

        for(Node n : mainMenuPane.getChildren()) {
            for(Button b : btnArray) {
                if(n.getId().equals(b.getId())) {
                    n.resize(buttonWidth, buttonHeight);
                }
            }
        }
    }

    @Override
    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    public void about(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/about.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            return;
        }

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }

    public void connect(ActionEvent actionEvent) {
        genericSetScene("connection_picker_v2.fxml");

        if(client.getAutomaton().validateTransition(Action.CONNECT)) {
            client.getAutomaton().makeTransition(Action.CONNECT);
        } else {
            responseLabel.setText("Invalid automaton transition");
//            statusController.responseLabel.setText("Invalid automaton transition");
        }
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/connection_picker_v2.fxml"));
//        Parent pane;
//
//        try {
//            pane = loader.load();
//        } catch (IOException e) {
//            //TODO: logger
//            System.out.println("Failed to load fxml");
//            return;
//        }
//
//        ((CtrlNecessities) loader.getController()).setClient(client);
//
//        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
    }

    public void disconnect(ActionEvent actionEvent) {
        client.setInstruction(Instruction.DISCONNECT);
    }

    public void quit(ActionEvent actionEvent) {
        if(client.isClientConnected()) {
            client.setInstruction(Instruction.DISCONNECT);
        } else {
            if(client.getAutomaton().validateTransition(Action.QUIT)) {
                client.getAutomaton().makeTransition(Action.QUIT);
            } else {
                responseLabel.setText("Invalid automaton transition");
//                statusController.responseLabel.setText("Invalid automaton transition");
            }

            Platform.exit();
        }
    }

    public void createLobby(ActionEvent actionEvent) {
        if(client.getAutomaton().validateTransition(Action.CREATE)) {
            client.getAutomaton().makeTransition(Action.CREATE);
            genericSetScene("lobby_creation.fxml");
        } else {
            responseLabel.setText("Automaton: transition validation failed");
//            statusController.responseLabel.setText("Automaton: transition validation failed");
        }
    }

    public void joinLobby(ActionEvent actionEvent) {
        client.setInstruction(Instruction.LOBBY);
    }

    public void quickPlay(ActionEvent actionEvent) {
        client.setInstruction(Instruction.QUICK_PLAY);
    }
}
