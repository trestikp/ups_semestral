package graphics;

import game.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Ellipse;
import network.Instruction;

public class ReconnectionRepeatCtrl extends OverlordCtrl implements CtrlNecessities {
    // Status bar elements
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;


    public Label messageLbl;


    @FXML
    public void initialize() {
        if(client == null) {
            client = Handler.getClient();

            setClient(client);

            client.setCurrentCtrl(this);
        }
    }

    public void reconnect(ActionEvent actionEvent) {
        if(!client.readConnectionFromFile()) {
            genericSetScene("main_menu_disconnected.fxml");
            client.getStatus().setResponseText("Failed to retrieve connection");

            return;
        }

        if(client.getShowingReconnect()) {
            client.setShowingReconnect(false);
            messageLbl.setText("Attempting reconnect");
        } else {
            client.setQuitAfterDisconnect(true); //variable name makes no sense because i'm reusing it  //TODO?
            client.setInstruction(Instruction.CONNECT);
        }
    }

    public void cancel(ActionEvent actionEvent) {
        client.resetConnectionInfo();
        genericSetScene("main_menu_disconnected.fxml");
    }

    @Override
    public void setClient(Client client) {
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }
}
