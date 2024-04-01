package paxos_pack;

//package com.yazhoubay.util.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class SamplePublish {

    public static void main(String[] args) {

        //测试环境
        String broker = "tcp://127.0.0.1:1884";//阿里云
        String topic = "topic/aa/a1";
        String username = "aaa";
        String password = "Su250520s";

        String clientId = MqttClient.generateClientId();

        //地瓜地瓜,我是土豆,我是土豆
        //番茄番茄,我是西红柿,我是西红柿
        //蓝天蓝天,我是白云,我是白云
        //长江长江,我是黄河,我是黄河
        //Hello World MQTT
        String content = "地瓜地瓜,我是土豆,我是土豆111";

        int qos = 0;

        try {
            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            // 连接参数
            MqttConnectOptions options = new MqttConnectOptions();
            // 设置用户名和密码
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setKeepAliveInterval(10);
            options.setConnectionTimeout(10);
            // 连接
            client.connect(options);
            // 创建消息并设置 QoS
//            MqttMessage message = new MqttMessage(content.getBytes());
//            message.setQos(qos);
//            // 发布消息
//            client.publish(topic, message);
//            System.out.println("Message published");
//            System.out.println("topic: " + topic);
//            System.out.println("message content: " + content);
            // 断开连接
//            client.disconnect();
            // 关闭客户端
//            client.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

    }
}
