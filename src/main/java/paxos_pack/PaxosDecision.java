package paxos_pack;

import framework.*;
import Application.*;
import lombok.Data;

@Data
public final class PaxosDecision implements Message {
    private final AMOCommand command;
    private final int sequenceNum;
}
