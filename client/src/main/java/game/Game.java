package game;


import java.util.ArrayList;

public class Game {
//    private Player player, enemy;
    private PSColor player, enemy;

//    private GameboardCtrl gbCtrl;
    private int[] gameBoard;
    private ArrayList<Integer> playerStoneIndexes;
    private ArrayList<Integer> jumpedOver;
    private ArrayList<Integer> opponentJumpedOver;

    public Game(PSColor playerColor) {
//        player = new Player(playerColor, false);
//
//        if(playerColor == PSColor.WHITE) {
//            enemy = new Player(PSColor.BLACK, true);
//        } else {
//            enemy = new Player(PSColor.WHITE, true);
//        }
        player = playerColor;
        playerStoneIndexes = new ArrayList<>();
        jumpedOver = new ArrayList<>();
        opponentJumpedOver = new ArrayList<>();

        if(playerColor == PSColor.WHITE) {
            enemy = PSColor.BLACK;
        } else {
            enemy = PSColor.WHITE;
        }

        gameBoard = new int[]{
                //-1 represents "WHITE" field (not possible), 0 available field ("BLACK")
                // 1 "player" stone, 3 "player" king, 2 "enemy" stone, 4 "enemy king
              // 0    1    2    3    4    5    6    7
                -1 ,  2 , -1 ,  2 , -1 ,  2 , -1 ,  2, //0
                 2 , -1 ,  2 , -1 ,  2 , -1 ,  2 , -1, //1
                -1 ,  2 , -1 ,  2 , -1 ,  2 , -1 ,  2, //2
                 0 , -1 ,  0 , -1 ,  0 , -1 ,  0 , -1, //3
                -1 ,  0 , -1 ,  0 , -1 ,  0 , -1 ,  0, //4
                 1 , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //5
                -1 ,  1 , -1 ,  1 , -1 ,  1 , -1 ,  1, //6
                 1 , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //7

//                // 0    1    2    3    4    5    6    7
//                -1 ,  2 , -1 ,  0 , -1 ,  2 , -1 ,  2, //0
//                2  , -1 ,  2 , -1 ,  0 , -1 ,  2 , -1, //1
//                -1 ,  0 , -1 ,  0 , -1 ,  2 , -1 ,  2, //2
//                0  , -1 ,  2 , -1 ,  2 , -1 ,  0 , -1, //3
//                -1 ,  0 , -1 ,  1 , -1 ,  2 , -1 ,  0, //4
//                1  , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //5
//                -1 ,  1 , -1 ,  1 , -1 ,  1 , -1 ,  1, //6
//                1  , -1 ,  1 , -1 ,  1 , -1 ,  1 , -1, //7
        };

        for(int i = 40; i < gameBoard.length; i++) {
            if(gameBoard[i] == 1)
                playerStoneIndexes.add(i);
        }
    }

//    public Player getPlayer() {
//        return player;
//    }
//
//    public Player getEnemy() {
//        return enemy;
//    }

//    public ArrayList<Integer> getPossibleMoves(int indexPosition) {
//        ArrayList<Integer> possibilities = null;
//
//        switch (gameBoard[indexPosition]) {
//            case 1:
//            case 2: possibilities = getStonePossibilities(indexPosition); break;
//            case 3:
//            case 4: System.out.println("This is enemy stone!"); break; //TODO: (gameboard console?) enemy stone!
//            case 0: break;
//            default: System.out.println("OH DUCK");
//        }
//
//        return possibilities;
//    }

    public void moveFromTo(int source, int vector) {
        gameBoard[source + vector] = gameBoard[source];

        if(Math.abs(vector) > 9) {
            jumpedOver.add(source + (vector / 2));
        }

        gameBoard[source] = 0;

//        return vector != -7 && vector != -9 && vector != 7 && vector != 9;
    }

    public void moveOpponentFromTo(int source, int target) {
        source = 63 - source;
        target = 63 - source;
        int vector = target - source;

        gameBoard[source + vector] = gameBoard[source];

        if(Math.abs(vector) > 9) {
            opponentJumpedOver.add(source + (vector / 2));
        }

        gameBoard[source] = 0;
    }


    public void getPossibleMoves(int indexPosition, ArrayList<Integer> positions, boolean isSubsquent) {
        int ret = -1;

        //7,9 - player is always "down" and gameboard starts with index 0 in the top left corner

        if((ret = getDirectionTarget(indexPosition, -7)) != -1) {
            if(isSubsquent) {
                if(ret != indexPosition - 7) {
                    positions.add(ret);
                    getPossibleMoves(ret, positions, true);
                }
            } else {
                positions.add(ret);
            }
        }

        if((ret = getDirectionTarget(indexPosition, -9)) != -1) {
            if(isSubsquent) {
                if(ret != indexPosition - 9) {
                    positions.add(ret);
                    getPossibleMoves(ret, positions, true);
                }
            } else {
                positions.add(ret);
            }
        }

        //if stone @indexPosition is a king
        if(gameBoard[indexPosition] == 3) {
            if((ret = getDirectionTarget(indexPosition, +7)) != -1) {
                if(isSubsquent) {
                    if(ret != indexPosition + 7) {
                        positions.add(ret);
                        getPossibleMoves(ret, positions, true);
                    }
                } else {
                    positions.add(ret);
                }
            }

            if((ret = getDirectionTarget(ret, +9)) != -1) {
                if(isSubsquent) {
                    if(ret != indexPosition + 9) {
                        positions.add(ret);
                        getPossibleMoves(ret, positions, true);
                    }
                } else {
                    positions.add(ret);
                }
            }
        }
    }

    private int getDirectionTarget(int indexPosition, int direction) {
        int target = indexPosition + direction;

        if(target < 0 || target > 63) { //target is out of board
            return -1;
        } else if(gameBoard[target] == 0) { //target is empty field
            return target;
        } else if((gameBoard[target] % 2) == 0) { //target is occupied by opponent stone
            target += direction;

            if(target < 0 || target > 63) return -1;
            if(gameBoard[target] == 0) return target;
        }

        return -1;
    }

    public int[] getGameBoard() {
        return gameBoard;
    }

    public PSColor getPlayerColor() {
        return player;
    }

    public void printGameBoard() {
        System.out.println("===current gameboard===");
        for(int i = 0; i < gameBoard.length; i++) {
            System.out.print(gameBoard[i] + "  ");
            if(i % 8 == 7) {
                System.out.println("");
            }
        }
    }

    public ArrayList<Integer> getPlayerStoneIndexes() {
        return playerStoneIndexes;
    }

    public ArrayList<Integer> getJumpedOver() {
        return jumpedOver;
    }

    public ArrayList<Integer> getOpponentJumpedOver() {
        return opponentJumpedOver;
    }

    //FUCK IT, BRUTE FORCE TIME
    public void updatePlayerStoneIndexes() {
        playerStoneIndexes.clear();
        for(int i = 0; i < gameBoard.length; i++) {
            if((gameBoard[i] % 2) == 1) {
                playerStoneIndexes.add(i);
            }
        }
    }

    public void removeIndexFromStones(int index) {
        for(int i = 0; i < playerStoneIndexes.size(); i++) {
            if(playerStoneIndexes.get(i) == index) {
                playerStoneIndexes.remove(i);
                return;
            }
        }
    }
}


