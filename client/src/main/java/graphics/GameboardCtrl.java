package graphics;

import game.Client;
import game.Game;
import game.Stone;
import javafx.event.EventDispatchChain;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Ellipse;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class GameboardCtrl extends OverlordCtrl implements CtrlNecessities {
    @FXML
    public AnchorPane main_game_pane;
    @FXML
    public AnchorPane head_apane;
    @FXML
    public GridPane board_gpane;

    private Client client;

    private Queue<Integer> highlightedPanes = new LinkedList<>();

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
        main_game_pane.widthProperty().addListener((event) -> {
            resize_gameboard();
        });
        main_game_pane.heightProperty().addListener((event) -> {
            resize_gameboard();
        });

        draw_board();

        client = Handler.getClient();
//        System.out.println("Initialized Gameboard with game ID: " + client.getGame().getId());
    }

    private void resize_gameboard() {
        double avail_width = 0, avail_height = 0;

        // 10 is anchor constraint from bottom
        avail_height = main_game_pane.getHeight() - head_apane.getHeight() - 10;
        avail_width = main_game_pane.getWidth() - 20;

        if(avail_height < avail_width) {
            AnchorPane.setTopAnchor(board_gpane, head_apane.getHeight());
            AnchorPane.setLeftAnchor(board_gpane, (avail_width - avail_height) / 2);
            AnchorPane.setRightAnchor(board_gpane, (avail_width - avail_height) / 2);
            // bottom is constant 10 from FXML
            AnchorPane.setBottomAnchor(board_gpane, 10.0);
        } else {
            AnchorPane.setTopAnchor(board_gpane, (avail_height - avail_width) / 2 + head_apane.getHeight());
            AnchorPane.setLeftAnchor(board_gpane, 10.0);
            AnchorPane.setRightAnchor(board_gpane, 10.0);
            AnchorPane.setBottomAnchor(board_gpane, (avail_height - avail_width) / 2);
        }
    }

    private void draw_board() {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Pane p = new Pane();

                p.setId(i * 8 + j + "");

                if((i + j) % 2 == 0) {
                    p.setStyle("-fx-background-color: yellow");
                } else {
                    p.setStyle("-fx-background-color: black");
                }

                p.setOnMouseClicked((event) -> {
                    System.out.println("You clicked " + p.getId() + "!!");

                    unHighlightMoves();

                    if(p.getChildren().isEmpty()) {
                        System.out.println("This pane is empty");
                    }
                    else {
                        System.out.println("This pane has a stone!");
                        highlightMoves(Integer.parseInt(p.getId()));
                    }
                });

                GridPane.setConstraints(p, j, i);
                board_gpane.getChildren().add(p);
            }
        }
    }

    public void drawStones(ArrayList<Stone> stones) {
        for(Stone s : stones) {
            for(Node n : board_gpane.getChildren()) {
                if(s.getIndexPosition() == Integer.parseInt(n.getId())) {
                    s.drawStone((Pane) n);
                }
            }
//            baf b = new baf(s, board_gpane.getChildren());
//            b.start();
        }
    }

    private void highlightMoves(int paneID) {
//        ArrayList<Integer> hl = client.getGame().getPossibleMoves(paneID);
//
//        if(hl != null) {
//            for(int i : hl) {
//                board_gpane.getChildren().get(i).setStyle("-fx-background-color: #00FF00");
//                highlightedPanes.add(i);
//            }
//        } else {
//            System.out.println("HIGHLIGHT LIST IS NULL");
//        }
    }

    private void unHighlightMoves() {
        while(!highlightedPanes.isEmpty()) {
            board_gpane.getChildren().get(highlightedPanes.poll()).setStyle("-fx-background-color: #000000");
        }
    }

    public void setClient(Client client) {
        StatusBar status = new StatusBar(clientConnectCircle, responseLabel, clientStateLabel, clientNameLabel,
                clientConnectionLabel, opponentConnectCircle, opponentConnectionLabel, opponentNameLabel);
        setClient(client, status);
    }
}
