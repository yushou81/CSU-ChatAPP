package com.ys.service.server;

import com.ys.dao.UserDao;
import com.ys.model.User;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class MultiClientServerWithThreadPool {
    // 使用一个线程安全的集合来存储客户端的Socket
    private static Map<String, Socket> userSockets = new ConcurrentHashMap<>();

    // 创建一个固定大小的线程池
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10); // 线程池大小设为10

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);  // 监听8080端口
            System.out.println("Server is listening on port 8080");

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // 使用线程池处理客户端，并传递UserDao
                threadPool.submit(new ClientHandler(clientSocket, new UserDao()));
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
        for (Map.Entry<String, Socket> entry : userSockets.entrySet()) {
            Socket socket = entry.getValue();
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

    // 发送私聊消息
    public static void sendPrivateMessage(String targetUserId, String message) {
        Socket targetSocket = userSockets.get(targetUserId);
        if (targetSocket != null) {
            try {
                PrintWriter out = new PrintWriter(targetSocket.getOutputStream(), true);
                out.println("私聊消息: " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("用户 " + targetUserId + " 不在线。");
        }
    }

    // 处理客户端的线程
    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private UserDao userDao;
        private String userId;

        public ClientHandler(Socket clientSocket, UserDao userDao) {
            this.clientSocket = clientSocket;
            this.userDao = userDao;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                boolean isLoggedIn = false;

                // 处理注册和登录，直到用户成功登录
                while (!isLoggedIn) {
                    String message = in.readLine();
                    System.out.println("收到消息: " + message);
                    if (message == null) {
                        break;  // 如果收到null，表示客户端断开连接
                    }

                    if (message.startsWith("REGISTER")) {
                        handleRegister(message, out);
                    } else if (message.startsWith("LOGIN")) {
                        isLoggedIn = handleLogin(message, out);
                    }

                }

                String message;
                // 登录成功后，处理私聊和消息广播
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("PRIVATE")) {
                        handlePrivateMessage(message);
                    } else {
                        if (userId != null) {
                            broadcastMessage("用户 " + userId + " 说: " + message, clientSocket);
                        } else {
                            out.println("请先登录！");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (userId != null) {
                    userSockets.remove(userId);
                    System.out.println("用户 " + userId + " 已断开连接");
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 处理注册
        private void handleRegister(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 4) {
                String username = parts[1];
                String password = parts[2];
                String email = parts[3];

                // 创建新用户对象
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setEmail(email);

                // 调用UserDao注册用户
                if (userDao.registerUser(newUser)) {
                    // 调用UserDao验证用户登录
                    User user = userDao.loginUser(username, password);
                    if (user != null) {
                        this.userId = String.valueOf(user.getUser_id());
                        userSockets.put(this.userId, clientSocket);  // 将用户添加到在线用户列表中
                        out.println("SUCCESS:" + user.getUser_id());  // 登录成功，返回userId
                    } else {
                        out.println("FAILURE: 用户名或密码错误");
                    }
                } else {
                    out.println("FAILURE: 用户名已存在或注册失败");
                }
            } else {
                out.println("FAILURE: 注册信息格式错误");
            }
        }

        // 处理登录
        private boolean handleLogin(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String username = parts[1];
                String password = parts[2];

                // 调用UserDao验证用户登录
                User user = userDao.loginUser(username, password);
                if (user != null) {
                    this.userId = String.valueOf(user.getUser_id());
                    userSockets.put(this.userId, clientSocket);  // 将用户添加到在线用户列表中
                    out.println("SUCCESS:" + user.getUser_id());  // 登录成功，返回userId

                    //要添加发送用户的好友列表


                    return true;
                } else {
                    out.println("FAILURE: 用户名或密码错误");
                    return false;
                }
            } else {
                out.println("FAILURE: 登录信息格式错误");
                return false;
            }
        }

        // 处理私聊消息
        private void handlePrivateMessage(String message) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String targetUserId = parts[1];
                String privateMessage = parts[2];

                // 发送私聊消息
                sendPrivateMessage(targetUserId, "来自用户 " + userId + " 的私聊消息: " + privateMessage);
            } else {
                System.out.println("私聊消息格式错误！");
            }
        }
    }
}

