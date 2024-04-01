package paxos_pack;

import framework.Timer;
import lombok.Data;

@Data
final class ClientTimer implements Timer {
    static final int CLIENT_RETRY_MILLIS = 100;
    private final PaxosRequest request;
}

@Data
final class HeartBeatTimer implements Timer {
    static final int HEARTBEAT_RETRY_MILLIS = 15;
}

@Data
final class CheckLeaderTimer implements Timer {
    static final int MAX_CHECK_RETRY_MILLIS = 100;
}

@Data
final class LeaderElectionTimer implements Timer {
    static final int MIN_CHECK_RETRY_MILLIS = 25;
}

@Data
final class ProposalTimer implements Timer {
    static final int PROPOSAL_RETRY_MILLIS = 25;
    private final int slotNum;
    private final PaxosRequest request;
    private final String clientAddress;
}