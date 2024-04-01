package paxos_pack;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

import static paxos_pack.HelperFunc.*;

public class Test {
    public Test(){}
//    public static void writeConfig(int serverSize, int clienSize) throws IOException {
//        String ServerConfigPath = "/Users/zhenhuansu/Desktop/cs551_paxos/src/main/java/files/serverConfig.txt";
//        String ClientConfigPath = "/Users/zhenhuansu/Desktop/cs551_paxos/src/main/java/files/clientConfig.txt";
//        BufferedWriter serverOut = new BufferedWriter(new FileWriter(ServerConfigPath));
//        BufferedWriter clientOut = new BufferedWriter(new FileWriter(ClientConfigPath));
//        serverOut.write(Integer.toString(serverSize));
//        clientOut.write(Integer.toString(clienSize));
//        serverOut.close();
//        clientOut.close();
//    }
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
//        FileInputStream classFilePath = (FileInputStream) Test.class.getClassLoader().getResourceAsStream("test1.txt");
//        byte[] aaa = new byte[10];
//        int n = classFilePath.read(aaa,0,10);
//        System.out.write(aaa, 0, n);
        InputStream is = Test.class.getResourceAsStream("test1.txt");
        InputStreamReader read1 = new InputStreamReader(is, "utf-8");
        BufferedReader in = new BufferedReader((read1));

//        System.out.println(classFilePath);
////        String testFile = classFilePath+"test1.txt";
//        String testFile = classFilePath;
//        BufferedReader in = new BufferedReader(new FileReader(testFile));
        String[] config_list = (in.readLine()).split(" ");
//        writeConfig(Integer.parseInt(config_list[0]),Integer.parseInt(config_list[1]));
        System.out.println(config_list[0]+" "+config_list[1]);



        URL resourceUrl = Test.class.getResource("serverConfig.txt");
        String path = resourceUrl.getPath();
        Writer writer = new FileWriter(path);
//        File file = new File(resourceUrl.toURI());
//        OutputStream output = new FileOutputStream(file);
//        OutputStreamWriter out = new OutputStreamWriter(output,"utf-8");
        BufferedWriter serverOut = new BufferedWriter(writer);
        serverOut.write(config_list[0]);


        InputStream is2 = Test.class.getResourceAsStream("serverConfig.txt");
        InputStreamReader read2 = new InputStreamReader(is2, "utf-8");
        BufferedReader in2 = new BufferedReader((read2));
        System.out.println(in2.readLine());
    }
}
