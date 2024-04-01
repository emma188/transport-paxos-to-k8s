package paxos_pack;

import framework.*;
import lombok.Data;

@Data
public final class PaxosReply implements Message {
    private final Result result;
}
