package game;

import graphics.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import network.Instruction;
import network.TcpConnection;
import network.TcpMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Client class handles communication with server, stores game instance and stores automaton
 */
public class Client implements Runnable {
    /** This class runs on this Thread */
    private Thread t;

    /** Current controller stored in its parent */
    private OverlordCtrl currentCtrl;

    /** Application automaton */
    private Automaton auto;
    /** Game instance */
    private Game game = null;

    /** Connection instance */
    private TcpConnection connection;

    /** Instruction to be sent to server */
    private Instruction inst = null;
    private Instruction expectingFirst = null;

    /** Request parameters */
    private ArrayList<String> requestPar = new ArrayList<>();

    /** Clients ID assigned by server */
    private int clientID;

    //opponent
    /** Opponents name */
    private String opponentName;
    /** Opponents connected status */
    private boolean opponentConnected;

    //connection info
    /** Players username */
    private String username;
    /** Server hostname */
    private String host;
    /** Server port */
    private int port;

    //status info
    /** GUI status bar */
    private StatusBar status;

    private long lastCommunication;
    private long lastReconnectAttempt;
    private boolean CONNECTED = false;
    private boolean pingResponse = true;
    private boolean wasConnected = false;

    private boolean quitAfterDisconnect = false;
    private boolean showingReconnect = false;

    private int strikes = 0;

    /**
     * Constructor
     */
    public Client() {
        auto = new Automaton();

        clientID = 0;
        System.out.print("Creating client");
    }

    /**
     * Runs "infinite" cycle handling server communication. Overridden from thread
     */
    @Override
    public void run() {
        boolean waitingForReply = false;
        TcpMessage msg;

        while(auto.getGameState() != State.END) {
            try {
                Thread.sleep(5);
                //Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            if(connection != null && connection.getSoc() != null && connection.getSoc().isConnected()) {
            updateStatusElements();

//            System.out.println("inst: " + (inst == null ? "null" : inst.getName()) + "  -  " + waitingForReply);

            if(showingReconnect) continue;

            if(!waitingForReply) {
                if(inst != null) {
//                        System.out.println("Sending request: " + inst.getName());

                    boolean rv = sendRequest(inst);

                    status.setResponseText("Waiting for server response");

                    if(rv) {
                        waitingForReply = true;
                    } else {
                        status.setResponseText("Failed to send request");

                        inst = null;
                    }
                }
            }

            if(isClientConnected()) { //existing socket
                msg = connection.receiveMessage();

//                System.err.println("CONNECTED: " + CONNECTED);

                if(msg != null) {
                    if(msg.getCreationError()) {
                        status.setResponseText("Failed to create message");

                        doStrikeOrDisconnect();
                    }

                    if (msg.getInst() == Instruction.OK || msg.getInst() == Instruction.ERROR) {
                        if(!msg.validateTcpMessage(inst)) {
                            status.setResponseText("Failed to parse received message");

                            doStrikeOrDisconnect();

                            continue;
                        }

                        if(waitingForReply) {
                            handleRequest(msg);

                            waitingForReply = false;
                            inst = null;
                        } else {
                            status.setResponseText("Reply not expected");
                        }
                    } else {
                        if(!msg.validateTcpMessage(msg.getInst())) {
                            status.setResponseText("Failed to parse received message");

                            doStrikeOrDisconnect();

                            continue;
                        }

                        handleServerMessage(msg);
                    }

                    lastCommunication = System.currentTimeMillis();
                    lastReconnectAttempt = lastCommunication;
                } else {
                    if(CONNECTED) {
                        if((System.currentTimeMillis() - lastCommunication) > 5000) {
                            System.err.println("Lost communication");

                            Platform.runLater(() -> {
                                if(currentCtrl instanceof GameboardCtrl) {
                                    ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                                }
                            });

                            if(quitAfterDisconnect) { //if user wanted to disconnect but response hasn't come
                                System.exit(0);
                            }

                            connection.close();
                            connection = null;
                            CONNECTED = false;

                            opponentConnected = false;// TODO right here right?

                            auto.setGameState(State.LOST_CON);
                            status.setResponseText("Cannot contact server. Reconnecting");

                            pingResponse = true;
                        }
                    } else {
                        if(wasConnected && (System.currentTimeMillis() - lastReconnectAttempt) > 5000) {
                            System.err.println("You have socket, but aren't CONNECTED");

                            //reconnection attempt might have established connection but failed to CONNECT
                            if(connection != null) {
                                connection.close();
                                connection = null;
                            }
                        } else {
//                            System.out.println("I'm getting there");
                        }
                    }
                }

                if ((System.currentTimeMillis() - lastCommunication) > 1000 && CONNECTED &&
                        pingResponse && inst == null) {
                    sendPing();
                }
            } else {
                //user was connected but hasn't attempted reconnect for more then 5s
                if((System.currentTimeMillis() - lastReconnectAttempt) > 5000 && wasConnected) {
//                    reset move sequence if turn wasn't confirmed
                    if(inst == Instruction.TURN && waitingForReply) {
                        requestPar.clear();
                        inst = null;
                        waitingForReply = false;

                        Platform.runLater(() -> {
                            ((GameboardCtrl) currentCtrl).resetMoveSequence();
                            ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
                        });
                    }

                    System.err.println(((System.currentTimeMillis() - lastCommunication) / 1000) + "s since last communication");

                    //if user is disconnected for more then 60s show reconnect
                    if((System.currentTimeMillis() - lastCommunication) > 30000 && !showingReconnect) {

                        //if connection isn't restored within 5 minutes, stop reconnecting (server removed player)
                        if((System.currentTimeMillis() - lastCommunication) >= 300000) {
                            System.out.println("Failed to reestablish connection.");
                            resetConnectionInfo();

                            Platform.runLater(() -> {
                                currentCtrl.genericSetScene("main_menu_disconnected.fxml");
                                status.setResponseText("You can't reconnect after 5 minutes");
                            });

                            continue;
                        }

                        showingReconnect = true;
                        waitingForReply = false;

                        Platform.runLater(() -> {
                            currentCtrl.genericSetScene("reconnection_repeat.fxml");
                            ((ReconnectionRepeatCtrl) currentCtrl).messageLbl.setText("Failed to reconnect. Do you " +
                                    "wish to try again?");
                        });
                    } else {
                        System.err.println("Setting inst to CONNECT");

                        waitingForReply = false;
                        setInstruction(Instruction.CONNECT);
                    }
                } else {
                    if(!CONNECTED) {
                        inst = null;
                        waitingForReply = false;
                    }
                }

//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    /**
     * Start method for Thread
     */
    public void start() {
        if(t == null) {
            t = new Thread(this, "backend");
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     * Creates connection for application to start communication
     */
    public boolean establishConnection() {
        if(connection != null && CONNECTED) {
            System.err.println("Connection already exists");
            status.setResponseText("Connection already exists");

            return false;
        }

        System.err.println("---Socket---");
        connection = new TcpConnection(host, port);
        System.err.println("----End-----");

        if(connection.getSoc() == null) {
            System.err.println("Failed to create socket");
            status.setResponseText("Failed to create socket");
            connection = null;

            return false;
        }

        if(!connection.getSoc().isConnected()) {
            System.err.println("Created socket but can't connect");
            status.setResponseText("Created socket but can't connect");

            connection.close();
            connection = null;

            return false;
        }

        //must be set to reasonable time when connection starts
        lastCommunication = System.currentTimeMillis();

        return true;
    }

    private void closeConnection() {
        clientID = 0;
        username = null;

        if(isClientConnected()) {
            connection.close();
        }

        connection = null;

        host = null;
        port = -1;

        deleteConnectionFile();
    }

    /**
     * Calls appropriate send request
     * @param inst request of this parameter
     * @return false if client fails to send request
     */
    private boolean sendRequest(Instruction inst) {
        boolean rv = true;

        if(inst != Instruction.CONNECT && !CONNECTED) {
            return false;
        }

        switch (inst) {
            case CONNECT: sendConnect(); break;
            case DISCONNECT: sendDisconnect(); break;
            case CREATE_LOBBY: rv = sendCreateLobby(); break;
            case DELETE_LOBBY: sendDeleteLobby(); break;
            case JOIN_GAME: rv = sendJoinGame(); break;
            case TURN: rv = sendTurn(); break;
            case LOBBY: sendLobby(); break;
            default: rv = false;
        }

        return rv;
    }

    /**
     * Calls appropriate handle request
     * @param reply server reply
     */
    private void handleRequest(TcpMessage reply) {
        if(!CONNECTED && reply.getInst() != Instruction.CONNECT) {
            System.out.println("Ahh unexpected server response on CONNECT");
        }

        switch (inst) {
            case CONNECT: handleConnect(reply); break;
            case DISCONNECT: handleDisconnect(reply); break;
            case CREATE_LOBBY: handleCreateLobby(reply); break;
            case DELETE_LOBBY: handleDeleteLobby(reply); break;
            case JOIN_GAME: handleJoinGame(reply); break;
            case TURN: handleTurn(reply); break;
            case LOBBY: handleLobby(reply); break;
        }

        if(reply.getResponseText() != null) {
            status.setResponseText(reply.getResponseText());
        }
    }

    /**
     * Handles server message that isn't response to a request
     * @param msg server's message
     */
    private void handleServerMessage(TcpMessage msg) {
        if(msg.getInst() == Instruction.OPPONENT_JOIN) {                                                //OPPONENT_JOIN
            if(auto.validateTransition(Action.START_I)) {
                auto.makeTransition(Action.START_I);

                game = new Game(PSColor.WHITE);

//                sendReply(true);

                initGUIBoard(msg);
            }
        } else if(msg.getInst() == Instruction.OPPONENT_TURN) {                                         //OPPONENT_TURN
            if (msg.getResponseCode() == 201) {
                if (auto.validateTransition(Action.TURN)) {
                    for (int i = 1; i < msg.getParams().length; i++) {
                        try {
                            int from = Integer.parseInt(msg.getParams()[i - 1]);
                            int to = Integer.parseInt(msg.getParams()[i]);

                            game.moveOpponentFromTo(from, to);
                        } catch (NumberFormatException e) {
                            System.err.println("Failed to convert int to string");
                        }
                    }

                    if (!game.getOpponentJumpedOver().isEmpty()) {
                        for (int i : game.getOpponentJumpedOver()) {
                            game.getGameBoard()[i] = 0;
                            game.removeIndexFromStones(i);
                        }
                    }

                    Platform.runLater(() -> {
                        if (!game.getOpponentJumpedOver().isEmpty()) {
                            ((GameboardCtrl) currentCtrl).removeStones(game.getOpponentJumpedOver());
                        }

                        ((GameboardCtrl) currentCtrl).moveOpponentStones(msg.getParams());
                        ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
                    });

                    game.printGameBoard();
                    auto.makeTransition(Action.TURN);
//                    sendReply(true);
                } else {
                    status.setResponseText("Automaton is in wrong state");
                }
            } else if(msg.getResponseCode() == 203 || msg.getResponseCode() == 204) {
                if (getAutomaton().validateTransition(Action.END)) {
                    Platform.runLater(() -> {
                        currentCtrl.genericSetScene("main_menu_connected.fxml");
                        status.setResponseText(msg.getResponseText());
                    });

                    auto.makeTransition(Action.END);
                } else {
                    status.setResponseText("Automaton is in wrong state");
                }
            } else {
                status.setResponseText("Received unknown code for OPPONENT_TURN");
            }
        } else if(msg.getInst() == Instruction.PING) {                                                          //PING
            pingResponse = true;
        } else if(msg.getInst() == Instruction.OPPONENT_DISC) {                                         //OPPONENT_DISC
            opponentConnected = false;

            Platform.runLater(() -> {
                if(currentCtrl instanceof GameboardCtrl) {
                    ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                }
            });

//            auto.setGameState(State.LOST_CON);
            status.setResponseText("Opponent lost connection. Waiting..");
        } else if(msg.getInst() == Instruction.OPPONENT_RECO) {
            opponentConnected = true;

            Platform.runLater(() -> {
                if(currentCtrl instanceof GameboardCtrl) {
                    ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                }
//                if(currentCtrl instanceof GameboardCtrl && auto.getGameState() == State.TURN) {
//                    ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
//                }
//
//                if(currentCtrl instanceof GameboardCtrl && auto.getGameState() == State.OPPONENT_TURN) {
//                    ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
//                }

                status.setResponseText("Opponent reconnected");
            });
        }
    }

    /**
     * Send reply to servers message
     * @param success whether message was handled successfully
     */
    private void sendReply(boolean success) {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        if(success) {
            sb.append("OK");
        } else {
            sb.append("ERROR");
        }
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Send request methods                                                                                         |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    /**
     * Sends request to "connect" - create player on server
     */
    private void sendConnect() {
        StringBuilder sb = new StringBuilder();

        if(clientID > 0) lastReconnectAttempt = System.currentTimeMillis();

        if(!establishConnection()) {
//            status.setResponseText("Failed to establish connection");
            System.out.println("Failed to establish connection");
            return;
        }

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.CONNECT.getName());
        sb.append("|");
        sb.append(username);
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    /**
     * Sends request for disconnect
     */
    private void sendDisconnect() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.DISCONNECT.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    /**
     * Sends instruction to create lobby
     * @return request send status
     */
    private boolean sendCreateLobby() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.CREATE_LOBBY.getName());
        sb.append("|");

        if(currentCtrl instanceof LobbyCreationCtrl) {
            sb.append(((LobbyCreationCtrl) currentCtrl).getLobbyName());
        } else {
            status.setResponseText("Somehow wrong controller");
            return false;
        }

        sb.append('\n');

        connection.sendMessageTxt(sb.toString());

        return true;
    }

    /**
     * Send delete lobby request
     */
    private void sendDeleteLobby() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.DELETE_LOBBY.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    /**
     * Send request for lobby list
     */
    private void sendLobby() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.LOBBY.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    /**
     * Send request to join game from lobby list
     * @return request send status
     */
    private boolean sendJoinGame() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.JOIN_GAME.getName());

        if(requestPar.isEmpty()) {
            status.setResponseText("Failed to fetch room name");
            return false;
        }

        if(requestPar.size() > 1) {
            status.setResponseText("Got too many parameters");
            return false;
        }

        sb.append("|");
        sb.append(requestPar.get(0));
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());

        requestPar.clear();

        return true;
    }

    /**
     * Send turn request
     * @return send status
     */
    private boolean sendTurn() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.TURN.getName());

        if(requestPar.isEmpty()) {
            status.setResponseText("No moves to send");
            return false;
        }

        if(requestPar.size() > 30) {
            status.setResponseText("Too many moves to send");
            return false;
        }

        for(String par : requestPar) {
            sb.append("|");
            sb.append(par);
        }

        sb.append('\n');

        connection.sendMessageTxt(sb.toString());

        return true;
    }

    private void sendPing() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.PING.getName());
        sb.append("\n");

        connection.sendMessageTxt(sb.toString());

        pingResponse = false;
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Handle request methods                                                                                       |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    /**
     * Handle connect request confirmation
     * @param reply server message
     */
    public void handleConnect(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(reply.getResponseCode() == 201) {
                clientID = reply.getPlayer_id();

                saveConnectionToFile();

                if(auto.validateTransition(Action.CONNECT)) {
                    auto.makeTransition(Action.CONNECT);

                    CONNECTED = true;
                    wasConnected = true;

                    Platform.runLater(() -> {
                        currentCtrl.genericSetScene("main_menu_connected.fxml");
                        status.setResponseText(reply.getResponseText());
                    });
                } else {
                    status.setResponseText("Automaton validation failed");
                }
            } else if(reply.getResponseCode() == 202) {
                CONNECTED = true;
                quitAfterDisconnect = false;
                wasConnected = true;

                if(reply.getParams()[0].equals(State.CONNECTED.getName())) {
                    auto.setGameState(State.CONNECTED);

                    Platform.runLater(() -> {
                        currentCtrl.genericSetScene("main_menu_connected.fxml");
                        status.setResponseText(reply.getResponseText());
                    });
                } else if(reply.getParams()[0].equals(State.TURN.getName()) ||
                        reply.getParams()[0].equals(State.OPPONENT_TURN.getName())) {
                    if(reply.getParams()[0].equals(State.TURN.getName())) auto.setGameState(State.TURN);
                    else auto.setGameState(State.OPPONENT_TURN);

                    if(game == null) {
                        if(reply.getParams()[2].length() > 1) System.err.println("Expected 1 or 0, entered string is longer");
                        char onTop = reply.getParams()[2].charAt(0);
                        if(Character.getNumericValue(onTop) == 1) {
                            game = new Game(PSColor.BLACK);
                        } else if(Character.getNumericValue(onTop) == 0) {
                            game = new Game(PSColor.WHITE);
                        } else {
                            System.err.println("onTop must be 1 or 0!! Failed reconnect");
                            return;
                        }
                    }

                    opponentName = reply.getParams()[3];
                    opponentConnected = true;

                    game.updateGameboardByServer(reply.getParams()[1]);
                    game.updatePlayerStoneIndexes();

                    Platform.runLater(() -> {
                        if(!(currentCtrl instanceof GameboardCtrl)) {
                            currentCtrl.genericSetScene("gameboard_v2.fxml");

                            status.opponentNameLabel.setText("Opponent: " + opponentName);
                            setOpponentVisible();
                        }

                        ((GameboardCtrl) currentCtrl).initStones();
                        ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
//                        if(auto.getGameState() == State.OPPONENT_TURN) {
//                            ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
//                        }
                        game.printGameBoard();
                    });

//                    if(reply.getParams()[0].equals(State.TURN.getName())) {
//                    auto.setGameState(State.TURN);
//
//                    boolean rv = game.validateGameboard(reply.getParams()[1]);
//
//                    Platform.runLater(() -> {
//                        if(rv) {
//                            game.updatePlayerStoneIndexes();
//                            ((GameboardCtrl) currentCtrl).initStones();
//                            ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
//                            status.setResponseText("Gameboard didn't match. Using server gameboard");
//                            game.printGameBoard(); // TODO blah
//                        }
//
//                        ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
//                    });
//                } else if(reply.getParams()[0].equals(State.OPPONENT_TURN.getName())) {
//                    auto.setGameState(State.OPPONENT_TURN);
//
//                    boolean rv = game.validateGameboard(reply.getParams()[1]);
//
//                    Platform.runLater(() -> {
//                        if(rv) {
//                            game.updatePlayerStoneIndexes();
//                            ((GameboardCtrl) currentCtrl).initStones();
//                            ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
//                            status.setResponseText("Gameboard didn't match. Using server gameboard");
//                            game.printGameBoard(); //TODO blah
//                        }
//                    });
                } else {
                    doStrikeOrDisconnect(); //TODO do this or ignore?

                    System.err.println("Failed to get state from server on reconnection. Returning to menu");

                    status.setResponseText("Failed to get state. Returning to menu");
                    auto.setGameState(State.CONNECTED);
                }
            } else {
                status.setResponseText("Unknown positive code");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());

            if(quitAfterDisconnect) {
                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("main_menu_disconnected.fxml");
                    status.setResponseText("Failed to reconnect");
                });

                resetConnectionInfo();
                deleteConnectionFile();

                quitAfterDisconnect = false;
            }

            //if CONNECT fails, close connection (no strikes)
            if(connection != null) {
                connection.close();
                connection = null;
                CONNECTED = false;
                opponentConnected = false;
            }
        } else {
            if(connection != null) {
                connection.close();
                connection = null;
                CONNECTED = false;
                opponentConnected = false;
            }

            status.setResponseText("Unknown response, closing connection");
        }
    }

    /**
     * Handle disconnect request confirmation
     * @param reply server message
     */
    public void handleDisconnect(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.DISCONNECT)) {
                auto.makeTransition(Action.DISCONNECT);

                CONNECTED = false;
                closeConnection();

                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("main_menu_disconnected.fxml");
                    status.setResponseText(reply.getResponseText());
                });
            } else {
                status.setResponseText("Automaton validation failed");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());
        } else {
            status.setResponseText("Unknown response");
        }

        if(quitAfterDisconnect) {
            System.exit(0);
        }
    }

    /**
     * Handle create lobby request confirmation
     * @param reply server message
     */
    private void handleCreateLobby(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.CREATE)) {
                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("waiting.fxml");
                    status.setResponseText(reply.getResponseText());
                });

                auto.makeTransition(Action.CREATE);
            } else {
                status.setResponseText("Automaton validation failed");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());
        } else {
            status.setResponseText("Unknown response");
        }
    }

    /**
     * Handle lobby deletion request confirmation
     * @param reply server message
     */
    private void handleDeleteLobby(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.CANCEL)) {
                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("main_menu_connected.fxml");
                    status.setResponseText(reply.getResponseText());
                });

                auto.makeTransition(Action.CANCEL);
            } else {
                status.setResponseText("Automaton validation failed");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());
        } else {
            status.setResponseText("Unknown response");
        }
    }

    /**
     * Handle lobby list request confirmation
     * @param reply server message
     */
    private void handleLobby(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.JOIN)) {
                ArrayList<HBox> lobbies = new ArrayList<>();

                if(reply.getParams().length > 0) {
                    for(String par : reply.getParams()) {

                        lobbies.add(generateLobbyItem(par));
                    }
                }

                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("lobby_list.fxml");
                    ((LobbyListCtrl) currentCtrl).setLobbyContent(lobbies);

                    status.setResponseText(reply.getResponseText());
                });

                auto.makeTransition(Action.JOIN);
            } else {
                status.setResponseText("Automaton validation failed");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());
        } else {
            status.setResponseText("Unknown response");
        }
    }

    /**
     * Handle join game request confirmation
     * @param reply server message
     */
    private void handleJoinGame(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(reply.getParams().length == 1) {
                if (auto.validateTransition(Action.START_O)) {
                    auto.makeTransition(Action.START_O);

                    game = new Game(PSColor.BLACK);
                    opponentName = reply.getParams()[0];
                    opponentConnected = true;

                    initGUIBoard(reply);
                    Platform.runLater(() -> {
                        ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                    });
                } else {
                    status.setResponseText("Automaton validation failed");
                }
            } else {
                status.setResponseText("Unexpected parameter count");
            }
        } else if(reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());
        } else {
            status.setResponseText("Unknown response");
        }
    }

    /**
     * Handle turn request confirmation
     * @param reply server message
     */
    private void handleTurn(TcpMessage reply) {
        if (reply.getInst() == Instruction.OK) {
            if (reply.getResponseCode() == 202) {
                if (auto.validateTransition(Action.TURN)) {
                    try {
                        for (int i = 1; i < requestPar.size(); i++) {
                            int from = Integer.parseInt(requestPar.get(i - 1));
                            int to = Integer.parseInt(requestPar.get(i));

                            System.out.println("Moving form " + requestPar.get(i - 1) + " to " + requestPar.get(i));
                            game.moveFromTo(from, (to - from));
//                            game.moveFromTo_v2(from, to, false);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to convert number");
                    }

                    if (!game.getJumpedOver().isEmpty()) {
                        for (int i : game.getJumpedOver()) {
                            game.getGameBoard()[i] = 0;
                        }
                    }

                    game.updatePlayerStoneIndexes();

                    Platform.runLater(() -> {
                        if (!game.getJumpedOver().isEmpty()) {
                            ((GameboardCtrl) currentCtrl).removeStones(game.getJumpedOver());
                        }

                        ((GameboardCtrl) currentCtrl).moveStone();
                        ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                    });

                    game.printGameBoard();

                    auto.makeTransition(Action.TURN);
                } else {
                    status.setResponseText("Automaton validation failed");
                }
            } else if(reply.getResponseCode() == 203 || reply.getResponseCode() == 204) {
                if(getAutomaton().validateTransition(Action.END)) {
                    Platform.runLater(() -> {
                        currentCtrl.genericSetScene("main_menu_connected.fxml");
                        status.setResponseText(reply.getResponseText());
                    });

                    auto.makeTransition(Action.END);
                } else {
                    status.setResponseText("Automaton is in wrong state");
                }
            } else {
                status.setResponseText("Unknown response code");
            }
//            else if(reply.getResponseCode() == 203) {
//                if(getAutomaton().validateTransition(Action.WIN)) {
//                    Platform.runLater(() -> {
//                        currentCtrl.genericSetScene("main_menu_connected.fxml");
//                        status.setResponseText(reply.getResponseText());
//                    });
//
//                    auto.makeTransition(Action.WIN);
//                } else {
//                    status.setResponseText("Automaton is in wrong state");
//                }
//            } else if(reply.getResponseCode() == 204) {
//                //there is currently no real difference between "win" and "lose" transitions, but could server for
//                //stat logging
//                if(getAutomaton().validateTransition(Action.LOSE)) {
//                    auto.makeTransition(Action.LOSE);
//
//                    Platform.runLater(() -> {
//                        currentCtrl.genericSetScene("main_menu_connected.fxml");
//                        status.setResponseText(reply.getResponseText());
//                    });
//                } else {
//                    status.setResponseText("Automaton is in wrong state");
//                }
//            }
        } else if (reply.getInst() == Instruction.ERROR) {
            status.setResponseText(reply.getResponseText());

            Platform.runLater(() -> {
                ((GameboardCtrl) currentCtrl).resetMoveSequence();
                ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
            });
        } else {
            status.setResponseText("Unknown response");
        }


        requestPar.clear();
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Attribute methods                                                                                            |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    /**
     * Sets opponent status elements to visible
     */
    private void setOpponentVisible() {
        status.opponentConnectCircle.setVisible(true);
        status.opponentConnectionLabel.setVisible(true);
        status.opponentNameLabel.setVisible(true);
    }

    /**
     *
     * @return if client is connected to server
     */
    public boolean isClientConnected() {
        return connection != null && connection.getSoc() != null && connection.getSoc().isConnected();
    }

    /**
     *
     * @return Game instance
     */
    public Game getGame() {
        return game;
    }

    /**
     *
     * @return server connection
     */
    public TcpConnection getConnection() {
        return connection;
    }

    /**
     * Sets player username
     * @param username players username
     */
    public void setUsername(String username) {
        this.username = username;

        if(username == null) {
            status.clientNameLabel.setVisible(false);
        } else {
            status.clientNameLabel.setText("You: " + username);
            status.clientNameLabel.setVisible(true);
        }
    }

    /**
     * Sets instruction to be requested and handled
     * @param inst instruction
     */
    public void setInstruction(Instruction inst) {
        this.inst = inst;
    }

    /**
     * Set hostname
     * @param host name
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Sets server port
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets status bar instance
     * @param status status bar
     */
    public void setStatusBar(StatusBar status) {
        this.status = status;
    }

    /**
     *
     * @return automaton instance
     */
    public Automaton getAutomaton() {
        return auto;
    }

    /**
     * Adds parameter to request parameters
     * @param par String to be appended
     */
    public void addRequestPar(String par) {
        requestPar.add(par);
    }

    /**
     * Updates status elements
     */
    public void updateStatusElements() {
        Platform.runLater(() -> {
//            if(connection == null || connection.getSoc() == null || !connection.getSoc().isConnected()) {
            if(!CONNECTED) {
                status.clientConnectCircle.setStyle("-fx-fill: #FF0000");
                status.clientConnectionLabel.setText("Disconnected");
            } else {
                status.clientConnectCircle.setStyle("-fx-fill: #00FF00");
                status.clientConnectionLabel.setText("Connected");
            }

//            status.clientNameLabel.setVisible(username != null);
            setUsername(username);

            if(auto != null && auto.getGameState() != null) {
                status.clientStateLabel.setText("State: " + auto.getGameState().getName());
            } else {
                status.clientStateLabel.setText("State: ERROR");
            }

            status.responseLabel.setVisible(status.responseLabel.getText() != null);

            if(game != null) {
                if(opponentConnected) {
                    status.opponentConnectionLabel.setText("Connected");
                    status.opponentConnectCircle.setStyle("-fx-fill: #00FF00");
                } else {
                    status.opponentConnectionLabel.setText("Disconnected");
                    status.opponentConnectCircle.setStyle("-fx-fill: #FF0000");
                }
            }
        });
    }

    /**
     * Set current controller
     * @param currentCtrl OverlordCtrl extender
     */
    public void setCurrentCtrl(OverlordCtrl currentCtrl) {
        this.currentCtrl = currentCtrl;
    }


    public StatusBar getStatus() {
        return status;
    }

    public void setShowingReconnect(boolean showingReconnect) {
        this.showingReconnect = showingReconnect;
    }

    public boolean getShowingReconnect() {
        return showingReconnect;
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Support methods                                                                                              |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    /**
     * Generates lobby item that is displayed in lobby list
     * @param par lobby name
     * @return HBox containing lobby list item
     */
    public HBox generateLobbyItem(String par) {
        HBox container = new HBox();
        container.setMinHeight(70.0);
//                        container.setPrefHeight(50.0);
        container.setAlignment(Pos.CENTER);

        Label roomName = new Label(par);
        HBox.setMargin(roomName, new Insets(0, 10, 0, 0));
        roomName.setStyle("-fx-font-size: 18px");

        Button joinRoom = new Button("Join");
        HBox.setMargin(joinRoom, new Insets(0, 0, 0, 10));
        joinRoom.setStyle("-fx-font-size: 18px");
        joinRoom.setAlignment(Pos.CENTER);
        joinRoom.setMinHeight(30.0);
        joinRoom.setPrefHeight(50.0);
        joinRoom.setPrefWidth(100.0);

        joinRoom.setOnAction((event) -> {
            requestPar.clear();
            requestPar.add(roomName.getText());

            this.setInstruction(Instruction.JOIN_GAME);
            System.out.println("roomName: " + roomName.getText());
            try {
                System.out.println("requestPar: " + requestPar.get(0));
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Don't click join so fast!");
            }
        });

        container.getChildren().addAll(roomName, joinRoom);

        return container;
    }

    /**
     * Inits GUI gameboard
     * @param reply server reply
     */
    private void initGUIBoard(TcpMessage reply) {
        Platform.runLater(() -> {
            currentCtrl.genericSetScene("gameboard_v2.fxml");
            ((GameboardCtrl) currentCtrl).initStones();
            status.setResponseText(reply.getResponseText());
        });

        opponentName = reply.getParams()[0];
        opponentConnected = true;

        Platform.runLater(() -> {
            status.opponentNameLabel.setText("Opponent: " + opponentName);
            setOpponentVisible();
        });
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         What a mess                                                                                                  |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/
    private void saveConnectionToFile() {
        File f = new File("./.last_connection");

//        if(f.exists()) {
//            status.setResponseText("Last connection exists");
//            return;
//        }

        try {
            f.delete();
            boolean rv = f.createNewFile();

            if(!rv) {
                status.setResponseText("Failed to create connection file");
            }
        } catch (IOException e) {
            status.setResponseText("Failed to create file with connection");
        }

        try {
            PrintWriter pw = new PrintWriter(f);

            pw.write(host + "|" + port + "|" + username + "|" + clientID + "\n");
            pw.flush();

            pw.close();
        } catch (FileNotFoundException e) {
            status.setResponseText("Failed to find connection file");
        }
    }

    public boolean readConnectionFromFile() {
        File f = new File("./.last_connection");
        String line = null;

        if(!f.exists()) return false;

        try {
            Scanner sc = new Scanner(f);

            if(sc.hasNextLine()) line = sc.nextLine();

            if(line != null) {
                String[] parts = line.split("\\|");

                if(parts.length != 4) return false;

                host = parts[0];

                try {
                    port = Integer.parseInt(parts[1]);

                    if(port < 0 || port > 65536) return false;
                } catch (NumberFormatException e) {
                    return false;
                }

                username = parts[2];

                try {
                    clientID = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private void deleteConnectionFile() {
        File f = new File("./.last_connection");

        if(f.exists()) {
            boolean rv = f.delete();

            if(!rv) {
                status.setResponseText("Failed to delete connection file");
            }
        }
    }

    private void doStrikeOrDisconnect() {
        if(CONNECTED && strikes < 3) {
            System.err.println("Strike " + strikes);
            strikes++;
        } else { //if user isn't CONNECTED, disconnect immediately without strikes
            connection.close();
            connection = null;
            CONNECTED = false;

            System.err.println("Forcefully closing connection");
            status.setResponseText("Suspicious connection. Closing");

            deleteConnectionFile();
        }
    }

    public void setQuitAfterDisconnect(boolean quitAfterDisconnect) {
        this.quitAfterDisconnect = quitAfterDisconnect;
    }

    public void resetConnectionInfo() {
        host = null;
        port = -1;
        clientID = 0;
        username = null;

        connection = null;
        auto.setGameState(State.DISCONNECTED);

        lastReconnectAttempt = -1;
        lastCommunication = -1;
        wasConnected = false;
        showingReconnect = false;
    }
}