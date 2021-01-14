package graphics;

import game.Client;
import game.PSColor;
import game.State;
import game.Stone;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import network.Instruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class GameboardCtrl extends OverlordCtrl implements CtrlNecessities {
    @FXML
    public GridPane board_gpane;

    public PSColor pc;

//    public HBox status;
//    @FXML
//    private StatusBarCtrl statusController;

    private LinkedList<Integer> highlightedPanes = new LinkedList<>();
    private LinkedList<Integer> moveSequence = new LinkedList<>();

    private Image red = new Image(getClass().getResource("/img/red_piece.png").toString());
    private Image blue = new Image(getClass().getResource("/img/blue_piece.png").toString());
    private Image redKing = new Image(getClass().getResource("/img/red_king_piece.png").toString());
    private Image blueKing = new Image(getClass().getResource("/img/blue_king_piece.png").toString());

    //status
    public Ellipse clientConnectCircle;
    public Label responseLabel;
    public Label clientStateLabel;
    public Label clientNameLabel;
    public Label clientConnectionLabel;
    public Ellipse opponentConnectCircle;
    public Label opponentConnectionLabel;
    public Label opponentNameLabel;

    @FXML
    public void initialize() {
        drawBoard();
    }

    private void drawBoard() {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Pane p = new Pane();

                p.setId(i * 8 + j + "");

                if((i + j) % 2 == 0) {
                    p.setStyle("-fx-background-color: lightyellow");
                } else {
                    p.setStyle("-fx-background-color: black");
                }

                GridPane.setConstraints(p, j, i);
                board_gpane.getChildren().add(p);
            }
        }
    }

    public void initStones() {
        ImageView ps, es;

        if(client == null) {
            System.err.println("Client shouldn't be null at this point");
        }

        PSColor playerColor = client.getGame().getPlayerColor();
        int[] gameBoard = client.getGame().getGameBoard();

        Image red = new Image(getClass().getResource("/img/red_piece.png").toString());
        Image blue = new Image(getClass().getResource("/img/blue_piece.png").toString());

        for(int i = 0; i < gameBoard.length; i ++) {
            Pane p = (Pane) board_gpane.getChildren().get(i);

            if(playerColor == PSColor.BLACK) {
                ps = new ImageView(red);
                es = new ImageView(blue);
            } else {
                es = new ImageView(red);
                ps = new ImageView(blue);
            }

            if(gameBoard[i] == 2) {
                es.fitWidthProperty().bind(p.widthProperty());
                es.fitHeightProperty().bind(p.heightProperty());

                es.setOnMouseClicked(event -> {
                        responseLabel.setText("Opponents stone. No action");
                });

//                es.setOnMouseClicked(event -> {
//                    System.out.println("Clicked stone");
//                });

                p.getChildren().add(es);
            } else if(gameBoard[i] == 1) {
                ps.fitWidthProperty().bind(p.widthProperty());
                ps.fitHeightProperty().bind(p.heightProperty());

                ps.setOnMouseClicked(event -> {
                    int paneID = Integer.parseInt(((ImageView) event.getSource()).getParent().getId());
                    imageViewEvent(paneID);

                    System.out.println("You clicked " + paneID);
//                    int paneID = Integer.parseInt(((ImageView) event.getSource()).getParent().getId());
//
//                    System.out.println("You clicked " + paneID + "!!");
//
////                    unHighlightMoves();
//
//                    if(((Pane) board_gpane.getChildren().get(paneID)).getChildren().isEmpty()) {
//                        System.out.println("This pane is empty");
//                        unHighlightMoves();
//                    }
//                    else {
//                        System.out.println("This pane has a stone!");
//                        unHighlightMoves();
//                        highlightMoves(paneID);
//                    }
                });

                p.getChildren().add(ps);
            } else {
                p.setOnMouseClicked(e -> {
                    responseLabel.setText("This field is empty.");
                    unHighlightMoves();
                });
            }
        }
    }

    private void imageViewEvent(int paneID) {
        if(((Pane) board_gpane.getChildren().get(paneID)).getChildren().isEmpty()) {
            unHighlightMoves();
        } else {
            unHighlightMoves();
            highlightMoves(paneID, false);
        }
    }

    public void unsetImageViewEventsExceptID(ArrayList<Integer> all, int id) {
        for(int i : all) {
            if(i == id) continue;
            if(((Pane) board_gpane.getChildren().get(i)).getChildren().isEmpty()) {
                System.err.println("Got wrong index of player stone! (unsetting)");
            } else {
                ((Pane) board_gpane.getChildren().get(i)).getChildren().get(0).setOnMouseClicked(e -> {
                    if(client.getAutomaton().getGameState() == State.OPPONENT_TURN) {
                        responseLabel.setText("Wait for your turn!");
                    } else {
                        responseLabel.setText("Somehow you can't move");
                    }
                });
            }
        }
    }

    public void setImageViewEvents(ArrayList<Integer> ids) {
        for(int i : ids) {
            if(((Pane) board_gpane.getChildren().get(i)).getChildren().isEmpty()) {
                System.err.println("Got wrong index of player stone! (setting)");
            } else {
                ((Pane) board_gpane.getChildren().get(i)).getChildren().get(0).setOnMouseClicked(e -> imageViewEvent(i));
            }
        }
    }

    public void unsetImageViewEvents(ArrayList<Integer> ids) {
        for(int i : ids) {
            if(((Pane) board_gpane.getChildren().get(i)).getChildren().isEmpty()) {
                System.err.println("Got wrong index of player stone! (setting)");
            } else {
                ((Pane) board_gpane.getChildren().get(i)).getChildren().get(0).setOnMouseClicked(e -> {
                    if(client.getAutomaton().getGameState() == State.OPPONENT_TURN) {
                        responseLabel.setText("Wait for your turn!");
                    } else {
                        responseLabel.setText("Somehow you can't move");
                    }
                });
            }
        }
    }

    private boolean highlightMoves(int paneID, boolean fakeSub) {
        ArrayList<Integer> hl = new ArrayList<>();
        client.getGame().getPossibleMoves(paneID, hl, fakeSub);

        if(!hl.isEmpty()) {
            for(int i : hl) {
                board_gpane.getChildren().get(i).setStyle("-fx-background-color: #00FF00");
                board_gpane.getChildren().get(i).setOnMouseClicked(event -> {
                    clickedHL(paneID, i);

//                    while(!highlightedPanes.isEmpty()) {
//                        Node s = board_gpane.getChildren().get(highlightedPanes.poll());
//                        s.setOnMouseClicked(null);
//                    }
                });

                highlightedPanes.add(i);
            }

            return true;
        } else {
            System.out.println("HIGHLIGHT LIST IS NULL");
            return false;
        }
    }

    private void clickedHL(int source, int clicked) {
        unHighlightMoves();

        if(moveSequence.isEmpty()) {
            moveSequence.addFirst(source);

            unsetImageViewEventsExceptID(client.getGame().getPlayerStoneIndexes(), source);
        }

        moveSequence.addLast(clicked);

        movePiece(source, clicked);

        if(client.getGame().canMoveAgain(source, clicked) && highlightMoves(clicked,true)) {
            System.out.println("more moves to make");

            try {
                getPaneWithID(clicked).getChildren().get(0).setOnMouseClicked(null);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Welp continuous");
            }

        } else {
            System.out.println("Out of moves. Sending to server");

            try {
                getPaneWithID(clicked).getChildren().get(0).setOnMouseClicked(null);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Welp");
            }

            System.out.print("Sequence");
            for(int i : moveSequence) {
                System.out.print(i + "  ");
            }
            System.out.println();

            client.setInstruction(Instruction.TURN);
            for(int i : moveSequence) {
                client.addRequestPar(i + "");
            }

            // TODO after server response enable pieces
        }
    }

    public void reverseMoveSequence() {
        Node n = ((Pane) board_gpane.getChildren().get(moveSequence.getLast())).getChildren().get(0);
        ((Pane) board_gpane.getChildren().get(moveSequence.getFirst())).getChildren().add(n);
        ((Pane) board_gpane.getChildren().get(moveSequence.getLast())).getChildren().remove(0);
    }


    private void removeHighlightOnClick() {
        while(!highlightedPanes.isEmpty()) {
            Node s = board_gpane.getChildren().get(highlightedPanes.poll());
            s.setOnMouseClicked(null);
        }
    }

    private void unHighlightMoves() {
        for(int i : highlightedPanes) {
            board_gpane.getChildren().get(i).setStyle("-fx-background-color: #000000");
        }

        removeHighlightOnClick();
    }

    public void setClient(Client client) {
//        setClient(client, statusController);
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    private Pane getPaneWithID(int id) {
        return (Pane) board_gpane.getChildren().get(id);
    }


    private void movePiece(int from, int to) {
        try {
            Node n = ((Pane) board_gpane.getChildren().get(from)).getChildren().get(0);
            ((Pane) board_gpane.getChildren().get(to)).getChildren().add(n);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Pane with id: " + from + " doesn't have stone");
        }
    }

    public void removeStones(ArrayList<Integer> ids) {
        for(int i : ids) {
            ((Pane) board_gpane.getChildren().get(i)).getChildren().clear();
        }
    }

    public void resetMoveSequence() {
        Node n = null;

        for(int i = 0; i < moveSequence.size(); i++) {
            if(!getPaneWithID(moveSequence.get(i)).getChildren().isEmpty()) {
                n = getPaneWithID(moveSequence.get(i)).getChildren().get(0);
                getPaneWithID(moveSequence.get(i)).getChildren().clear();
                break;
            }
        }

        if(n == null) {
            System.err.println("GUI lost stone on move");
            return;
        }

        getPaneWithID(moveSequence.getFirst()).getChildren().add(n);
        moveSequence.clear();
    }

    public void moveStone() {
        Node n = null;

        for(int i = 0; i < moveSequence.size(); i++) {
            if(!getPaneWithID(moveSequence.get(i)).getChildren().isEmpty()) {
                n = getPaneWithID(moveSequence.get(i)).getChildren().get(0);
                getPaneWithID(moveSequence.get(i)).getChildren().clear();
                break;
            }
        }

        if(n == null) {
            System.err.println("GUI lost stone on move");
            return;
        }

        getPaneWithID(moveSequence.getLast()).getChildren().add(n);
        moveSequence.clear();
    }

    public void moveOpponentStones(String[] seq) {
        for(int i = 1; i < seq.length; i++) {
            int from = 63 - Integer.parseInt(seq[i - 1]);
            int to = 63 - Integer.parseInt(seq[i]);

            try {
                Node n = getPaneWithID(from).getChildren().get(0);
                getPaneWithID(to).getChildren().add(n);
                getPaneWithID(from).getChildren().clear();
            } catch (IndexOutOfBoundsException e) {
                System.err.println("There is no opponent stone to move");
            }
        }
    }
}
