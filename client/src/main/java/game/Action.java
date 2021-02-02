package game;

/**
 * Class names Actions used for Automaton state transitions
 */
public enum Action {
    QUIT        ("quit"             , 0),
    CANCEL      ("cancel"           , 1),
    CONNECT     ("connect"          , 2),
    DISCONNECT  ("disconnect"       , 3),
    CREATE      ("create"           , 4),
    WAIT        ("wait"             , 5),
    START_I     ("player_start"     , 6),
    START_O     ("opponent_start"   , 7),
    TURN        ("turn"             , 8),
    JOIN        ("join"             , 9),
    INVALID_IN  ("invalid_input"    , 10),
    WIN         ("win"              , 11),
    LOSE        ("lose"             , 12),
    END         ("end"              , 13),
    OPP_TURN    ("opponent_turn"    , 14);

    private final String name;
    private final int code;

    private Action(String name, int code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Get action name (text)
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get code of action ("index")
     * @return int
     */
    public int getCode() {
        return code;
    }
}