package graphics;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Ellipse;

/**
 * Class grouping status elements into one class for easier access
 */
public class StatusBar {
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    /**
     * Constructor
     * @param cCircle client connection circle
     * @param response server response label. Often used to inform user of current action
     * @param cState application state label
     * @param cName client name label
     * @param cConnection client connection label
     * @param oCircle opponent connection circle
     * @param oConnection opponent connection label
     * @param oName opponent name label
     */
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

    /**
     * Set response label text. Method for easier access
     * @param text to be displayed
     */
    public void setResponseText(String text) {
        Platform.runLater(() -> responseLabel.setText(text));
    }
}
