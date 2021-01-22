package network;

public enum Instruction {
    /** requests */
    CONNECT         ("CONNECT"      , 0),
    JOIN_GAME       ("JOIN_GAME"    , 1),
    TURN            ("TURN"         , 2),
    PING            ("PING"         , 3),
    DISCONNECT      ("DISCONNECT"   , 4),
    CREATE_LOBBY    ("CREATE_LOBBY" , 5),
    QUICK_PLAY      ("QUICK_PLAY"   , 6),
    CANCEL_QUICK    ("CANCEL_QUICK" , 7),
    DELETE_LOBBY    ("DELETE_LOBBY" , 8),
    LOBBY           ("LOBBY"        , 9),
    OPPONENT_JOIN   ("OPPONENT_JOIN", 10),
    OPPONENT_TURN   ("OPPONENT_TURN", 11),
    OPPONENT_DISC   ("OPPONENT_DISC", 12),
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
