package paxos_pack;


import framework.*;
import lombok.Data;

@Data
public final class PaxosRequest implements Message {
    // Your code here...
    private final Command command;
}
