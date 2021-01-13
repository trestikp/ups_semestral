package graphics;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Ellipse;

public class StatusBarCtrl {

    public Ellipse clientConnectCircle;
    public Label clientConnectionLabel;
    public Label clientNameLabel;
    public Label clientStateLabel;
    public Label responseLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    public void initialize() {

    }

    public void setResponseText(String text) {
        Platform.runLater(() -> responseLabel.setText(text));
    }
}
