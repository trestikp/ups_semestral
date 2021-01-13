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


public class ConnectionPickerCtrl_v2 extends OverlordCtrl implements CtrlNecessities {
//    private Client client;

    private String host;
    private int port;
    private String username;

    public TextField ipInputTF;
    public TextField portInputTF;
    public TextField usernameInputTF;

    public Button connectBtn;
    public Button cancelBtn;

    public Label ipErrorLbl;
    public Label portErrorLbl;
    public Label usernameErrorlbl;

    //status
//    public HBox status;
//    @FXML
//    public StatusBarCtrl statusController;

    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;


    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);

//        client.setStatusElements(clientConnectCircle, clientConnectionLabel, clientNameLabel, clientStateLabel,
//                                 responseLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
//        client.updateStatusElements();
    }

    private boolean validateInputs(String ip, String port, String username) {
        boolean allGood = false, ipGood = false, portGood = false, nameGood = false;

        ipGood = true;
//        if(ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
//            ip_error_label.setVisible(false);
//
//            ipGood = true;
//        } else {
//            //TODO: matching IPv6 ?, hostname?
//
//            ip_error_label.setText("Entered value is not an IPv4 address!");
//            ip_error_label.setStyle("-fx-text-fill: #FF0000");
//            ip_error_label.setVisible(true);
//
//            ipGood = false;
//        }

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
            nameGood = false;

            usernameErrorlbl.setText("Please enter a username");
            usernameErrorlbl.setVisible(true);
        } else {
            nameGood = true;
            usernameErrorlbl.setVisible(false);
        }

        return (ipGood && portGood && nameGood);
    }

    public void connect(ActionEvent actionEvent) {
        if(validateInputs(ipInputTF.getText(), portInputTF.getText(), usernameInputTF.getText())) {
            host = ipInputTF.getText();
            port = Integer.parseInt(portInputTF.getText());
            username = usernameInputTF.getText();

            client.setUsername(username);
            client.setHost(host);
            client.setPort(port);

            client.establishConnection();

            client.setInstruction(Instruction.CONNECT);
        } else {
            if(client.getAutomaton().validateTransition(Action.INVALID_IN)) {
                client.getAutomaton().makeTransition(Action.INVALID_IN);
            } else {
                responseLabel.setText("Invalid automaton transition");
//                statusController.responseLabel.setText("Invalid automaton transition");
            }
        }
    }

    public void cancel(ActionEvent actionEvent) {
        genericSetScene("main_menu_disconnect_v2.fxml");

        if(client.getAutomaton().validateTransition(Action.CANCEL)) {
            client.getAutomaton().makeTransition(Action.CANCEL);
        } else {
            responseLabel.setText("Invalid automaton transition");
//            statusController.responseLabel.setText("Invalid automaton transition");
        }
    }
}
