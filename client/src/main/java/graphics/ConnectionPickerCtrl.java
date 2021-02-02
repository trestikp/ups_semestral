package graphics;

import game.Action;
import game.Client;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Ellipse;
import network.Instruction;


/**
 * FXML controller for file connection_picker.fxml. Extends OverlordCtrl and implements CtrlNecessities
 */
public class ConnectionPickerCtrl extends OverlordCtrl implements CtrlNecessities {
    /** Parsed and processed server hostname */
    private String host;
    /** Parsed and processed server port */
    private int port;
    /** Parsed and processed username */
    private String username;

    /** Hostname input TextField */
    public TextField ipInputTF;
    /** Port input TextField */
    public TextField portInputTF;
    /** Username input TextField */
    public TextField usernameInputTF;

    /** Button for connection */
    public Button connectBtn;
    /** Button for cancelation */
    public Button cancelBtn;

    /** Hostname error label */
    public Label ipErrorLbl;
    /** Port error label */
    public Label portErrorLbl;
    /** Username error label */
    public Label usernameErrorlbl;

    // Status bar elements
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    /**
     * Sets client instance and status bar for this scene
     * @param client client instance
     */
    public void setClient(Client client) {
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    /**
     * Basic input validation. Validates Hostname, Port, Username.
     * @param ip hostname
     * @param port port
     * @param username username
     * @return true on successful validation
     */
    private boolean validateInputs(String ip, String port, String username) {
        boolean ipGood, portGood, nameGood;

        ipGood = true;

        if(ip.length() > 64) {
            ipErrorLbl.setText("Maximum allowed length is 64 characters");
            ipErrorLbl.setVisible(true);

            ipGood = false;
        }

        try {
            int b = Integer.parseInt(port);
            if(b < 0 || b > 65336) throw new NumberFormatException();

            portErrorLbl.setVisible(false);

            portGood = true;
        }catch (NumberFormatException e) {
            portErrorLbl.setText("Entered value is invalid. Expecting value 1-65536");
//            portErrorLbl.setStyle("-fx-text-fill: #FF0000");
            portErrorLbl.setVisible(true);

            portGood = false;
        }

        if(username.isBlank()) {
            usernameErrorlbl.setText("Please enter a username");
            usernameErrorlbl.setVisible(true);

            nameGood = false;
        } else if(username.length() > 32) {
            usernameErrorlbl.setText("Username can be a max of 32 characters");
            usernameErrorlbl.setVisible(true);

            nameGood = false;
        } else {
                nameGood = true;
                usernameErrorlbl.setVisible(false);
        }

        return (ipGood && portGood && nameGood);
    }

    /**
     * Button event action. Sets Hostname, Port and Username in client instance and sets instruction to CONNECT.
     * @param actionEvent
     */
    public void connect(ActionEvent actionEvent) {
        if(validateInputs(ipInputTF.getText(), portInputTF.getText(), usernameInputTF.getText())) {
            host = ipInputTF.getText();
            port = Integer.parseInt(portInputTF.getText());
            username = usernameInputTF.getText();

            client.setUsername(username);
            client.setHost(host);
            client.setPort(port);

            client.setInstruction(Instruction.CONNECT);
        } else {
            if(client.getAutomaton().validateTransition(Action.INVALID_IN)) {
                client.getAutomaton().makeTransition(Action.INVALID_IN);
            } else {
                responseLabel.setText("Invalid automaton transition");
            }
        }
    }

    /**
     * Button event action. Cancels connection picking and returns user to main_menu_disconnected.fxml
     * @param actionEvent
     */
    public void cancel(ActionEvent actionEvent) {
        genericSetScene("main_menu_disconnected.fxml");

        if(client.getAutomaton().validateTransition(Action.CANCEL)) {
            client.getAutomaton().makeTransition(Action.CANCEL);
        } else {
            responseLabel.setText("Invalid automaton transition");
        }
    }
}
