package com.ys.service.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class ClientHandler implements Runnable {  // 使用Runnable接口实现
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from client: " + message);
                MultiClientServerWithThreadPool.broadcastMessage(message, clientSocket);  // 广播消息
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭客户端连接
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
