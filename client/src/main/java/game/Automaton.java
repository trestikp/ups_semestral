package game;


/**
 * Automaton class contains current application state and transition table.
 */
public class Automaton {
    /** Application state, not only game state */
    private State gameState;

    /** Transition matrix */
    private final State[][] transitions = new State[State.values().length][Action.values().length];

    /**
     * Automaton constructor. Fills transition matrix and sets default state.
     */
    public Automaton() {
        /* START OF TRANSITION INITIALIZATION */

        //init all transitions as error
        for(int i = 0; i < State.values().length; i++) {
            for(int j = 0; j < State.values().length; j++) {
                transitions[i][j] = State.NOT_ALLOWED;
            }
        }

        //initializing possible transitions
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


    /**
     * Performs transition in transition table from gameState with action
     * @param a Action to be performed
     */
    public void makeTransition(Action a) {
        gameState = transitions[gameState.getCode()][a.getCode()];
    }

    /**
     * Validates if translation can be made not going into "NOT_ALLOWED" state
     * @param a Action to be performed
     * @return true/ false
     */
    public boolean validateTransition(Action a) {
        return transitions[gameState.getCode()][a.getCode()] != State.NOT_ALLOWED;
    }

    /**
     * Returns current gameState (application state)
     * @return State instance
     */
    public State getGameState() {
        return gameState;
    }

    /**
     * Sets state bypassing transition matrix. Intended for reconnection
     * @param state to be set
     */
    public void setGameState(State state) {
        this.gameState = state;
    }
}
