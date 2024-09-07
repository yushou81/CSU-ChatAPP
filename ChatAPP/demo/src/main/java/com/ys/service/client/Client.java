package com.ys.service.client;

import com.mysql.cj.protocol.MessageListener;

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
    private MessageListener messageListener;

    public interface MessageListener {

        //收到新消息时调用
        void onMessageReceived(String message);
        //收到历史信息记录时调用
        void onHistoryReceived(List<String> history);
        //收到好友列表时调用
        void onFriendListReceived(Map<String, String> friendList);  // 添加好友列表的回调
    }

    // 注册监听器，用于在外部处理消息接收
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // 初始化客户端，连接到服务器，并返回布尔值指示连接是否成功
    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            System.out.println("Connected to server: " + serverIp+"from src/main/java/com/ys/service/client/Client.java");
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return true;  // 连接成功
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage()+"from src/main/java/com/ys/service/client/Client.java");
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
            System.out.println("成功发送登录");
        }else {
            System.out.println("登录发送失败");
        }
        return handleLoginOrRegisterResponse();
    }

    // 处理服务器返回的登录或注册响应
    private boolean handleLoginOrRegisterResponse() {
        try {
            String response = in.readLine();  // 读取服务器的响应
            //debug信息
            System.out.println(response);
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

    //获取好友列表
    public void getFriendList() {
        sendMessage("GET_FRIENDS:" + this.userId);  // 发送获取好友列表的请求
    }

    // 发送消息给服务器，并返回布尔值指示发送是否成功
    public boolean sendMessage(String message) {
        if (out != null) {
            try {
                out.println(message);
                out.flush(); // 强制刷新
                if (out.checkError()) {
                    System.err.println("client信息发送失败");
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

    public List<String> getMessageHistory(int targetUserId) {
        sendMessage("GET_MESSAGE_HISTORY:" + targetUserId);  // 发送获取聊天记录请求
        List<String> messageHistory = new ArrayList<>();

        try {
            String response;
            System.out.println("开始获取聊天记录:");

            // 持续读取消息，直到收到 "END_OF_MESSAGE_HISTORY"
            while ((response = in.readLine()) != null) {
                System.out.println("收到聊天记录: " + response);  // 输出每条消息记录
                if (response.equals("END_OF_MESSAGE_HISTORY")) {
                    System.out.println("聊天记录获取完毕");
                    break;  // 当接收到结束标识时退出循环
                }
                messageHistory.add(response);  // 将消息添加到列表中
            }
        } catch (IOException e) {
            System.err.println("Error while receiving message history: " + e.getMessage());
        }

        return messageHistory;  // 返回消息历史列表
    }

    // 接收来自服务器的消息
    public String receiveMessage() throws IOException {
        if (in != null) {
            return in.readLine();
        }
        return null;
    }

    // 处理历史记录请求
    public void requestMessageHistory(int targetUserId) {
        sendMessage("GET_MESSAGE_HISTORY:" + targetUserId);
    }


//    // 启动接收线程，用于实时接收消息和历史消息
//    public void startReceiveMessages() {
//        new Thread(() -> {
//            try {
//                String message;
//                List<String> history = new ArrayList<>();
//                Map<String, String> friendList = new HashMap<>();
//                boolean receivingHistory = false;
//                boolean receivingFriends = false;
//
//                while ((message = in.readLine()) != null) {
//                    if (message.equals("END_OF_MESSAGE_HISTORY")) {
//                        if (messageListener != null) {
//                            messageListener.onHistoryReceived(history);
//                        }
//                        history.clear();
//                        receivingHistory = false;
//                    } else if (message.equals("END_OF_FRIEND_LIST")) {
//                        if (messageListener != null) {
//                            messageListener.onFriendListReceived(friendList);
//                        }
//                        friendList.clear();
//                        receivingFriends = false;
//                    } else if (message.startsWith("时间:")) {
//                        receivingHistory = true;
//                        history.add(message);
//                    } else if (message.startsWith("好友ID:")) {
//                        receivingFriends = true;
//                        String[] parts = message.split(", 好友名: ");
//                        if (parts.length == 2) {
//                            String friendId = parts[0].replace("好友ID: ", "").trim();
//                            String friendName = parts[1].trim();
//                            friendList.put(friendName, friendId);
//                        }
//                    } else if (message.startsWith("私聊消息: 来自用户")) {
//                        if (messageListener != null) {
//                            messageListener.onMessageReceived(message);
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
public void startReceiveMessages() {
    new Thread(() -> {
        try {
            String message;
            List<String> history = new ArrayList<>();
            Map<String, String> friendList = new HashMap<>();
            boolean receivingHistory = false;
            boolean receivingFriends = false;

            while ((message = in.readLine()) != null) {
                if (message.equals("END_OF_MESSAGE_HISTORY")) {
                    if (messageListener != null) {
                        messageListener.onHistoryReceived(history);
                    }
                    history.clear();
                    receivingHistory = false;
                } else if (message.equals("END_OF_FRIEND_LIST")) {
                    if (messageListener != null) {
                        System.out.println("好友列表接收完毕");
                        messageListener.onFriendListReceived(friendList);
                    }
                    friendList.clear();
                    receivingFriends = false;
                } else if (message.startsWith("时间:")) {
                    receivingHistory = true;
                    history.add(message);
                } else if (message.startsWith("好友ID:")) {
                    System.out.println("接收到"+message);
                    String[] parts = message.split(", 好友名: ");
                    if (parts.length == 2) {
                        String friendId = parts[0].replace("好友ID: ", "").trim();
                        String friendName = parts[1].trim();
                        friendList.put(friendName, friendId);

                    }
                } else if (message.startsWith("私聊消息: 来自用户")) {
                    System.out.println("debug :"+message);
                    if (messageListener != null) {
                        messageListener.onMessageReceived(message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }).start();
}
    //关闭客户端连接
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}



