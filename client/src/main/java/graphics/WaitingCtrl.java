package graphics;

import game.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Ellipse;
import network.Instruction;

public class WaitingCtrl extends OverlordCtrl implements CtrlNecessities {
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    public Button cancelWaitBtn;
//    public HBox status;
//    @FXML
//    private StatusBarCtrl statusController;

    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    public void cancel(ActionEvent actionEvent) {
        client.setInstruction(Instruction.DELETE_LOBBY);
    }
}
