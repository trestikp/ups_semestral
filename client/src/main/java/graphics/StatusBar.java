package graphics;

import game.State;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Ellipse;

public class StatusBar {
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    public StatusBar(Ellipse cCircle, Label response, Label cState, Label cName, Label cConnection, Ellipse oCircle,
                     Label oConnection, Label oName) {
        this.clientConnectCircle = cCircle;
        this.responseLabel = response;
        this.clientStateLabel = cState;
        this.clientNameLabel = cName;
        this.clientConnectionLabel = cConnection;
        this.opponentConnectCircle = oCircle;
        this.opponentConnectionLabel = oConnection;
        this.opponentNameLabel = oName;
    }

    public void setResponseText(String text) {
        Platform.runLater(() -> responseLabel.setText(text));
    }
}
