package game;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Class handles game logic
 */
public class Game {
    /** Players stone color */
    private PSColor player;

    /** Game board representation */
    private int[] gameBoard;
    /** Players stone indexes in @gameBoard */
    private ArrayList<Integer> playerStoneIndexes;
    /** List of indexes in @gameBoard player jumped over on his turn */
    private ArrayList<Integer> jumpedOver;
    /** List of indexes in @gameBoard opponent jumped over on his turn */
    private ArrayList<Integer> opponentJumpedOver;

    /**
     * Game constructor. Initializes @gameBoard and inits @playerStoneIndexes
     * @param playerColor players color
     */
    public Game(PSColor playerColor) {
        player = playerColor;
        playerStoneIndexes = new ArrayList<>();
        jumpedOver = new ArrayList<>();
        opponentJumpedOver = new ArrayList<>();

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
//                -1 ,  2 , -1 ,  2 , -1 ,  3 , -1 ,  2, //0
//                2  , -1 ,  2 , -1 ,  0 , -1 ,  2 , -1, //1
//                -1 ,  2 , -1 ,  0 , -1 ,  2 , -1 ,  2, //2
//                0  , -1 ,  2 , -1 ,  0 , -1 ,  0 , -1, //3
//                -1 ,  0 , -1 ,  0 , -1 ,  0 , -1 ,  0, //4
//                0  , -1 ,  0 , -1 ,  1 , -1 ,  1 , -1, //5
//                -1 ,  1 , -1 ,  2 , -1 ,  1 , -1 ,  1, //6
//                1  , -1 ,  1 , -1 ,  0 , -1 ,  1 , -1, //7
        };

        for(int i = 40; i < gameBoard.length; i++) {
            if(gameBoard[i] == 1 || gameBoard[i] == 3)
                playerStoneIndexes.add(i);
        }
    }

    /**
     * Player move in @gameBoard
     * @param source from
     * @param vector direction and size of move
     */
    public void moveFromTo(int source, int vector) {
        if((source + vector) < 8) {
            gameBoard[source + vector] = 3; //3 = players king
        } else {
            gameBoard[source + vector] = gameBoard[source];
        }

        if(Math.abs(vector) > 9) {
            jumpedOver.add(source + (vector / 2));
        }

        gameBoard[source] = 0;
    }

    /**
     * Opponents move in @gameBoard, has reverse indexes
     * @param source from
     * @param target to
     */
    public void moveOpponentFromTo(int source, int target) {
        source = 63 - source;
        target = 63 - target;
        int vector = target - source;

        if(source + vector >= 56) {
            gameBoard[source + vector] = 4;
        } else {
            gameBoard[source + vector] = gameBoard[source];
        }

        if(Math.abs(vector) > 9) {
            opponentJumpedOver.add(source + (vector / 2));
        }

        gameBoard[source] = 0;
    }

    public void moveFromTo_v2(int source, int to, boolean opponent) {
        int vect = getDirectionFromVector(to - source);
        int it = source;

        System.err.println("vect: " + vect);

        while(it != to) {
            System.out.println("Moving over: " + it);
            //returning false here could potentially break gameboard
            if(opponent) {
                if(gameBoard[it] == 1 || gameBoard[it] == 3) opponentJumpedOver.add(it);
            } else {
                if(gameBoard[it] == 2 || gameBoard[it] == 4) jumpedOver.add(it);
            }

            gameBoard[it] = 0;

            it += vect;
        }
    }


    /**
     * Fills array list with possible move locations. Recursively!
     * @param indexPosition source location index
     * @param positions list of positions that is filled
     * @param onlyOver only add indexes jumping over opponents stone
     * @param originalPosition first indexPosition
     * @param bannedDir direction in which recursion cannot move (to prevent backtracking)
     */
    public void getPossibleMoves(int indexPosition, ArrayList<Integer> positions, boolean onlyOver,
                                 int originalPosition, int bannedDir) {
        int ret = -1;

        if((ret = getDirectionTarget(indexPosition, -7)) != -1 && bannedDir != -7) {
            if((ret == indexPosition + 2 * -7)) {
                positions.add(ret);
                getPossibleMoves(ret, positions, true, originalPosition, 7);
            } else {
                if(!onlyOver) positions.add(ret);
            }
        }

        if((ret = getDirectionTarget(indexPosition, -9)) != -1 && bannedDir != -9) {
            if((ret == indexPosition + 2 * -9)) {
                positions.add(ret);
                getPossibleMoves(ret, positions, true, originalPosition, 9);
            } else {
                if(!onlyOver) positions.add(ret);
            }
        }

        if(gameBoard[originalPosition] == 3) {
            if ((ret = getDirectionTarget(indexPosition, +7)) != -1 && bannedDir != 7) {
                if ((ret == indexPosition + 2 * +7)) {
                    positions.add(ret);
                    getPossibleMoves(ret, positions, true, originalPosition, -7);
                } else {
                    if(!onlyOver) positions.add(ret);
                }
            }

            if ((ret = getDirectionTarget(indexPosition, +9)) != -1 && bannedDir != 9) {
                if ((ret == indexPosition + 2 * +9)) {
                    positions.add(ret);
                    getPossibleMoves(ret, positions, true, originalPosition, -9);
                } else {
                    if(!onlyOver) positions.add(ret);
                }
            }
        }
    }

    public void getPossibleMoves_v2(int indexPosition, ArrayList<Integer> positions,
                                    int bannedVect, int originalPosition) {
        int start, target;
        boolean isKing;
        int[] dirVectors = {-7, -9, 7, 9};
        ArrayList<Integer> temp = new ArrayList<>();
        boolean willJumpOver = false;

        if(gameBoard[originalPosition] == 3) {
            isKing = true;
        } else if(gameBoard[originalPosition] == 1) {
            isKing = false;
        } else {
            System.err.println("Somehow player tried to get moves for field without his stone");
            return;
        }

        if(isKing) {
            for(int i = 0; i < 4; i++) {
                //-dir from bannedVect!!! (bannedVect is actual move vector, and stone can't move in opposite direction)
                if(dirVectors[i] == -getDirectionFromVector(bannedVect)) continue;

                start = indexPosition;

                //longest row is 8, but lets just give 2 more for sure :)
                for(int j = 0; j < 10; j++) { //for is used so the program can't get hung up on an infinite while
                    target = getDirectionTarget(start, dirVectors[i]);

                    if (target == -1) {
                        if(willJumpOver) {
                            positions.addAll(temp);
                            willJumpOver = false;
                        }
                        temp.clear();

                        break;
                    }

                    if(indexPosition != originalPosition) {
                        if(Math.abs(target - start) > 9) {
                            willJumpOver = true; // >9 means the move is over stone
                            temp.add(target);
                        }
                    } else {
                        positions.add(target);
                    }

                    start = target;
                }
            }
        } else {
            for(int i = 0; i < 2; i++) {
                target = getDirectionTarget(indexPosition, dirVectors[i]);

                if(target != -1) {
                    if(indexPosition != originalPosition) {
                        if(Math.abs(target - indexPosition) > 9) positions.add(target);
                    } else {
                        positions.add(target);
                    }
                }
            }
        }
    }

    /**
     * Returns index of target in direction or -1
     * @param indexPosition source index
     * @param direction direction vector
     * @return index of possible move or -1 if cannot move in direction
     */
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

    /**
     *
     * @return game board
     */
    public int[] getGameBoard() {
        return gameBoard;
    }

    /**
     *
     * @return player color
     */
    public PSColor getPlayerColor() {
        return player;
    }

    /**
     * Prints gameboard as 8x8 grid. For debugging purposes
     */
    public void printGameBoard() {
        System.out.println("===current gameboard===");
        for(int i = 0; i < gameBoard.length; i++) {
            System.out.print(gameBoard[i] + "  ");
            if(i % 8 == 7) {
                System.out.println("");
            }
        }
    }

    /**
     *
     * @return players stone indexes list
     */
    public ArrayList<Integer> getPlayerStoneIndexes() {
        return playerStoneIndexes;
    }

    /**
     *
     * @return jumped over stones list
     */
    public ArrayList<Integer> getJumpedOver() {
        return jumpedOver;
    }

    /**
     *
     * @return opponent jumped over stones
     */
    public ArrayList<Integer> getOpponentJumpedOver() {
        return opponentJumpedOver;
    }

    /**
     * Check if player can move again
     * @param from index
     * @param to index
     * @return true/ false
     */
    public boolean canMoveAgain(int from, int to) {
//        int dir = getDirectionFromVector((to - from));
//
//        System.err.println("from " + from + " to " + to + " is dir " + dir);
//        System.err.println(("which is " + ((to - from) / dir) + "times"));
//
//        for(int i = 0; i < ((to - from) / dir); i++) {
////            if(gameBoard[from + (to - from) * i] % 2 == 0) return true;       //yeah... % doesn't work 0 % 2 is true
//            if(gameBoard[from + dir * i] == 2 || gameBoard[from + dir * i] == 4 ) return true;
//        }
//
//        return false;
        return (to - from) == 2 * -7 || (to - from) == 2 * -9 || (to - from == 2 * 7 || (to - from) == 2 * 9);
    }

    public int getDirectionFromVector(int vector) {
        if(vector == 0) return 0;

        if(vector < 0) {
            if(vector % 7 == 0) return -7;
            else if(vector % 9 == 0) return -9;
            else return 0; // i won't actually check for this, as it shouldn't happen theoretically
        } else {
            if(vector % 7 == 0) return 7;
            else if(vector % 9 == 0) return 9;
            else return 0; // same
        }
    }

    /**
     * Clears and reinitializes @playerStoneIndexes list
     */
    //brute froce
    public void updatePlayerStoneIndexes() {
        playerStoneIndexes.clear();
        for(int i = 0; i < gameBoard.length; i++) {
            if((gameBoard[i] % 2) == 1) {
                playerStoneIndexes.add(i);
            }
        }
    }

    /**
     * Removes element from @playerStoneIndex list
     * @param index to be removed
     */
    public void removeIndexFromStones(int index) {
//        playerStoneIndexes.remove((Integer) index); //should work?
        for(int i = 0; i < playerStoneIndexes.size(); i++) {
            if(playerStoneIndexes.get(i) == index) {
                playerStoneIndexes.remove(i);
                return;
            }
        }
    }

    public boolean validateGameboard(String param) {
        int count = 0;
        boolean rv = true;

        for(int i = 0; i < gameBoard.length; i++) {
            if(gameBoard[i] >= 0) {
                if(gameBoard[i] != Character.getNumericValue(param.charAt(count))) {
                    gameBoard[i] = Character.getNumericValue(param.charAt(count));
                    rv = false;
                }
                count++;
            }
        }

        return rv;
    }


    private int inverseStone(int stone) {
        switch (stone) {
            case 1: return 2;
            case 2: return 1;
            case 3: return 4;
            case 4: return 3;
        }

        return 0;
    }
    public void updateGameboardByServer(String param) {
        int count = 0;

        if(player == PSColor.BLACK) {
            for(int i = 0; i < gameBoard.length; i++) {
                if(gameBoard[i] >= 0) {
                    gameBoard[i] = inverseStone(Character.getNumericValue(param.charAt(param.length() - 1 - count)));
                    count++;
                }
            }
        } else {
            for(int i = 0; i < gameBoard.length; i++) {
                if(gameBoard[i] >= 0) {
                    gameBoard[i] = Character.getNumericValue(param.charAt(count));
                    count++;
                }
            }
        }
    }
}