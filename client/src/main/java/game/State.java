package game;

/**
 * Application states
 */
public enum State {
    DISCONNECTED    ("disconnected"     , 0),
    PICKER          ("picker"           , 1),
    CONNECTED       ("connected"        , 2),
    CREATING_LOBBY  ("creating lobby"   , 3),
    CHOOSING_LOBBY  ("choosing lobby"   , 4),
    WAITING         ("waiting"          , 5),
    TURN            ("turn"             , 6),
    OPPONENT_TURN   ("opponents_turn"   , 7),
    PAUSE           ("pause"            , 8),
    NOT_ALLOWED     ("not allowed"      , 9),
    LOST_CON        ("lost connection"  , 10),
    END             ("end"              , 11);

    private final String name;
    private final int code;

    private State(String name, int code) {
        this.name = name;
        this.code = code;
    }

    /**
     * Get application state name
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get application code ("index")
     * @return int
     */
    public int getCode() {
        return code;
    }
}