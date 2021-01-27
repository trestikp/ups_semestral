package graphics;

import game.Client;
import game.PSColor;
import game.State;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;
import network.Instruction;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * FXML controller for file connection_picker.fxml. Extends OverlordCtrl and implements CtrlNecessities.
 * Controls game board GUI.
 */
public class GameboardCtrl extends OverlordCtrl implements CtrlNecessities {
    /** Game Board pane */
    @FXML
    public GridPane board_gpane;

    /** List of panes player can move to from selected position */
    private LinkedList<Integer> highlightedPanes = new LinkedList<>();
    /** Sequence in which player clicked fields */
    private LinkedList<Integer> moveSequence = new LinkedList<>();

    /** Red stone image used to generate ImageViews (Image is 2000x2000 px takes long to load) */
    private final Image red = new Image(getClass().getResource("/img/red_piece.png").toString());
    /** Red stone image used to generate ImageViews (Image is 2000x2000 px takes long to load) */
    private final Image blue = new Image(getClass().getResource("/img/blue_piece.png").toString());
    /** Red stone image used to generate ImageViews (Image is 2000x2000 px takes long to load) */
    private final Image redKing = new Image(getClass().getResource("/img/red_king_piece.png").toString());
    /** Red stone image used to generate ImageViews (Image is 2000x2000 px takes long to load) */
    private final Image blueKing = new Image(getClass().getResource("/img/blue_king_piece.png").toString());

    private int firstClick;


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
     * Controller creation calls
     */
    @FXML
    public void initialize() {
        drawBoard();
    }

    /**
     * Prepares chess board in @board_gpane
     */
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

    /**
     * Initializes stones to board_gpane
     */
    public void initStones() {
        ImageView ps, es;

        if(client == null) {
            System.err.println("Client shouldn't be null at this point");
        }

        PSColor playerColor = client.getGame().getPlayerColor();
        int[] gameBoard = client.getGame().getGameBoard();

        for(int i = 0; i < gameBoard.length; i ++) {
            Pane p = (Pane) board_gpane.getChildren().get(i);

            if(gameBoard[i] == 3 || gameBoard[i] == 4) {
                if(playerColor == PSColor.BLACK) {
                    ps = new ImageView(redKing);
                    es = new ImageView(blueKing);
                } else {
                    es = new ImageView(redKing);
                    ps = new ImageView(blueKing);
                }
            } else {
                if(playerColor == PSColor.BLACK) {
                    ps = new ImageView(red);
                    es = new ImageView(blue);
                } else {
                    es = new ImageView(red);
                    ps = new ImageView(blue);
                }
            }

            if(gameBoard[i] > 0) {
                if (gameBoard[i] == 2 || gameBoard[i] == 4) {
                    es.fitWidthProperty().bind(p.widthProperty());
                    es.fitHeightProperty().bind(p.heightProperty());

                    es.setOnMouseClicked(event -> {
                        responseLabel.setText("Opponents stone. No action");
                    });

                    p.getChildren().add(es);
                } else {
                    ps.fitWidthProperty().bind(p.widthProperty());
                    ps.fitHeightProperty().bind(p.heightProperty());

                    ps.setOnMouseClicked(event -> {
                        int paneID = Integer.parseInt(((ImageView) event.getSource()).getParent().getId());
                        imageViewEvent(paneID);

                        System.out.println("You clicked " + paneID);
                    });

                    p.getChildren().add(ps);
                }
            }

            else {
                p.setOnMouseClicked(e -> {
                    responseLabel.setText("This field is empty.");
                    unHighlightMoves();
                });
            }
        }
    }

    /**
     * Event set to players ImageView. Highlights fields which can be moved to from clicked ImageView.
     * Unhighlights fields when clicked on empty field.
     * @param paneID id of pane containing ImageView
     */
    private void imageViewEvent(int paneID) {
        if(((Pane) board_gpane.getChildren().get(paneID)).getChildren().isEmpty()) {
            unHighlightMoves();
        } else {
            unHighlightMoves();
            highlightMoves(paneID);
        }
    }

    /**
     * After player makes a move, removes action from other stones so player can't move with different stone.
     * @param all list of stone positions
     * @param id moved stone. Is exempted from action removal
     */
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

    /**
     * Sets ImageView events when players turn begins.
     * @param ids players stone locations
     */
    public void setImageViewEvents(ArrayList<Integer> ids) {
        for(int i : ids) {
            if(((Pane) board_gpane.getChildren().get(i)).getChildren().isEmpty()) {
                System.err.println("Got wrong index of player stone! (setting)");
            } else {
                ((Pane) board_gpane.getChildren().get(i)).getChildren().get(0).setOnMouseClicked(e -> imageViewEvent(i));
            }
        }
    }

    /**
     * Unsets all ImageView events when it's opponents turn
     * @param ids players stone locations
     */
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

    /**
     * Highlights possible moves from clicked location
     * @param paneID id of clicked location
     */
    private void highlightMoves(int paneID) {
        ArrayList<Integer> hl = new ArrayList<>();
        client.getGame().getPossibleMoves(paneID, hl, false, paneID, 0);
//        client.getGame().getPossibleMoves_v2(paneID, hl, 0, paneID);

        if(!hl.isEmpty()) {
            for(int i : hl) {
                getPaneWithID(i).setStyle("-fx-background-color: #00FF00");
                getPaneWithID(i).setOnMouseClicked(event -> {
                    clickedHL(paneID, i);
                });

                highlightedPanes.add(i);
            }
        } else {
            System.out.println("HIGHLIGHT LIST IS NULL");
        }
//        if(!hl.isEmpty()) {
//            for(int i : hl) {
//                if(Math.abs(paneID - i) > 2 * 9) {
//                    getPaneWithID(i).setStyle("-fx-background-color: #00FF00");
//                } else {
//                    getPaneWithID(i).setStyle("-fx-background-color: YELLOW");
//                    getPaneWithID(i).setOnMouseClicked(event -> {
//                        clickedHL(paneID, i);
//                    });
//                }
//
//                highlightedPanes.add(i);
//            }
//        } else {
//            System.out.println("HIGHLIGHT LIST IS NULL");
//        }
    }

    /**
     * Mouse clicked event of highlighted pane. Moves piece
     * @param source from
     * @param clicked to
     */
    private void clickedHL(int source, int clicked) {
        unHighlightProximity(source);
//        unHighlightMoves();

        if(moveSequence.isEmpty()) {
            moveSequence.addFirst(source);
            firstClick = source;

            unsetImageViewEventsExceptID(client.getGame().getPlayerStoneIndexes(), source);
        }

        moveSequence.addLast(clicked);

        movePiece(source, clicked);

        //blah
//        highlightedPanes.clear();
//
//        ArrayList<Integer> plsWork = new ArrayList<>();
//        client.getGame().getPossibleMoves_v2(clicked, plsWork, (clicked - source), firstClick);
//
//        for(int i : plsWork) {
//            getPaneWithID(i).setStyle("-fx-background-color: #00FF00");
//            getPaneWithID(i).setOnMouseClicked(event -> {
//                clickedHL(clicked, i);
//            });
//
//            highlightedPanes.add(i);
//        }
        //blah

        if(client.getGame().canMoveAgain(source, clicked) && !highlightedPanes.isEmpty()) {
            System.out.println("more moves to make");

            try {
                getPaneWithID(clicked).getChildren().get(0).setOnMouseClicked(null);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Welp continuous");
            }

            highlightProximity(clicked);

        } else {
            System.out.println("Out of moves. Sending to server");

            unHighlightMoves(); //if user chooses not to move in a sequence, possible sequence stays highlighted

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
        }
    }

    private void highlightPanes(int source) {
        for(int i : highlightedPanes) {
            getPaneWithID(i).setStyle("-fx-background-color: #00FF00");
            getPaneWithID(i).setOnMouseClicked(event -> {
                clickedHL(source, i);
            });
        }
    }

    /**
     * Removes onClick event from highlighted panes
     */
    private void removeHighlightOnClick() {
        while(!highlightedPanes.isEmpty()) {
            Node s = board_gpane.getChildren().get(highlightedPanes.poll());
            s.setOnMouseClicked(null);
        }
    }

    /**
     * Unhighlights all possible moves
     */
    private void unHighlightMoves() {
        for(int i : highlightedPanes) {
            board_gpane.getChildren().get(i).setStyle("-fx-background-color: #000000");
        }

        removeHighlightOnClick();
    }

    /**
     * Unhighlights possible moves with reach of one move
     * @param id pane around which highlight are removed
     */
    private void unHighlightProximity(int id) {
        ArrayList<Integer> test = new ArrayList<>();

        for(int i : highlightedPanes) {
            if(Math.abs(id - i) <= 2 * 9) {
                getPaneWithID(i).setStyle("-fx-background-color: #000000");
                getPaneWithID(i).setOnMouseClicked(null);
                test.add(i);
            }
        }

        for(int i : test) {
            highlightedPanes.remove((Integer) i);
        }
    }

    /**
     * Highlights panes in proximity
     * @param id pane around which panes are highlighted
     */
    private void highlightProximity(int id) {
        for(int i : highlightedPanes) {
            if(Math.abs(id - i) <= 2 * 9) {
                getPaneWithID(i).setStyle("-fx-background-color: YELLOW");
                getPaneWithID(i).setOnMouseClicked(event -> {
                    clickedHL(id, i);
                });
            }
        }
    }

    /**
     * Ref. to @CtrlNecessities
     * @param client instance
     */
    public void setClient(Client client) {
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }

    /**
     * Saves a bit of writing
     * @param id of clicked pane
     * @return Pane
     */
    private Pane getPaneWithID(int id) {
        return (Pane) board_gpane.getChildren().get(id);
    }

    /**
     * Move piece in GUI only
     * @param from pane id
     * @param to pane id
     */
    private void movePiece(int from, int to) {
        try {
            Node n = ((Pane) board_gpane.getChildren().get(from)).getChildren().get(0);
            ((Pane) board_gpane.getChildren().get(to)).getChildren().add(n);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Pane with id: " + from + " doesn't have stone");
        }
    }

    /**
     * Removes stones from panes
     * @param ids list of pane ids
     */
    public void removeStones(ArrayList<Integer> ids) {
        for(int i : ids) {
            ((Pane) board_gpane.getChildren().get(i)).getChildren().clear();
        }

        ids.clear();
    }

    /**
     * If server fails to validate move, returns piece to starting location
     */
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

    /**
     * When stone reaches backline change image to king stone
     * @param position reached position
     */
    public void upgradePiece(int position) {
        Pane p = getPaneWithID(position);
        ImageView img = (ImageView) p.getChildren().get(0);

        // == nor equals() works but this does...
        if(img.getImage().getUrl().equals(red.getUrl())) {
            img.setImage(redKing);
        } else if(img.getImage().getUrl().equals(blue.getUrl())) {
            img.setImage(blueKing);
        } else {
            responseLabel.setText("This is an unknown piece! Cannot upgrade");
        }
    }

    /**
     * Moves stone to the end of move sequence after server verification. Theoretically shouldn't be necessary
     */
    public void moveStone() {
        ImageView n = null;

        for(int i = 0; i < moveSequence.size(); i++) {
            if(!getPaneWithID(moveSequence.get(i)).getChildren().isEmpty()) {
                n = (ImageView) getPaneWithID(moveSequence.get(i)).getChildren().get(0);
                getPaneWithID(moveSequence.get(i)).getChildren().clear();
                break;
            }
        }

        if(n == null) {
            System.err.println("GUI lost stone on move");
            return;
        }

        getPaneWithID(moveSequence.getLast()).getChildren().add(n);

        System.err.println("Target location: " + moveSequence.getLast());

        if(moveSequence.getLast() < 8) {
            System.err.println("Upgrading piece");
            upgradePiece(moveSequence.getLast());
        }

        moveSequence.clear();
    }

    /**
     * Performs opponent move sequence. Theoretically could only move stone from first position to last, because
     * it's verified by server
     * @param seq move sequence
     */
    public void moveOpponentStones(String[] seq) {
        for(int i = 1; i < seq.length; i++) {
            int from = 63 - Integer.parseInt(seq[i - 1]);
            int to = 63 - Integer.parseInt(seq[i]);

            try {
                Node n = getPaneWithID(from).getChildren().get(0);
                getPaneWithID(to).getChildren().add(n);
                getPaneWithID(from).getChildren().clear();

                if(to >= 56) {
                    upgradePiece(to);
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println("There is no opponent stone to move");
            }
        }
    }
}
