package game;

import javafx.scene.chart.StackedAreaChart;

public class Automaton {
    private State gameState;

    private final State[][] transitions = new State[State.values().length][Action.values().length];

    public Automaton() {
        /* START OF TRANSITION INITIALIZATION */

        //init all transitions as error
        for(int i = 0; i < State.values().length; i++) {
            for(int j = 0; j < State.values().length; j++) {
                transitions[i][j] = State.NOT_ALLOWED;
            }
        }

        //initializing possible transitions
//        transitions[State.INIT.getCode()]           [Action.CONNECT.getCode()]      = State.CONNECTED;
//
//        transitions[State.CONNECTED.getCode()]      [Action.START.getCode()]        = State.WORKING;
//        transitions[State.CONNECTED.getCode()]      [Action.QUEUE.getCode()]        = State.WAITING;
//        transitions[State.CONNECTED.getCode()]      [Action.RECONNECT.getCode()]    = State.PAUSED;
//
//        transitions[State.WAITING.getCode()]        [Action.START.getCode()]        = State.WORKING;
//
//        transitions[State.WORKING.getCode()]        [Action.PAUSE.getCode()]        = State.PAUSED;
//        transitions[State.WORKING.getCode()]        [Action.TURN.getCode()]         = State.WORKING;
//        transitions[State.WORKING.getCode()]        [Action.END.getCode()]          = State.END;
//
//        transitions[State.PAUSED.getCode()]         [Action.RESUME.getCode()]       = State.WORKING;
//        transitions[State.PAUSED.getCode()]         [Action.WAIT.getCode()]         = State.PAUSED;
//
//        transitions[State.CONNECTED.getCode()]      [Action.LOSS.getCode()]         = State.DISCONNECTED;
//        transitions[State.WAITING.getCode()]        [Action.LOSS.getCode()]         = State.DISCONNECTED;
//        transitions[State.WORKING.getCode()]        [Action.LOSS.getCode()]         = State.DISCONNECTED;
//        transitions[State.PAUSED.getCode()]         [Action.LOSS.getCode()]         = State.DISCONNECTED;
//
//        transitions[State.DISCONNECTED.getCode()]   [Action.CONNECT.getCode()]    = State.CONNECTED;

        transitions[State.DISCONNECTED.getCode()]       [Action.QUIT.getCode()]             = State.END;
        transitions[State.DISCONNECTED.getCode()]       [Action.CONNECT.getCode()]          = State.PICKER;

        transitions[State.PICKER.getCode()]             [Action.CANCEL.getCode()]           = State.DISCONNECTED;
        transitions[State.PICKER.getCode()]             [Action.INVALID_IN.getCode()]       = State.PICKER;
        transitions[State.PICKER.getCode()]             [Action.CONNECT.getCode()]          = State.CONNECTED;

        transitions[State.CONNECTED.getCode()]          [Action.DISCONNECT.getCode()]       = State.DISCONNECTED;
        transitions[State.CONNECTED.getCode()]          [Action.WAIT.getCode()]             = State.WAITING;
        transitions[State.CONNECTED.getCode()]          [Action.START_I.getCode()]          = State.TURN;
        transitions[State.CONNECTED.getCode()]          [Action.START_O.getCode()]          = State.OPPONENT_TURN;
        transitions[State.CONNECTED.getCode()]          [Action.CREATE.getCode()]           = State.CREATING_LOBBY;
        transitions[State.CONNECTED.getCode()]          [Action.JOIN.getCode()]             = State.CHOOSING_LOBBY;
        transitions[State.CONNECTED.getCode()]          [Action.QUIT.getCode()]             = State.END;

        transitions[State.CREATING_LOBBY.getCode()]     [Action.CANCEL.getCode()]           = State.CONNECTED;
        transitions[State.CREATING_LOBBY.getCode()]     [Action.CREATE.getCode()]           = State.WAITING;

        transitions[State.WAITING.getCode()]            [Action.CANCEL.getCode()]           = State.CONNECTED;
        transitions[State.WAITING.getCode()]            [Action.START_I.getCode()]          = State.TURN;
        transitions[State.WAITING.getCode()]            [Action.START_O.getCode()]          = State.OPPONENT_TURN;

        transitions[State.CHOOSING_LOBBY.getCode()]     [Action.CANCEL.getCode()]           = State.CONNECTED;
        transitions[State.CHOOSING_LOBBY.getCode()]     [Action.START_I.getCode()]          = State.TURN;
        transitions[State.CHOOSING_LOBBY.getCode()]     [Action.START_O.getCode()]          = State.OPPONENT_TURN;

        transitions[State.TURN.getCode()]               [Action.TURN.getCode()]             = State.OPPONENT_TURN;

        transitions[State.OPPONENT_TURN.getCode()]      [Action.TURN.getCode()]             = State.TURN;


        /* END OF TRANSITION INITIALIZATION */

        gameState = State.DISCONNECTED;
    }


    public void makeTransition(Action a) {
        gameState = transitions[gameState.getCode()][a.getCode()];
    }

    public boolean validateTransition(Action a) {
        return transitions[gameState.getCode()][a.getCode()] != State.NOT_ALLOWED;
    }

    public State getGameState() {
        return gameState;
    }

    public void setGameState(State state) {
        this.gameState = state;
    }
}
