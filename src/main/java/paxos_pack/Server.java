package paxos_pack;

import framework.*;
import Application.*;
import framework.Timer;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.io.*;
import java.util.*;

import static java.lang.Thread.sleep;
import static paxos_pack.HelperFunc.*;

public class Server {
    private static final String CONFIG_FILE_NAME = "mqtt-config.properties";
    private static MqttClient server;
    private static Properties config;

    //mqtt info
    String broker = "tcp://127.0.0.1:1883";
    int qos = 0;
    String serverId;
//    MqttClient server;
    //config
    int serverNum;
    String serverName;
    List<String> serverID_list = new ArrayList<String>();
    int clientNum;
    List<String> clientID_list = new ArrayList<String>();
    /*--------------------paxos------------------------*/
    int serverIndex;
    private AMOApplication<Application> amoApplication;
    // paxos log related constants
    private int currBallotNum;
    private String currentLeader;
    private int slotIn = 1; // slot in should start with 1 as per the definition.
    private int slotOut = 1;
    private int lastExecutedSlotNum = 0;
    private int proposalCount = 0;
    private TreeMap<Integer, LogEntry> paxosLog;
    private HashMap<Integer, Set<String>> acceptedServers; // acceptedCounts For each slot.

    // Election
    private HashSet<String> acceptedProposals;
    private int heartBeatNum = 0;
    private enum State {
        ACCEPTOR, LEADER, RUNNING
    }
    private State currState;

    //garbage collection
    private HashMap<String, Integer> ServersGarbageResponse;
    private int cnt;

    // leader health
    private boolean IS_LEADER_ALIVE = false;
    private enum LeaderState {
        ALIVE, WAIT, DEAD
    }
    private LeaderState currLeaderState;

    //timers
    LeaderElectionTimer leaderElectionTimer;
    CheckLeaderTimer checkLeaderTimer;
    HeartBeatTimer heartBeatTimer;


    public Server(){
        serverId = MqttClient.generateClientId();
        this.amoApplication = new AMOApplication<>((Application) new KVStore());
    }
    /*------------------------connection----------------------*/
    public void sizeConfig () throws IOException {
        String parentPath = System.getProperty("user.dir")+"/files/";
        String ServerConfigPath = parentPath+"sizeConfig.txt";
        BufferedReader in1 = new BufferedReader(new FileReader(ServerConfigPath));
        String[] sizes = (in1.readLine()).split(" ");
        serverNum = Integer.parseInt(sizes[0]);
        clientNum = Integer.parseInt(sizes[1]);
    }

    public void serverConfig_Sub() throws IOException, InterruptedException, MqttException {
        String parentPath = System.getProperty("user.dir")+"/files/";
        String ServerConfigPath = parentPath+"serverConfig.txt";
        BufferedReader in1 = new BufferedReader(new FileReader(ServerConfigPath));
//        serverNum = Integer.parseInt(in1.readLine());
        int count_index = 0;
        String readServer;
        while ((readServer = in1.readLine()) != null) {
            count_index++;
        }
        serverName="server"+(count_index+1);
        serverIndex = count_index+1;
        String serverName_modify;
        if(serverName.equals("server3")){
            serverName_modify = serverName;
        }
        else {
            serverName_modify = serverName+"\n";
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(ServerConfigPath,true));
        out.write(serverName_modify);
        out.close();
        //wait for all server arrives
        int serverCount = 0;
        while(serverCount < serverNum){
            serverCount = 0;
            BufferedReader in2 = new BufferedReader(new FileReader(ServerConfigPath));
            String readServer2;
            while ((readServer2 = in2.readLine()) != null) {
                serverCount++;
            }
            sleep(1000);
        }
        //all server arrives, put servers' name in the system
        BufferedReader in3 = new BufferedReader(new FileReader(ServerConfigPath));
//        in3.readLine();
        String readServer3;
        while ((readServer3 = in3.readLine()) != null) {
//            if(!readServer3.equals(serverName)) {
            serverID_list.add(readServer3);
//            }
        }
//        for (int i = 0; i < serverID_list.size(); i++) {
//            System.out.println(serverID_list.get(i));
//        }
        for (int i = 0; i < serverNum; i++) {
            String topic = serverID_list.get(i) + "_" + serverName;
            server.subscribe(topic);
            System.out.println("subscribe "+topic);
        }
    }
    public void clientConfig_Sub() throws IOException, InterruptedException, MqttException {
        String parentPath = System.getProperty("user.dir")+"/files/";
        String ClientConfigPath = parentPath+"clientConfig.txt";
        //read client config
//        BufferedReader client_in1 = new BufferedReader(new FileReader(ClientConfigPath));
//        clientNum = Integer.parseInt(client_in1.readLine());
        //wait for all server arrives
        int clientCount = 0;
//        System.out.println(clientNum);
        while(clientCount < clientNum){
            clientCount = 0;
            BufferedReader client_in2 = new BufferedReader(new FileReader(ClientConfigPath));
            String readClient;
            while ((readClient = client_in2.readLine()) != null) {
//                System.out.println("111");
                clientCount++;
            }
//            System.out.println(clientCount);
            client_in2.close();
            sleep(1000);
        }

        //all client arrives, put clients' name in the system
        BufferedReader client_in3 = new BufferedReader(new FileReader(ClientConfigPath));
//        client_in3.readLine()3
        String readClient2;
        while ((readClient2 = client_in3.readLine()) != null) {
//            System.out.println("111");
            clientID_list.add(readClient2);
        }
//        for (int i = 0; i < clientID_list.size(); i++) {
//            System.out.println(clientID_list.get(i));
//        }
        for (int i = 0; i < clientNum; i++) {
            String topic = clientID_list.get(i)+"_"+serverName;
            server.subscribe(topic);
            System.out.println("subscribe "+topic);
        }
    }

    public void temp() throws MqttException, IOException, InterruptedException{
//        InputStream inputStream = Client.class.getResourceAsStream(CONFIG_FILE_NAME);
//        config = new Properties();
//        config.load(inputStream);
//        String brokerUrl = config.getProperty("mqtt.broker.url");
//        String clientId = config.getProperty("mqtt.client.id");
//        String username = config.getProperty("mqtt.username");
//        String password = config.getProperty("mqtt.password");
//        int connectionTimeout = Integer.parseInt(config.getProperty("mqtt.connection.timeout"));
//        int keepAliveInterval = Integer.parseInt(config.getProperty("mqtt.keep.alive.interval"));

//        String parentPath = System.getProperty("user.dir")+"/files/";
//        String filename1 = parentPath+"mqtt-config.properties";
//        BufferedReader input = new BufferedReader(new FileReader(filename1));
//        input.readLine();
//        String brokerUrl = (input.readLine()).split("=")[1];
//        input.readLine();
//        String client_ID = (input.readLine()).split("=")[1];
//        String username = (input.readLine()).split("=")[1];
//        String password = (input.readLine()).split("=")[1];
//        input.readLine();
//        int connectionTimeout = Integer.parseInt((input.readLine()).split("=")[1]);
//        input.readLine();
//        int keepAliveInterval = Integer.parseInt((input.readLine()).split("=")[1]);

        String brokerUrl = "ws://36.133.117.64:31238";
        String client_ID = "mqttx_062fbf92";
        String username = "user";
        String password = "PFrW5ZmqoBfhhyuLv2Ajs2vpe4y4Mbeu";
        int connectionTimeout = 30;
        int keepAliveInterval = 60;

        server = new MqttClient(brokerUrl, client_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(connectionTimeout);
        options.setMaxInflight(1000);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(true); // 设置清除会话，根据需要调整
        server.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
                cause.printStackTrace();
            }

            public void messageArrived(String topic, MqttMessage message) throws MqttException, InterruptedException {
//                System.out.println(serverName + " receive3 ");
//                if(serverName.equals("server1")) {
//                    System.out.println(serverName + " receive2 " + message);
//                }
//                String[] topic_list = topic.split("_");
//                String sender = topic_list[0];
//                byte[] body = message.getPayload();
//                Message msg = (Message) byteArrayToObject(body);
//                handleMessage(msg,sender);

                if(currentLeader == serverName) {
                    byte[] msg = message.getPayload();
                    PaxosRequest request = (PaxosRequest) byteArrayToObject(msg);
//                    System.out.println(serverName + " receive3 " + request);
                    Command command = request.getCommand();
                    AMOCommand amoCommand = (AMOCommand) command;
                    System.out.println(amoCommand);
                    String sender = amoCommand.getAddress();
                    if (!amoApplication.alreadyExecuted(amoCommand)) {
                        Result res = amoApplication.execute(amoCommand);
//                    sleep(5000);
                        send(new PaxosReply(res), sender);
                    }
                }



//                byte[] msg = message.getPayload();
//                PaxosRequest request = (PaxosRequest) byteArrayToObject(msg);
//                System.out.println("server receive "+request.getCommand());
//                System.out.println("server receive "+new String(message.getPayload()));
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
//                System.out.println("deliveryComplete---------" + token.isComplete());
            }
        });
//        server.disconnect();
        server.connect();
        System.out.println("server starts...");
//        server.subscribe("aaa");

//        IMqttToken token = server.connectWithResult(options);
//        token.waitForCompletion();
//        sizeConfig();
//        serverConfig_Sub();
//        System.out.println("server starts...");
//        init();
//        clientConfig_Sub();

    }

    public void connection() throws MqttException, IOException, InterruptedException {
        server = new MqttClient(broker, serverId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setKeepAliveInterval(10);
        options.setConnectionTimeout(0);
        options.setMaxInflight(1000);
        server.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
                cause.printStackTrace();
            }

            public void messageArrived(String topic, MqttMessage message) throws MqttException, InterruptedException {
//                System.out.println(serverName + " receive3 ");
//                if(serverName.equals("server1")) {
//                    System.out.println(serverName + " receive2 " + message);
//                }
//                String[] topic_list = topic.split("_");
//                String sender = topic_list[0];
//                byte[] body = message.getPayload();
//                Message msg = (Message) byteArrayToObject(body);
//                handleMessage(msg,sender);

                if(currentLeader == serverName) {
                    byte[] msg = message.getPayload();
                    PaxosRequest request = (PaxosRequest) byteArrayToObject(msg);
//                    System.out.println(serverName + " receive3 " + request);
                    Command command = request.getCommand();
                    AMOCommand amoCommand = (AMOCommand) command;
                    System.out.println(amoCommand);
                    String sender = amoCommand.getAddress();
                    if (!amoApplication.alreadyExecuted(amoCommand)) {
                        Result res = amoApplication.execute(amoCommand);
//                    sleep(5000);
                        send(new PaxosReply(res), sender);
                    }
                }



//                byte[] msg = message.getPayload();
//                PaxosRequest request = (PaxosRequest) byteArrayToObject(msg);
//                System.out.println("server receive "+request.getCommand());
//                System.out.println("server receive "+new String(message.getPayload()));
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
//                System.out.println("deliveryComplete---------" + token.isComplete());
            }
        });

        IMqttToken token = server.connectWithResult(options);
        token.waitForCompletion();
        sizeConfig();
        serverConfig_Sub();
        System.out.println("server starts...");
        init();
        clientConfig_Sub();
//        for (int i = 0; i < clientNum; i++) {
//            server.publish(serverName+"_"+clientID_list.get(i),new MqttMessage(("done").getBytes()));
//        }
    }

    public void disconnection() throws MqttException{
        server.disconnect();
        server.close();
    }

    /*------------------------basic comm functions----------------------*/
    public void send(Message msg, String to) throws MqttException {
        byte[] msg_byte = objectToByteArray(msg);
        MqttMessage message = new MqttMessage(msg_byte);
        String topic_out = serverName+"_"+to;
        server.publish(topic_out,message);
    }

    public void broadcast(Message msg) throws MqttException {
        byte[] msg_byte = objectToByteArray(msg);
        MqttMessage message = new MqttMessage(msg_byte);
        for (int i = 0; i < serverNum; i++) {
            String topic_out = serverName+"_"+serverID_list.get(i);
            server.publish(topic_out,message);
        }
    }

    /*------------------------paxos code----------------------*/

    public void init() throws MqttException, InterruptedException {
        currBallotNum = 0;
        acceptedProposals = new HashSet<>();

        paxosLog = new TreeMap<>();
        acceptedServers = new HashMap<>();

        ServersGarbageResponse = new HashMap<>();
        cnt = 0;

        checkLeaderTimer = new CheckLeaderTimer();
        leaderElectionTimer = new LeaderElectionTimer();
        heartBeatTimer = new HeartBeatTimer();

        currState = State.ACCEPTOR;
        currLeaderState = LeaderState.DEAD;

//        String address = this.address().toString();
//        int index = getIndex();

        if (serverNum > 1) {
//            set(checkLeaderTimer, CheckLeaderTimer.MAX_CHECK_RETRY_MILLIS);
            if(serverName.equals("server1")){
                currState = State.LEADER;
                currLeaderState = LeaderState.ALIVE;
                IS_LEADER_ALIVE = true;
                currentLeader = serverName;
            }
        }
        else {
            currState = State.LEADER;
            currLeaderState = LeaderState.ALIVE;
            IS_LEADER_ALIVE = true;
            currentLeader = serverName;
        }
    }
    public PaxosLogSlotStatus status(int logSlotNum) {
        if (!paxosLog.containsKey(logSlotNum))
            return PaxosLogSlotStatus.EMPTY;

        return paxosLog.get(logSlotNum).getStatus();
    }
    public Command command(int logSlotNum) {
        if (!paxosLog.containsKey(logSlotNum) || paxosLog.get(logSlotNum).getStatus().equals(PaxosLogSlotStatus.EMPTY))
            return null;

        if (paxosLog.get(logSlotNum).getRequest().getCommand() instanceof AMOCommand) {
            AMOCommand cmd = (AMOCommand) paxosLog.get(logSlotNum).getRequest().getCommand();
            return cmd.getCommand();
        } else {
            // added for lab4 commands
            return paxosLog.get(logSlotNum).getRequest().getCommand();
        }
    }
    public int firstNonCleared() {
        return slotOut;
    }
    public int lastNonEmpty() {
        if (paxosLog.isEmpty())
            return 0;

        return paxosLog.lastKey();
    }

    /*-----------------------handle function---------------------*/
    private void handleMessage(Message m, String sender) throws MqttException, InterruptedException {
//        if(serverName.equals("server1")) {
//            System.out.println(serverName + " receive1 " + m);
//        }
        if(m instanceof PaxosRequest){
            handlePaxosRequest((PaxosRequest) m,sender);
        }
        else if(m instanceof PrepareMessage){
            handlePrepareMessage((PrepareMessage) m,sender);
        }
        else if(m instanceof PromiseMessage){
            handlePromiseMessage((PromiseMessage) m,sender);
        }
        else if(m instanceof AcceptMessage){
            handleAcceptMessage((AcceptMessage) m,sender);
        }
        else if(m instanceof AcceptedMessage){
            handleAcceptedMessage((AcceptedMessage) m,sender);
        }
        else if(m instanceof DecisionMessage){
            handleDecisionMessage((DecisionMessage) m,sender);
        }
        else if(m instanceof HeartBeatMessage){
            handleHeartBeatMessage((HeartBeatMessage) m,sender);
        }
    }

    private void handlePaxosRequest(PaxosRequest m, String sender) throws MqttException, InterruptedException {
        if (currState.equals(State.LEADER)) {
//            System.out.println(serverName+" receive "+m);
            if (checkIfRequestExistsInLog(m)) return;
            acceptedServers.put(slotIn, new HashSet<>());
            proposeCommand(slotIn, m, sender);
            set(new ProposalTimer(slotIn, m, sender), ProposalTimer.PROPOSAL_RETRY_MILLIS);
            slotIn++;
        }

    }

    private void handlePrepareMessage(PrepareMessage m, String sender) throws MqttException, InterruptedException {

        if (m.getBallotNum() > currBallotNum) {
            IS_LEADER_ALIVE = true;
            currBallotNum = m.getBallotNum();
            ServersGarbageResponse = new HashMap<>();
            cnt = 0;
            if (!sender.equals(serverName)) {
                currState = State.ACCEPTOR;
                markLeaderAsAlive();
                heartBeatNum = 0;
            }
            currentLeader = sender;
            if (serverName.equals(sender)) handlePromiseMessage(new PromiseMessage(m.getBallotNum(), paxosLog), sender);
            else send(new PromiseMessage(m.getBallotNum(), paxosLog), sender);
        }
    }
    private void handlePromiseMessage(PromiseMessage m, String sender) throws MqttException, InterruptedException {
        if (m.getBallotNum() == currBallotNum && currState.equals(State.RUNNING)) {
            acceptedProposals.add(sender);

            if (sender != serverName) {
                mergeLog(m.getLogList());
            }
            ServersGarbageResponse = new HashMap<>();
            cnt = 0;
            if (acceptedProposals.size() >= (serverNum / 2 + 1)) {
                currState = State.LEADER;
                for (String object : acceptedProposals) {
                    System.out.println(object);
                }
                System.out.println(serverName+" is leader!!!!!!!!!!!!! "+currBallotNum);
                IS_LEADER_ALIVE = true;
                currLeaderState = LeaderState.ALIVE;
                heartBeatNum = 1;

                broadcast(new HeartBeatMessage(heartBeatNum, currBallotNum, paxosLog, lastExecutedSlotNum, amoApplication, slotOut));
                set(heartBeatTimer, HeartBeatTimer.HEARTBEAT_RETRY_MILLIS);

                reProposeAcceptedMessages();
            }
        }
    }
    private void handleAcceptMessage(AcceptMessage m, String sender) throws MqttException {
        // old messages
        if (lastExecutedSlotNum > m.getSlotNum()) return;
        if (paxosLog.containsKey(m.getSlotNum())
                && (paxosLog.get(m.getSlotNum()).getStatus() == PaxosLogSlotStatus.CHOSEN
                || paxosLog.get(m.getSlotNum()).getStatus() == PaxosLogSlotStatus.CLEARED)) return;

        if (m.getBallotNum() == currBallotNum && sender.equals(currentLeader) ) {
            markLeaderAsAlive();
            heartBeatNum = Math.max(m.getHeartBeatNum(), heartBeatNum);
            LogEntry logEntry = new LogEntry(currBallotNum, m.getRequest(), PaxosLogSlotStatus.ACCEPTED, m.getClientAddress());
            paxosLog.put(m.getSlotNum(), logEntry);
            if (sender.equals(serverName)) handleAcceptedMessage(new AcceptedMessage(m.getSlotNum(), m.getBallotNum(), logEntry, lastExecutedSlotNum), sender);
            else send(new AcceptedMessage(m.getSlotNum(), m.getBallotNum(), logEntry, lastExecutedSlotNum), sender);
        }
    }

    private void handleAcceptedMessage(AcceptedMessage m, String sender) throws MqttException {

        if (currState.equals(State.LEADER) && m.getBallotNum() == currBallotNum) {
            if (!acceptedServers.get(m.getSlotNum()).contains(sender)) {
                acceptedServers.get(m.getSlotNum()).add(sender);

                if (m.getLastExecutedSlot() >= slotOut) {
                    ServersGarbageResponse.put(sender, m.getLastExecutedSlot());
                }

                if (acceptedServers.get(m.getSlotNum()).size() >= (serverNum / 2) + 1) {
                    if (paxosLog.containsKey(m.getSlotNum()) && paxosLog.get(m.getSlotNum()).getStatus() == PaxosLogSlotStatus.CLEARED) {}
                    else {
                        LogEntry logEntry = new LogEntry(currBallotNum,
                                m.getLogEntry().getRequest(),
                                PaxosLogSlotStatus.CHOSEN, m.getLogEntry()
                                .getClientAddress());
                        paxosLog.put(m.getSlotNum(), logEntry);
                        checkAndExecuteCommands();
                    }

                } else {
                    LogEntry logEntry = new LogEntry(currBallotNum,
                            m.getLogEntry().getRequest(),
                            PaxosLogSlotStatus.ACCEPTED, m.getLogEntry().getClientAddress());
                    paxosLog.put(m.getSlotNum(), logEntry);
                }

//                if (ServersGarbageResponse.size() >= servers.length) {
//                    clearLog();
//                }
            }
        }
    }

    private void handleDecisionMessage(DecisionMessage m, String sender) throws MqttException {

        if (m.getBallotNum() == currBallotNum && sender.equals(currentLeader)) {
            markLeaderAsAlive();

            if (paxosLog.containsKey(m.getSlotNum()) && paxosLog.get(m.getSlotNum()).getStatus() == PaxosLogSlotStatus.CLEARED) {

                return;
            }
            LogEntry logEntry = new LogEntry(m.getLogEntry().getBallotNum(),
                    m.getLogEntry().getRequest(),
                    PaxosLogSlotStatus.CHOSEN, m.getLogEntry().getClientAddress());
            paxosLog.put(m.getSlotNum(), logEntry);
            checkAndExecuteCommands();
        }
    }

    private void handleHeartBeatMessage(HeartBeatMessage m, String sender) {
        System.out.println(serverName+" heart from "+sender+" "+m.getBallotNum()+" "+currBallotNum);

        int oldLastExecutedSlotNum = this.lastExecutedSlotNum;
        if (serverName.equals(sender)) return;
        if (m.getLastExecutedSlotNum() < this.lastExecutedSlotNum) return;
        if (sender.equals(currentLeader) && m.getHeartBeatNum() <= heartBeatNum) return;

        if (m.getBallotNum() >= currBallotNum) {
            markLeaderAsAlive();
            heartBeatNum = m.getHeartBeatNum();
            currBallotNum = m.getBallotNum();
            currentLeader = sender;
            amoApplication = m.getAmoApplication();
            paxosLog = m.getLogList();
            this.lastExecutedSlotNum = m.getLastExecutedSlotNum();
            currState = State.ACCEPTOR;
            slotIn = (!paxosLog.isEmpty()) ? paxosLog.lastKey() + 1 : 1;
            slotOut = m.getSlotOut();
            //TODO remove && shardMaster != null
            if (this.lastExecutedSlotNum > oldLastExecutedSlotNum)
                sendDecidedMessages(oldLastExecutedSlotNum);
        }
    }

    private void sendDecidedMessages(int slotNum) {
        int slot = slotNum;
//        System.out.println(address() + " " + currBallotNum +" " + slot + "-" + lastExecutedSlotNum + "\n" + paxosLog + "\n");
        while (slot <= lastExecutedSlotNum) {
//            if (paxosLog.containsKey(slot) &&
//                    (paxosLog.get(slot).getStatus().equals(PaxosLogSlotStatus.CHOSEN) ||
//                            paxosLog.get(slot).getStatus().equals(PaxosLogSlotStatus.EMPTY))) {
//                if (paxosLog.get(slot).getStatus().equals(PaxosLogSlotStatus.CHOSEN)) {
//                    handleMessage(new PaxosReplyForShardMaster(
//                            paxosLog.get(slot).request().command()), shardMaster);
//                }
//            }
            slot++;
        }
    }

    private void markLeaderAsAlive() {
        currLeaderState = LeaderState.ALIVE;
        IS_LEADER_ALIVE = true;
    }
    /*----------------------------Timer------------------------*/
    private void set(Timer t, int time) throws InterruptedException, MqttException {
        sleep(time);
        if(t instanceof HeartBeatTimer){
            onHeartBeatTimer((HeartBeatTimer) t);
        }
        else if(t instanceof CheckLeaderTimer){
            onCheckLeaderTimer((CheckLeaderTimer) t);
        }
        else if(t instanceof LeaderElectionTimer){
            onLeaderElectionTimer((LeaderElectionTimer) t);
        }
        else if(t instanceof ProposalTimer){
            onProposalTimer((ProposalTimer) t);
        }

    }

    private void onHeartBeatTimer(HeartBeatTimer t) throws MqttException, InterruptedException {
        if (!currState.equals(State.LEADER)) return;
        heartBeatNum++;
        broadcast(new HeartBeatMessage(heartBeatNum, currBallotNum, paxosLog, lastExecutedSlotNum, amoApplication, slotOut));
        set(t, HeartBeatTimer.HEARTBEAT_RETRY_MILLIS);
    }

    private void onProposalTimer(ProposalTimer t) throws MqttException, InterruptedException {
        if (currState.equals(State.LEADER)) {
            if (!paxosLog.containsKey(t.getSlotNum())
                    || (!paxosLog.get(t.getSlotNum()).getStatus().equals(PaxosLogSlotStatus.CHOSEN)
                    && !paxosLog.get(t.getSlotNum()).getStatus().equals(PaxosLogSlotStatus.CLEARED))
                    || acceptedServers.get(t.getSlotNum()).size() < (serverNum /2 + 1))
            {
                proposeCommand(t.getSlotNum(), t.getRequest(), t.getClientAddress());
                set(t, ProposalTimer.PROPOSAL_RETRY_MILLIS);
            }
        }
    }

    // check if we received
    private void onCheckLeaderTimer(CheckLeaderTimer t) throws MqttException, InterruptedException {

        if (!currState.equals(State.ACCEPTOR)) {
            set(t, CheckLeaderTimer.MAX_CHECK_RETRY_MILLIS);
            return;
        }

        if (currLeaderState == LeaderState.WAIT) {
            currLeaderState = LeaderState.DEAD;
            IS_LEADER_ALIVE = false;
        } else if (currLeaderState == LeaderState.ALIVE) {
            currLeaderState = LeaderState.WAIT;
        }

        if (!IS_LEADER_ALIVE) {
            currState = State.RUNNING;
            runForElection(getNextBallotNum());
        }

        set(t, CheckLeaderTimer.MAX_CHECK_RETRY_MILLIS);
    }

    private void onLeaderElectionTimer(LeaderElectionTimer t) throws MqttException, InterruptedException {

        if (currState.equals(State.RUNNING)) {
            IS_LEADER_ALIVE = false;
            runForElection(getNextBallotNum());
        }
    }

    /*----------------------------Utils------------------------*/
    private int getRandValue(int min, int max) {
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }
    private int getNextBallotNum() {
        proposalCount++;
        return (2 * serverIndex) + currBallotNum;
    }
    private void runForElection(int ballotNum) throws MqttException, InterruptedException {
        acceptedProposals.clear();
        broadcast(new PrepareMessage(ballotNum));
        handlePrepareMessage(new PrepareMessage(ballotNum), serverName);
        set(leaderElectionTimer, serverIndex * LeaderElectionTimer.MIN_CHECK_RETRY_MILLIS);
    }

    private void mergeLog(SortedMap<Integer, LogEntry> acceptorLog) {
        if (acceptorLog.isEmpty())
        {
            slotIn = paxosLog.isEmpty() ? 1 : paxosLog.lastKey() + 1;
            return;
        }

        int lastEntry = Math.max(acceptorLog.lastKey(), paxosLog.isEmpty() ? 0 : paxosLog.lastKey());

        for (int i = lastExecutedSlotNum + 1; i <= lastEntry; i++) {
            LogEntry logEntry;

            // if both the acceptor and server doesn't know about the log entry
            if (!acceptorLog.containsKey(i)) {
                if (!paxosLog.containsKey(i)){
                    paxosLog.put(i, new LogEntry(currBallotNum, null, PaxosLogSlotStatus.EMPTY, null));
                }
            }

            else if (!paxosLog.containsKey(i)
                    || paxosLog.get(i).getStatus().equals(PaxosLogSlotStatus.EMPTY)
                    || acceptorLog.get(i).getStatus().equals(PaxosLogSlotStatus.CHOSEN)) {
                if (paxosLog.containsKey(i) && paxosLog.get(i).getStatus() == PaxosLogSlotStatus.CLEARED) {

                    continue;
                }
                paxosLog.put(i, acceptorLog.get(i));
            }

            else if (acceptorLog.get(i).getStatus().equals(PaxosLogSlotStatus.ACCEPTED)
                    && acceptorLog.get(i).getBallotNum() > paxosLog.get(i).getBallotNum()) {

                logEntry = new LogEntry(
                        acceptorLog.get(i).getBallotNum(),
                        acceptorLog.get(i).getRequest(),
                        PaxosLogSlotStatus.ACCEPTED,
                        acceptorLog.get(i).getClientAddress());
                paxosLog.put(i, logEntry);
            }

            else if (acceptorLog.get(i).getStatus().equals(PaxosLogSlotStatus.CLEARED)) {

                logEntry = new LogEntry(acceptorLog.get(i).getBallotNum(), new PaxosRequest(new AMOCommand(null, null, 0)), PaxosLogSlotStatus.CLEARED, null);
                paxosLog.put(i, logEntry);
            }
        }

        slotIn = paxosLog.lastKey() + 1;
    }

    private void clearLog() {

        int minReplicaExecutedSlot = Collections.min(ServersGarbageResponse.values());

        for (int i = slotOut; i <= minReplicaExecutedSlot; i++) {

            if (!paxosLog.containsKey(i)) continue;
            if (paxosLog.get(i).getStatus().equals(PaxosLogSlotStatus.EMPTY)) {
                minReplicaExecutedSlot = i-1;
                break;
            }
            LogEntry prevLogEntry = paxosLog.get(i);
            LogEntry newLogEntry = new LogEntry(prevLogEntry.getBallotNum(), new PaxosRequest(new AMOCommand(null, null, 0)), PaxosLogSlotStatus.CLEARED, null);
            paxosLog.put(i, newLogEntry);
        }
        slotOut = minReplicaExecutedSlot + 1;
        ServersGarbageResponse.clear();
//        cnt = 0;
    }
    private void proposeCommand(int slot, PaxosRequest request, String clientAddress) throws MqttException {
        heartBeatNum++;
        AcceptMessage acceptMessage = new AcceptMessage(heartBeatNum, slot, currBallotNum, request, clientAddress);
        LogEntry logEntry = new LogEntry(currBallotNum, request, PaxosLogSlotStatus.ACCEPTED, clientAddress);
        paxosLog.put(slot, logEntry);
        for (String server : serverID_list) {
            if (server.equals(serverName)) handleAcceptMessage(acceptMessage, server);
            else send(acceptMessage, server);
        }
    }

    // done after the server election.
    private void reProposeAcceptedMessages() throws MqttException, InterruptedException {
        if (paxosLog.isEmpty()) return;
        checkAndExecuteCommands();
        int lastEntry = paxosLog.lastKey();
        for (int i = lastExecutedSlotNum + 1; i <= lastEntry; i++) {
            if (!paxosLog.containsKey(i)) {
                LogEntry logEntry = new LogEntry(currBallotNum, null, PaxosLogSlotStatus.EMPTY, null);
                continue;
            }
            if (paxosLog.get(i).getStatus().equals(PaxosLogSlotStatus.ACCEPTED)) {
                acceptedServers.put(i, new HashSet<>());
                proposeCommand(i, paxosLog.get(i).getRequest(), paxosLog.get(i).getClientAddress());
                set(new ProposalTimer(i, paxosLog.get(i).getRequest(), paxosLog.get(i).getClientAddress()), ProposalTimer.PROPOSAL_RETRY_MILLIS);
            }
        }
    }
    private boolean checkIfRequestExistsInLog(PaxosRequest request) {
        for (int i = lastExecutedSlotNum + 1; i < slotIn; i++) {
            if (paxosLog.containsKey(i) && !paxosLog.get(i).getStatus().equals(PaxosLogSlotStatus.EMPTY)) {
                if (paxosLog.get(i).getRequest().equals(request)) return true;
            }
        }
        return false;
    }

    // check if any
    private void checkAndExecuteCommands() throws MqttException {
        if (paxosLog.isEmpty()) return;
        int idx = lastExecutedSlotNum + 1;
        while (paxosLog.containsKey(idx) &&
                (paxosLog.get(idx).getStatus().equals(PaxosLogSlotStatus.CHOSEN)
                        || paxosLog.get(idx).getStatus().equals(PaxosLogSlotStatus.EMPTY)))
        {
            if (paxosLog.get(idx).getStatus().equals(PaxosLogSlotStatus.CHOSEN)) {
                ExecuteOrForwardCommand(paxosLog.get(idx), idx);
//                lastExecutedSlotNum++;
            }
            lastExecutedSlotNum++;
            idx++;
        }
//        System.out.println(address() + " " + currBallotNum + " " + lastExecutedSlotNum + "-" + idx + "\n" + paxosLog + "\n");
    }

    private void ExecuteOrForwardCommand(LogEntry logEntry, int idx) throws MqttException {

        broadcast(new DecisionMessage(currBallotNum, idx, paxosLog.get(idx)));

        Command cmd = logEntry.getRequest().getCommand();
        Result res = null;

        if (cmd instanceof AMOCommand) {
            AMOCommand amoCommand = (AMOCommand) logEntry.getRequest().getCommand();
            res = amoApplication.execute(amoCommand);
        }

        if (this.currState.equals(State.LEADER)) {
            send(new PaxosReply(res), logEntry.getClientAddress());
        }
    }




    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
        Server server = new Server();
//        server.connection();
        server.temp();
    }


}
