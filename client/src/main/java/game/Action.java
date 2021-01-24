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
    PAUSE       ("pause"            , 100);
//    CONNECT         ("connect"              , 0),
//    //REJECT          ("reject_connection"    , 1),
//    QUEUE           ("queue"                , 1),
//    START           ("play"                 , 2),
//    TURN            ("turn"                 , 3),
//    PAUSE           ("pause"                , 4),
//    RESUME          ("resume"               , 5),
//    WAIT            ("wait"                 , 6),
//    LOSS            ("connection loss"      , 7),
//    RECONNECT       ("reconnect"            , 8),
//    END             ("end"                  , 9),
//    DISCONNECT      ("disconnect"           , 10);
//    //ERROR           ("error"                , 8);


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