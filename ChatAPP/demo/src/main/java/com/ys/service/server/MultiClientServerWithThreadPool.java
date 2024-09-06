package com.ys.service.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiClientServerWithThreadPool {
    // 使用一个线程安全的集合来存储客户端的Socket
    private static List<Socket> clientSockets = Collections.synchronizedList(new ArrayList<>());

    // 创建一个固定大小的线程池
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10); // 线程池大小设为10

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);  // 监听8080端口
            System.out.println("Server is listening on port 8080");

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("New client connected");

                // 使用线程池处理客户端
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 当服务器关闭时，停止线程池
            threadPool.shutdown();
        }
    }

    // 广播消息给所有客户端
    public static void broadcastMessage(String message, Socket senderSocket) {
        synchronized (clientSockets) {
            for (Socket socket : clientSockets) {
                if (socket != senderSocket) {
                    try {
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}