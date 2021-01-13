package graphics;

import game.Action;
import game.Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import network.Instruction;
import network.TcpConnection;
import network.TcpMessage;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class ConnectionPickerCtrl {
    private Client client;

    private String host;
    private int port;
    private String username;

    public Label ip_error_label;
    public TextField ip_input;
    public Label port_error_label;
    public TextField port_input;
    public Label username_error_label;
    public TextField username_input;

    public void setClient(Client client) {
        this.client = client;
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

            port_error_label.setVisible(false);

            portGood = true;
        }catch (NumberFormatException e) {
            port_error_label.setText("Entered value is invalid. Expecting value 1-65536");
            port_error_label.setStyle("-fx-text-fill: #FF0000");
            port_error_label.setVisible(true);

            portGood = false;
        }

        nameGood = true;

        return (ipGood && portGood && nameGood);
    }

    public void connect(ActionEvent actionEvent) {
        if(validateInputs(ip_input.getText(), port_input.getText(), username_input.getText())) {
            host = ip_input.getText();
            port = Integer.parseInt(port_input.getText());
            username = username_input.getText();

            client.setUsername(username);
            client.setInstruction(Instruction.CONNECT);
        } else {
            //TODO: ?? do something on invalid input??
        }
    }

    public void cancel(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_res/main_menu_disconnected.fxml"));
        Parent pane;

        try {
            pane = loader.load();
        } catch (IOException e) {
            //TODO: logger
            System.out.println("Failed to load fxml");
            return;
        }

        Handler.setScene(new Scene(pane, Handler.getPrimaryStage().getWidth(), Handler.getPrimaryStage().getHeight()));
//        Handler.setSceneFromPath("/fxml_res/main_menu_disconnected.fxml");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }
}
