package com.ys.service.client;

import java.io.*;
import java.net.*;

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


//     主程序，演示客户端交互
    public static void main(String[] args) {
        Client client = new Client();
        if (client.connect("127.0.0.1", 8080)) {  // 连接服务器
            try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.println("请选择操作: 1. 注册 2. 登录");
                String choice = consoleInput.readLine();

                if ("1".equals(choice)) {
                    // 注册
                    System.out.print("请输入用户名: ");
                    String username = consoleInput.readLine();
                    System.out.print("请输入密码: ");
                    String password = consoleInput.readLine();
                    System.out.print("请输入邮箱: ");
                    String email = consoleInput.readLine();
                    if (client.register(username, password, email)) {
                        System.out.println("注册成功，您可以开始发送消息了！");
                    }
                } else if ("2".equals(choice)) {
                    // 登录
                    System.out.print("请输入用户名: ");
                    String username = consoleInput.readLine();
                    System.out.print("请输入密码: ");
                    String password = consoleInput.readLine();
                    if (client.login(username, password)) {
                        System.out.println("登录成功，您可以开始发送消息了！");
                    }
                } else {
                    System.out.println("无效选项");
                }

                // 进行消息发送和接收
                while (true) {
                    System.out.print("输入消息 (或输入 'PRIVATE:targetUserId:message' 进行私聊): ");
                    String input = consoleInput.readLine();
                    if ("quit".equalsIgnoreCase(input)) {
                        client.close();
                        break;
                    }
                    client.sendMessage(input);

                    // 接收服务器的响应
                    String response = client.receiveMessage();
                    if (response != null) {
                        System.out.println("服务器消息: " + response);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



