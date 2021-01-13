package graphics;

import game.Action;
import game.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Ellipse;
import network.Instruction;

public class LobbyCreationCtrl extends OverlordCtrl implements CtrlNecessities {
//    private Client client;

    public Ellipse clientConnectCircle;
    public Label clientConnectionLabel;
    public Label clientNameLabel;
    public Label clientStateLabel;
    public Label responseLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    public TextField lobbyNameTF;
    public Button createLobbyBtn;
    public Button cancelLobbyBtn;
    public Label lobbyNameErrLbl;


//    public HBox status;
//    @FXML
//    private StatusBarCtrl statusController;


    public void create(ActionEvent actionEvent) {
        if(lobbyNameTF.getText().isBlank()) {
            lobbyNameErrLbl.setText("Please enter a name");
            lobbyNameErrLbl.setVisible(true);
            return;
        }

        client.setInstruction(Instruction.CREATE_LOBBY);
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

    public String getLobbyName() {
        return lobbyNameTF.getText();
    }

    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }
}
