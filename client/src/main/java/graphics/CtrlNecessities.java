package graphics;

import game.Client;

/**
 * Interface for methods required by every controller using server connection
 */
public interface CtrlNecessities {
    /**
     * Must be implemented so controller has access to server communication
     * @param client instance
     */
    public void setClient(Client client);
}
