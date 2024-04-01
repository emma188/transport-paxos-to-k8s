package paxos_pack;

import Application.KVStore;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.*;

public interface HelperFunc {
    void HelperFunc();
    static Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            System.out.println("byteArrayToObject failed: \n");
            e.printStackTrace();
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    System.out.println("close byteArrayInputStream failed: \n");
                    e.printStackTrace();
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    System.out.println("close objectInputStream failed: \n");
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }
    public static byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            System.out.println("objectToByteArray failed: \n");
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    System.out.println("close objectOutputStream failed: \n");
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    System.out.println("close byteArrayOutputStream failed: \n");
                    e.printStackTrace();
                }
            }

        }
        return bytes;
    }
    public static void writeConfig(int serverSize, int clienSize) throws IOException {
        String ServerConfigPath = "/Users/zhenhuansu/Desktop/cs551_paxos/src/main/java/files/serverConfig.txt";
        String ClientConfigPath = "/Users/zhenhuansu/Desktop/cs551_paxos/src/main/java/files/clientConfig.txt";
        BufferedWriter serverOut = new BufferedWriter(new FileWriter(ServerConfigPath));
        BufferedWriter clientOut = new BufferedWriter(new FileWriter(ClientConfigPath));
        serverOut.write(Integer.toString(serverSize));
        clientOut.write(Integer.toString(clienSize));
        serverOut.close();
        clientOut.close();
    }

    public static void test(String filename, Client client) throws IOException, MqttException, InterruptedException {
//        System.out.println("1");
        String clientName = client.clientName;
        BufferedReader in = new BufferedReader(new FileReader(filename));
//        //ignore config
//        in.readLine();
        String command_info;
        while (!(command_info = in.readLine()).contains(clientName)) {
//            System.out.println(command_info);
            continue;
        }
//        System.out.println("2");
        String[] info_list = command_info.split(" ");
        int commandSize = Integer.parseInt(info_list[1]);
        for (int i = 0; i < commandSize; i++) {
            String command = in.readLine();
            String[] command_list = command.split(" ");
            if(command_list[0].equals("put")){
//                System.out.println("3");
                KVStore.Put comm = new KVStore.Put(command_list[1],command_list[2]);
                client.sendCommand(comm);
            }
            else if(command_list[0].equals("append")){
//                System.out.println("4");
                KVStore.Append comm = new KVStore.Append(command_list[1],command_list[2]);
                client.sendCommand(comm);
            }
            else if(command_list[0].equals("get")){
//                System.out.println("5");
                KVStore.Get comm = new KVStore.Get(command_list[1]);
                client.sendCommand(comm);
            }
        }

    }
}
