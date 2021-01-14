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

import java.util.ArrayList;

public class Client implements Runnable {
    private Thread t;

    private OverlordCtrl currentCtrl;

    private Automaton auto;
    private Game game = null;

    private TcpConnection connection;

    private Instruction inst = null;

    private ArrayList<String> requestPar = new ArrayList<>();

    private int clientID;

    //opponent
    private String opponentName;
    private boolean opponentConnected;

    //connection info
    private String username;
    private String host;
    private int port;

    //status info
    private StatusBar status;
//    private StatusBarCtrl status;


    public Client() {
        auto = new Automaton();

        clientID = 0;
        System.out.print("Creating client");
    }

    @Override
    public void run() {
        boolean waitingForReply = false;
        TcpMessage msg;

        while(auto.getGameState() != State.END) {
//            if(connection != null && connection.getSoc() != null && connection.getSoc().isConnected()) {
            if(isClientConnected()) {
                msg = connection.recieveMessage();

                updateStatusElements();

                if(msg != null) {
                    if (msg.getInst() == Instruction.OK || msg.getInst() == Instruction.ERROR ||
                            msg.getInst() == Instruction.LOBBY) {
                        if(waitingForReply) {
                            handleRequest(msg);

                            waitingForReply = false;
                            inst = null;
                        } else {
                            //TODO wasn't expecting reply
                            status.setResponseText("Reply not expected");
                        }
                    } else {
                        handleServerMessage(msg);
                    }
                }

                if(!waitingForReply) {
                    if(inst != null) {
                        boolean rv = sendRequest(inst);

                        status.setResponseText("Waiting for server response");

                        if(rv) {
                            waitingForReply = true;
                        } else {
                            //TODO failed to send request
                            handleError(inst);

                            inst = null;
                        }
                    }
                }
            } else {
//                System.err.println("No connection. Waiting for new one");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if(t == null) {
            t = new Thread(this, "backend");
            t.start();
        }
    }

    public void establishConnection() {
        if(connection != null) {
            System.err.println("Connection already exists");
            return;
        }

        connection = new TcpConnection(host, port);

        if(connection.getSoc() == null) {
            System.err.println("Failed to create socket");
            connection = null;
        }
    }

    private boolean sendRequest(Instruction inst) {
        boolean rv = true;

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

    private void handleRequest(TcpMessage reply) {
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

    private void handleServerMessage(TcpMessage msg) {
        if(msg.getInst() == Instruction.OPPONENT_JOIN) {
            if(auto.validateTransition(Action.START_I)) {
                auto.makeTransition(Action.START_I);

                game = new Game(PSColor.WHITE);

                sendReply(true);

                initGUIBoard(msg);
            }
        } else if(msg.getInst() == Instruction.OPPONENT_TURN) {
            if(auto.validateTransition(Action.TURN)) {
                for(int i = 1; i < msg.getParams().length; i++) {
                    try {
                        int from = Integer.parseInt(msg.getParams()[i - 1]);
                        int to = Integer.parseInt(msg.getParams()[i]);

                        game.moveOpponentFromTo(from, to);
                    } catch (NumberFormatException e) {
                        System.err.println("Failed to convert int to string");
                    }
                }

                if(!game.getOpponentJumpedOver().isEmpty()) {
                    for(int i : game.getOpponentJumpedOver()) {
                        game.getGameBoard()[i] = 0;
                        game.removeIndexFromStones(i);
                    }
                }

                Platform.runLater(() -> {
                    if(!game.getOpponentJumpedOver().isEmpty()) {
                        ((GameboardCtrl) currentCtrl).removeStones(game.getOpponentJumpedOver());
                    }

                    ((GameboardCtrl) currentCtrl).moveOpponentStones(msg.getParams());
                    ((GameboardCtrl) currentCtrl).setImageViewEvents(game.getPlayerStoneIndexes());
                });

                game.printGameBoard();
                auto.makeTransition(Action.TURN);
                sendReply(true);
            } else {
                status.setResponseText("Automaton is in wrong state");
            }
        } else {
            System.err.println("Server shouldn't say anything else by it self");
        }
    }

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

    private void handleError(Instruction inst) {
        switch (inst) {
            case CREATE_LOBBY: errorHandleCreateLobby();
        }
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Send request methods                                                                                         |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    private void sendConnect() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.CONNECT.getName());
        sb.append("|");
        sb.append(username);
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    private void sendDisconnect() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.DISCONNECT.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

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
        }

        sb.append('\n');

        connection.sendMessageTxt(sb.toString());

        return true;
    }

    private void sendDeleteLobby() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.DELETE_LOBBY.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

    private void sendLobby() {
        StringBuilder sb = new StringBuilder();

        sb.append(clientID);
        sb.append("|");
        sb.append(Instruction.LOBBY.getName());
        sb.append('\n');

        connection.sendMessageTxt(sb.toString());
    }

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

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Handle request methods                                                                                       |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    public void handleConnect(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            clientID = reply.getPlayer_id();

            //TODO reconnect
            if(auto.validateTransition(Action.CONNECT)) {
                auto.makeTransition(Action.CONNECT);

                Platform.runLater(() -> {
                    currentCtrl.genericSetScene("main_menu_connected.fxml");
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
    }

    public void handleDisconnect(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.DISCONNECT)) {
                auto.makeTransition(Action.DISCONNECT);

                connection = null;
                clientID = 0;

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
    }

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

    private void handleJoinGame(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(reply.getParams().length == 1) {
                if (auto.validateTransition(Action.START_O)) {
                    auto.makeTransition(Action.START_O);

                    game = new Game(PSColor.BLACK);
                    opponentName = reply.getParams()[0];
                    opponentConnected = true;

                    initGUIBoard(reply);
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

    private void handleTurn(TcpMessage reply) {
        if(reply.getInst() == Instruction.OK) {
            if(auto.validateTransition(Action.TURN)) {
                try {
                    for (int i = 1; i < requestPar.size(); i++) {
                        int from = Integer.parseInt(requestPar.get(i - 1));
                        int to = Integer.parseInt(requestPar.get(i));

                        System.out.println("Moving form " + requestPar.get(i - 1) + " to " + requestPar.get(i));
                        game.moveFromTo(from, (to - from));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Failed to convert number");
                }

                if(!game.getJumpedOver().isEmpty()) {
                    for(int i : game.getJumpedOver()) {
                        game.getGameBoard()[i] = 0;
                    }
                }

                game.updatePlayerStoneIndexes();

                Platform.runLater(() -> {
                    if(!game.getJumpedOver().isEmpty()) {
                        ((GameboardCtrl) currentCtrl).removeStones(game.getJumpedOver());
                    }

                    ((GameboardCtrl) currentCtrl).moveStone();
                    ((GameboardCtrl) currentCtrl).unsetImageViewEvents(game.getPlayerStoneIndexes());
                });

                game.printGameBoard();

                auto.makeTransition(Action.TURN);
            }
        } else if(reply.getInst() == Instruction.ERROR) {
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
|         Error handle methods                                                                                         |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    private void errorHandleCreateLobby() {
        status.setResponseText("Failed to create lobby");
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Attribute methods                                                                                            |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

    private void setOpponentVisible() {
        status.opponentConnectCircle.setVisible(true);
        status.opponentConnectionLabel.setVisible(true);
        status.opponentNameLabel.setVisible(true);
    }

    public boolean isClientConnected() {
        return connection != null && connection.getSoc() != null && connection.getSoc().isConnected();
    }

    public Game getGame() {
        return game;
    }

    public TcpConnection getConnection() {
        return connection;
    }

    public void setUsername(String username) {
        this.username = username;

        status.clientNameLabel.setText("You: " + username);
        status.clientNameLabel.setVisible(true);
    }

    public void setInstruction(Instruction inst) {
        this.inst = inst;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setStatusBar(StatusBar status) {
        this.status = status;
    }
//    public void setStatusBarCtrl(StatusBarCtrl status) {
//        this.status = status;
//    }

    public Automaton getAutomaton() {
        return auto;
    }

    public void addRequestPar(String par) {
        requestPar.add(par);
    }

    public void updateStatusElements() {
        Platform.runLater(() -> {
//            if(connection == null || connection.getSoc() == null || !connection.getSoc().isConnected()) {
            if(auto.getGameState() == State.DISCONNECTED || auto.getGameState() == State.PICKER) {
                status.clientConnectCircle.setStyle("-fx-fill: #FF0000");
                status.clientConnectionLabel.setText("Disconnected");
            } else {
                status.clientConnectCircle.setStyle("-fx-fill: #00FF00");
                status.clientConnectionLabel.setText("Connected");
            }

            status.clientNameLabel.setVisible(username != null);

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

    public void setCurrentCtrl(OverlordCtrl currentCtrl) {
        this.currentCtrl = currentCtrl;
    }

/*---------------------------------------------------------------------------------------------------------------------|
|                                                                                                                      |
|         Support methods                                                                                              |
|                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------*/

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
            System.out.println("requestPar: " + requestPar.get(0));
        });

        container.getChildren().addAll(roomName, joinRoom);

        return container;
    }

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
}
