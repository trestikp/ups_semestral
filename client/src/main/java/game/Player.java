package game;

import java.util.ArrayList;

public class Player {
    private PSColor color;
    private ArrayList<Stone> stones = new ArrayList<>();
    private boolean isEnemy;

    public Player(PSColor color, boolean isEnemy) {
        this.color = color;
        this.isEnemy = isEnemy;
        int startingRow = 5;

        if (isEnemy) startingRow = 0;

        for (int i = startingRow; i < (startingRow + 3); i++) {
            for (int j = 0; j < 8; j++) {
                if ((i + j) % 2 == 1) {
                    stones.add(new Stone(color, i * 8 + j));
                }
            }
        }
    }

    public PSColor getColor() {
        return color;
    }

    public ArrayList<Stone> getStones() {
        return stones;
    }
}
