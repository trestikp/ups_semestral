package game;

public enum State {
    DISCONNECTED    ("disconnected"     , 0),
    PICKER          ("picker"           , 1),
    CONNECTED       ("connected"        , 2),
    CREATING_LOBBY  ("creating lobby"   , 3),
    CHOOSING_LOBBY  ("choosing lobby"   , 4),
    WAITING         ("waiting"          , 5),
    TURN            ("turn"             , 6),
    OPPONENT_TURN   ("opponents turn"   , 7),
    PAUSE           ("pause"            , 8),
    NOT_ALLOWED     ("not allowed"      , 9),
    END             ("end"              , 10);
//    INIT            ("init"         , 0),
//    CONNECTED       ("connected"    , 1),
//    WAITING         ("waiting"      , 2),
//    WORKING         ("wokring"      , 3),
//    PAUSED          ("paused"       , 4),
//    END             ("end"          , 5),
//    DISCONNECTED    ("disconnected" , 6),
//    ERROR           ("error"        , 7);

    private final String name;
    private final int code;

    private State(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
}