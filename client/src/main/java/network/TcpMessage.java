package network;

import java.util.Arrays;

public class TcpMessage {
    private int player_id; // String?
    private Instruction inst = null;
    private int responseCode;
    private String responseText;
    private String[] params;


    /**
     * Creates TcpMessage from String received from server
     * @param serverRes server response
     */
    public TcpMessage(String serverRes) {
        String[] parts = serverRes.split("\\|");

        if(parts.length < 4) return;

        //TODO: format of response seems player_id|instruction|code|response_text
        try {
            try {
                player_id = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                player_id = -1;
                System.err.println("Failed to parse ID");
            }

            for (int i = 0; i < Instruction.values().length; i++) {
                if (Instruction.values()[i].getName().equals(parts[1])) {
                    inst = Instruction.values()[i];
                    break;
                }
            }

            if (inst == null) {
                inst = Instruction.INST_ERROR;
            }

            try {
                responseCode = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                responseCode = -1;
                System.err.println("Failed to parse response code");
            }

            responseText = parts[3];

            params = Arrays.copyOfRange(parts, 4, parts.length);
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Some part of the message doesn't suit my protocol");
        }
    }

    public boolean validateTcpMessage(Instruction i) {
        if(player_id == -1) {
            return false;
        }

        if(inst == Instruction.INST_ERROR) {
            return false;
        } else if(inst == Instruction.PING) {
            return true;
        }

        if(responseCode < 200 || responseCode >= 500 || (responseCode >= 300 && responseCode < 400)) {
            return false;
        }

        if(responseText == null) {
            return false;
        }

        int p;

        switch (i) {
            case QUICK_PLAY: case CONNECT: p = 1; break;
            case TURN: p = 30; break;
            case LOBBY: p = 512; break; // "random" number, should have limited number of rooms on server
            default: p = 0;
        }

        return params.length <= p;
    }

    public int getPlayer_id() {
        return player_id;
    }

    public Instruction getInst() {
        return inst;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public String[] getParams() {
        return params;
    }
}
