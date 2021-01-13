package graphics;

import game.Action;
import game.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;

public class LobbyListCtrl extends OverlordCtrl implements CtrlNecessities {
    public Ellipse clientConnectCircle;
    public Label clientConnectionLabel;
    public Label clientNameLabel;
    public Label clientStateLabel;
    public Label responseLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;


    public VBox lobbyHolder;
    public Button cancelBtn;

//    public HBox status;
//    @FXML
//    private StatusBarCtrl statusController;


    @Override
    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    public void setLobbyContent(ArrayList<HBox> lobbies) {
        if(lobbies.isEmpty()) {
            Label empty = new Label("There are no active lobbies");
            empty.setStyle("-fx-font-size: 18px");

            lobbyHolder.getChildren().add(empty);
        }

        for(HBox lb : lobbies) {
            lobbyHolder.getChildren().add(lb);
        }
    }

    public void cancel(ActionEvent actionEvent) {
        if(client.getAutomaton().validateTransition(Action.CANCEL)) {
            client.getAutomaton().makeTransition(Action.CANCEL);
            genericSetScene("main_menu_connect_v2.fxml");
        } else {
            responseLabel.setText("Automaton: transition validation failed");
//            statusController.responseLabel.setText("Automaton: transition validation failed");
        }
    }
}
