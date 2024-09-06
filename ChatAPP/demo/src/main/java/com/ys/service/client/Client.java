package com.ys.service.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader userInput;
    private String userId;

    // 初始化客户端，连接到服务器，并返回布尔值指示连接是否成功
    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            System.out.println("Connected to server: " + serverIp);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            userInput = new BufferedReader(new InputStreamReader(System.in));  // 用于读取用户输入
            return true;  // 连接成功
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;  // 连接失败
        }
    }

    //注册操作
    public boolean register(String username, String password, String email) {
        sendMessage("REGISTER:" + username + ":" + password + ":" + email);  // 发送注册信息
        return handleLoginOrRegisterResponse();
    }

    // 登录操作
    public boolean login(String username, String password) {
        if(sendMessage("LOGIN:" + username + ":" + password)){
            System.out.println("成功发送");
        }else {
            System.out.println("发送失败");
        }  // 发送登录信息

        return handleLoginOrRegisterResponse();
    }

    // 处理服务器返回的登录或注册响应
    private boolean handleLoginOrRegisterResponse() {
        try {
            String response = in.readLine();  // 读取服务器的响应
            if (response.startsWith("SUCCESS:")) {
                this.userId = response.split(":")[1];
                System.out.println("登录成功，您的用户ID是: " + userId);
                return true;
            } else if (response.startsWith("FAILURE")) {
                System.out.println("登录/注册失败: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error while handling server response: " + e.getMessage());
        }
        return false;
    }

    // 查找用户
    public void findUserById(int userIdToFind) {
        sendMessage("FIND_USER:" + userIdToFind);  // 发送查找用户的请求
        try {
            String response = in.readLine();  // 读取服务器的响应
            System.out.println("查找到的用户信息: " + response);
        } catch (IOException e) {
            System.err.println("Error while receiving user info: " + e.getMessage());
        }
    }

    // 获取好友列表
//    public String[] getFriendList() {
//        sendMessage("GET_FRIENDS:" + this.userId);  // 发送获取好友列表的请求
//        List<String> friendList = new ArrayList<>();
//        try {
//            String response;
//            System.out.println("好友列表:");
//            while (!(response = in.readLine()).equals("END_OF_FRIEND_LIST")) {
//                // response是 "好友ID: 1, 好友名: 1234"
//                String[] parts = response.split(", 好友名: ");  // 通过 ", 好友名: " 分割
//                if (parts.length == 2) {
//                    String friendId = parts[0].replace("好友ID: ", "").trim();  // 提取好友ID
//                    String friendName = parts[1].trim();  // 提取好友名
//                    // 将"好友ID: username"格式组合并加入列表
//                    friendList.add(friendId + ": " + friendName);
//                    System.out.println(friendId + ": " + friendName);  // 输出好友ID和用户名
//                }
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error while receiving friend list: " + e.getMessage());
//        }
//        return friendList.toArray(new String[0]);
//    }
    public Map<String, String> getFriendList() {
        sendMessage("GET_FRIENDS:" + this.userId);  // 发送获取好友列表的请求
        Map<String, String> friendMap = new HashMap<>();  // 用于存储好友的ID和名字
        try {
            String response;
            System.out.println("好友列表:");
            while (!(response = in.readLine()).equals("END_OF_FRIEND_LIST")) {
                // 假设response是 "好友ID: 1, 好友名: Alice"
                String[] parts = response.split(", 好友名: ");
                if (parts.length == 2) {
                    String friendId = parts[0].replace("好友ID: ", "").trim();  // 获取好友ID
                    String friendName = parts[1].trim();  // 获取好友名
                    friendMap.put(friendName, friendId);  // 将好友ID和名字加入Map
                    System.out.println("好友ID: " + friendId + ", 好友名: " + friendName);  // 输出好友ID和用户名
                }
            }
        } catch (IOException e) {
            System.err.println("Error while receiving friend list: " + e.getMessage());
        }

        return friendMap;  // 返回包含好友ID和用户名的Map
    }

    // 添加好友
    public boolean addFriend(int friendId) {
        sendMessage("ADD_FRIEND:" + this.userId + ":" + friendId);  // 发送添加好友请求
        try {
            String response = in.readLine();  // 读取服务器的响应
            if (response.equals("SUCCESS")) {
                System.out.println("好友添加成功!");
                return true;
            } else {
                System.out.println("添加好友失败: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error while adding friend: " + e.getMessage());
        }
        return false;
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



