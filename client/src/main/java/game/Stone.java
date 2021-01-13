package game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

public class Stone {
    private boolean isKing = false;
    private PSColor color;
    private int indexPosition;

    public Stone(PSColor color, int indexPosition) {
        this.color = color;
        this.indexPosition = indexPosition;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public ImageView getStoneImageView() {
        ImageView iv;

        if (color == PSColor.WHITE) {
            if (isKing) {
                iv = new ImageView(getClass().getResource("/img/red_king_piece.png").toString());
            } else {
                iv = new ImageView(getClass().getResource("/img/red_piece.png").toString());
            }
        } else {
            if (isKing) {
                iv = new ImageView(getClass().getResource("/img/blue_king_piece.png").toString());
            } else {
                iv = new ImageView(getClass().getResource("/img/blue_piece.png").toString());
            }
        }

        return iv;

//        iv.fitWidthProperty().bind(p.widthProperty());
//        iv.fitHeightProperty().bind(p.heightProperty());
//
//        p.getChildren().add(iv);
    }

    public void drawStone(Pane p) {
        ImageView iv;

        //BLACK has red pieces, WHITE has blue (this is for better visibility, if there is time left -> make colors rigth?)
        if (color == PSColor.BLACK) {
            if (isKing) {
                iv = new ImageView(getClass().getResource("/img/red_king_piece.png").toString());
            } else {
                iv = new ImageView(getClass().getResource("/img/red_piece.png").toString());
            }
        } else {
            if (isKing) {
                iv = new ImageView(getClass().getResource("/img/blue_king_piece.png").toString());
            } else {
                iv = new ImageView(getClass().getResource("/img/blue_piece.png").toString());
            }
        }

        iv.fitWidthProperty().bind(p.widthProperty());
        iv.fitHeightProperty().bind(p.heightProperty());

        iv.setOnMouseClicked(event -> {
            System.out.println("Clicked image");
        });

        p.getChildren().add(iv);
    }

    public int getIndexPosition() {
        return indexPosition;
    }
}
