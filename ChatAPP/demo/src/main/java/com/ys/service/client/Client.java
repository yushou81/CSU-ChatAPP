package com.ys.service.client;

import java.io.*;
import java.net.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader userInput;

    // 初始化客户端，连接到服务器，并返回布尔值指示连接是否成功
    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            System.out.println("Connected to server: " + serverIp);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;  // 连接成功
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;  // 连接失败
        }
    }

    // 发送消息给服务器，并返回布尔值指示发送是否成功
    public boolean sendMessage(String message) {
        if (out != null) {
            try {
                out.println(message);
                // 确保消息被发送
                if (out.checkError()) {
                    System.err.println("Failed to send message.");
                    return false;
                }
                return true;
            } catch (Exception e) {
                System.err.println("Error while sending message: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    // 接收来自服务器的消息
    public String receiveMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    // 关闭客户端连接
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }
}


