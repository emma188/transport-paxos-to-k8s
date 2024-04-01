package Application;

import framework.*;
import lombok.Data;

@Data
public final class AMOCommand implements Command {
    // Your code here...
    private final Command command;
    private final String address;
    private final int sequenceNum;
}
