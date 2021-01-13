package network;

public enum Instruction {
    /** requests */
//    HELLO           ("hello"        , 0),
//    SHAKES          ("shakes"       , 1),
    CONNECT         ("CONNECT"      , 2),
    JOIN_GAME       ("JOIN_GAME"    , 3),
    TURN            ("TURN"         , 4),
    PAUSE           ("PAUSE"        , 5),
    RESUME          ("RESUME"       , 6),
    PING            ("PING"         , 7),
    DISCONNECT      ("DISCONNECT"   , 8),
    CREATE_LOBBY    ("CREATE_LOBBY" , 9),
    QUICK_PLAY      ("QUICK_PLAY"   , 10),
    CANCEL_QUICK    ("CANCEL_QUICK" , 11),
    DELETE_LOBBY    ("DELETE_LOBBY" , 12),
    LOBBY           ("LOBBY"        , 13),
    OPPONENT_JOIN   ("OPPONENT_JOIN", 14),
    OPPONENT_TURN   ("OPPONENT_TURN", 15),
    /** responses */
//    HANDSHAKE       ("handshake"    , 50),
    INST_ERROR      ("INST_ERROR"   , 50),
    OK              ("OK"           , 200),
    ERROR           ("ERROR"        , 400);

    private final String name;
    private final int code;

    private Instruction(String name, int code) {
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
