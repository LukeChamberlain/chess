package websocket.state;

public class State {
    stateType stateType;
    public enum stateType {
        SIGNEDOUT,
        SIGNEDIN,
        IN_GAME
    }
}
