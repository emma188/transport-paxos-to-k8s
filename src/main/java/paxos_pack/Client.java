package paxos_pack;

import framework.*;
import Application.*;
import Application.KVStore.*;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import static java.lang.Thread.sleep;
import static paxos_pack.HelperFunc.byteArrayToObject;
import static paxos_pack.HelperFunc.objectToByteArray;
import static paxos_pack.HelperFunc.*;

public class Client {
    //mqtt info
    String broker = "tcp://127.0.0.1:1883";
    int qos = 0;
    String clientId;
//    MqttClient client;
    //config
    String clientName;
    int serverNum;
    List<String> serverID_list = new ArrayList<String>();
    //paxos
    private PaxosRequest request;
    private PaxosReply reply;
    private int sequenceNum = 0;
    Boolean replyArrive = false;
    static Boolean serverDone = false;
    int count = 0;

    //配置文件加的
    private static final String CONFIG_FILE_NAME = "mqtt-config.properties";
    private static MqttClient client;
    private static Properties config;



    public Client(){
        clientId = MqttClient.generateClientId();
    }
    /*------------------------connection----------------------*/
    public void sizeConfig () throws IOException {
        String parentPath = System.getProperty("user.dir")+"/files/";
        String ServerConfigPath = parentPath+"sizeConfig.txt";
        BufferedReader in1 = new BufferedReader(new FileReader(ServerConfigPath));
        String[] sizes = (in1.readLine()).split(" ");
        serverNum = Integer.parseInt(sizes[0]);
//        System.out.println(serverNum);
    }
    public void config() throws IOException, InterruptedException {
        String parentPath = System.getProperty("user.dir")+"/files/";
        String ServerConfigPath = parentPath+"serverConfig.txt";
        String ClientConfigPath = parentPath+"clientConfig.txt";
        BufferedReader in1 = new BufferedReader(new FileReader(ClientConfigPath));
//        in1.readLine();
        int count_index = 0;
        String readClient;
        while ((readClient = in1.readLine()) != null) {
            count_index++;
        }
        clientName="client"+(count_index+1);
        String clientName_modify;
        if(clientName.equals("client2")){
            clientName_modify = clientName;
        }
        else {
            clientName_modify = clientName+"\n";
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(ClientConfigPath,true));
        out.write(clientName_modify);
        out.close();

//        //wait for all client arrives
//        int clientCount = 0;
//        while(serverCount < serverNum){
//            serverCount = 0;
//            BufferedReader in2 = new BufferedReader(new FileReader(ServerConfigPath));
//            String readServer2;
//            while ((readServer2 = in2.readLine()) != null) {
//                serverCount++;
//            }
//            sleep(1000);
//        }


        //read server config
//        BufferedReader server_in1 = new BufferedReader(new FileReader(ServerConfigPath));
//        serverNum = Integer.parseInt(server_in1.readLine());
        //wait for all server arrives
        int serverCount = 0;
        while(serverCount < serverNum){
            serverCount = 0;
            BufferedReader server_in2 = new BufferedReader(new FileReader(ServerConfigPath));
            String readServer;
            while ((readServer = server_in2.readLine()) != null) {
//                System.out.println(readServer);
                serverCount++;
            }
            sleep(1000);
        }

        //all server arrives, put servers' name in the system
        BufferedReader server_in2 = new BufferedReader(new FileReader(ServerConfigPath));
//        server_in2.readLine();
        String readServer2;
        while ((readServer2 = server_in2.readLine()) != null) {
            serverID_list.add(readServer2);
        }
//        for (int i = 0; i < serverID_list.size(); i++) {
//            System.out.println(serverID_list.get(i));
//        }
    }

    public void topic_in_subscribe() throws MqttException {
        for (int i = 0; i < serverID_list.size(); i++) {
            String topic = serverID_list.get(i)+"_"+clientName;
            client.subscribe(topic);
//            System.out.println("subscribe "+topic);
        }
    }

    public void temp() throws MqttException, IOException, InterruptedException{
//        InputStream inputStream = Client.class.getResourceAsStream(CONFIG_FILE_NAME);
//        config = new Properties();
//        config.load(inputStream);
//        String brokerUrl = config.getProperty("mqtt.broker.url");
//        String clientId1 = config.getProperty("mqtt.client.id");
//        String username = config.getProperty("mqtt.username");
//        String password = config.getProperty("mqtt.password");
//        int connectionTimeout = Integer.parseInt(config.getProperty("mqtt.connection.timeout"));
//        int keepAliveInterval = Integer.parseInt(config.getProperty("mqtt.keep.alive.interval"));

        String parentPath = System.getProperty("user.dir")+"/files/";
        String filename1 = parentPath+"mqtt-config.properties";
        BufferedReader input = new BufferedReader(new FileReader(filename1));
        input.readLine();
        String brokerUrl = (input.readLine()).split("=")[1];
        input.readLine();
        String username = (input.readLine()).split("=")[1];
        String password = (input.readLine()).split("=")[1];
        input.readLine();
        int connectionTimeout = Integer.parseInt((input.readLine()).split("=")[1]);
        input.readLine();
        int keepAliveInterval = Integer.parseInt((input.readLine()).split("=")[1]);

        client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(connectionTimeout);
        options.setMaxInflight(1000);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setCleanSession(true); // 设置清除会话，根据需要调整
        client.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
                cause.printStackTrace();
            }

            public void messageArrived(String topic, MqttMessage message) throws MqttException {
//                String msg1 = new String(message.getPayload());
//                if(msg1.equals("done")){
//                    System.out.println("done");
//                    count++;
//                    if(count == serverNum) {
//                        serverDone = true;
//                    }
//                }
//                else{
//                    byte[] msg = message.getPayload();
//                    PaxosReply request = (PaxosReply) byteArrayToObject(msg);
//                    handlePaxosReply(request);
//                }


                byte[] msg = message.getPayload();
                PaxosReply request = (PaxosReply) byteArrayToObject(msg);
                handlePaxosReply(request);

//                client.disconnect();

//                System.out.println("client receive "+new String(message.getPayload()));
//                String msg = "2222";
//                sendMsg(msg);
            }

            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
        client.connect(options);
        sizeConfig();
        config();
        topic_in_subscribe();
        System.out.println(clientName+" starts...");

    }

    public void connection() throws MqttException, IOException, InterruptedException {
        sizeConfig();
        config();
        client = new MqttClient(broker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();

        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(10);
        options.setMaxInflight(1000);

        client.setCallback(new MqttCallback() {
            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
                cause.printStackTrace();
            }

            public void messageArrived(String topic, MqttMessage message) throws MqttException {
//                String msg1 = new String(message.getPayload());
//                if(msg1.equals("done")){
//                    System.out.println("done");
//                    count++;
//                    if(count == serverNum) {
//                        serverDone = true;
//                    }
//                }
//                else{
//                    byte[] msg = message.getPayload();
//                    PaxosReply request = (PaxosReply) byteArrayToObject(msg);
//                    handlePaxosReply(request);
//                }


                byte[] msg = message.getPayload();
                PaxosReply request = (PaxosReply) byteArrayToObject(msg);
                handlePaxosReply(request);

//                client.disconnect();

//                System.out.println("client receive "+new String(message.getPayload()));
//                String msg = "2222";
//                sendMsg(msg);
            }

            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        IMqttToken token = client.connectWithResult(options);
        token.waitForCompletion();
        topic_in_subscribe();
        System.out.println(clientName+" starts...");
    }
    public void disconnection() throws MqttException{
        client.disconnect();
        client.close();
    }

    /*------------------------basic comm functions----------------------*/
    public void broadcast(Message msg) throws MqttException {
        byte[] msg_byte = objectToByteArray(msg);
        MqttMessage message = new MqttMessage(msg_byte);
        for (int i = 0; i < serverNum; i++) {
            String topic_out = clientName+"_"+serverID_list.get(i);
            client.publish(topic_out,message);
        }
    }

    /*------------------------paxos code----------------------*/
    public void sendCommand(Command operation) throws MqttException, InterruptedException {
        request = new PaxosRequest(new AMOCommand(operation, clientName,sequenceNum++));
        reply = null;

        while (replyArrive == false){
//            System.out.println(clientName+ " send "+request.getCommand());
            broadcast(request);
            sleep(100);
        }
        //reset to send new command
        replyArrive = false;
//        set(new ClientTimer(request), ClientTimer.CLIENT_RETRY_MILLIS);
    }
    public boolean hasResult() {
        return reply != null;
    }

    public Result getResult() throws InterruptedException {
        // Your code here...
        AMOResult result = (AMOResult) reply.getResult();
        return result.getResult();
    }

    private  void handlePaxosReply(PaxosReply m) {

        AMOCommand currentAmoCommand = (AMOCommand) request.getCommand();
        AMOResult result = (AMOResult) m.getResult();
        if (result.getSequenceNum() == currentAmoCommand.getSequenceNum()) {
            //System.out.println(this.address() + " Recieved Reply " + m + " from: " + sender );
            reply = m;
            replyArrive = true;
        }
        System.out.println(reply);
    }




    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
//        System.out.println("aaa");
//        Properties config;
//        String CONFIG_FILE_NAME = "mqtt-config.properties";
////        System.out.println(Client.class.getClassLoader().getResource("mqtt-config.properties").getPath());
//        try (InputStream inputStream = Client.class.getResourceAsStream(CONFIG_FILE_NAME)) {
//            config = new Properties();
//            config.load(inputStream);
//            String brokerUrl = config.getProperty("mqtt.broker.url");
//            System.out.println(brokerUrl);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }




//        System.out.println(System.getProperty("user.dir"));
        String parentPath = System.getProperty("user.dir")+"/files/";

        String testFile = parentPath+"test1.txt";
        Client client1 = new Client();
//        client1.connection();
        client1.temp();
        test(testFile, client1);
        if(client1.clientName.equals("client2")){
            FileWriter fileWriter = new FileWriter(parentPath+"clientConfig.txt");
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
            FileWriter fileWriter2 = new FileWriter(parentPath+"serverConfig.txt");
            fileWriter2.write("");
            fileWriter2.flush();
            fileWriter2.close();
        }
        client1.disconnection();



    }

}
