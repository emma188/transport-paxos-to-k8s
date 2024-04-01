package paxos_pack;

import framework.*;
import Application.*;
import java.io.Serializable;
import java.util.TreeMap;
import lombok.Data;


@Data
class LogEntry implements Serializable {
    private final int ballotNum;
    private final PaxosRequest request;
    private final PaxosLogSlotStatus status;
    private final String clientAddress;
}

@Data
class HeartBeatMessage implements Message {
    private final int heartBeatNum;
    private final int ballotNum;
    private final TreeMap<Integer, LogEntry> logList;
    private final int lastExecutedSlotNum;
    private final AMOApplication<Application> amoApplication;
//    private final Application shardMasterApplication;
    private final int slotOut;
}

@Data
class PrepareMessage implements Message {
    private final int ballotNum;
}

@Data
class PromiseMessage implements Message {
    private final int ballotNum;
    private final TreeMap<Integer, LogEntry> logList;
}

@Data
class AcceptMessage implements Message {
    private final int heartBeatNum;
    private final int slotNum;
    private final int ballotNum;
    private final PaxosRequest request;
    private final String clientAddress;
}

@Data
class AcceptedMessage implements Message {
    private final int slotNum;
    private final int ballotNum;
    private final LogEntry logEntry;
    private final int lastExecutedSlot;
    // TODO add a last executed slot Number which is helpful for the cleanup.
}

@Data
class DecisionMessage implements Message {
    private final int ballotNum;
    private final int slotNum;
    private final LogEntry logEntry;
}





