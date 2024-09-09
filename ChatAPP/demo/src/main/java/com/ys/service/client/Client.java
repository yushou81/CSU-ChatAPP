package com.ys.service.client;

import com.mysql.cj.protocol.MessageListener;
import com.ys.controller.CreateTeamController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ys.dao.UserDao;
import javafx.application.Platform;


import com.ys.controller.AddfriendsController;
import com.ys.controller.SettingController;
import javafx.scene.control.Alert;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader userInput;

    public String getUserId() {
        return userId;
    }

    private String userId;

    private String serverIp;
    private MessageListener messageListener;
    private VideoStreamClient videoStreamClient;
    private SettingController settingController;
    private AddfriendsController addfriendsController;
    private FileClient fileClient;

    public Client() {
        this.videoStreamClient = VideoStreamClientManager.getClient();  // 创建视频流客户端实例
    }

    public interface MessageListener {

        //收到新消息时调用
        void onMessageReceived(String message);
        //收到历史信息记录时调用
        void onHistoryReceived(List<String> history);
        //收到好友列表时调用
        void onFriendListReceived(Map<String, String> friendList);  // 添加好友列表的回调

        void onCreateGroup(String teamName,boolean success);  // 新增的方法
        void onTeamListReceived(Map<String,String> groupList);




    }

    // 注册监听器，用于在外部处理消息接收
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    // 初始化客户端，连接到服务器，并返回布尔值指示连接是否成功
    public boolean connect(String serverIp, int serverPort) {
        try {
            this.serverIp=serverIp;
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

    public void sendCreateTeamRequest(String userId,String teamName) {
        // 发送创建团队请求到服务器
        if (sendMessage("CREATE_TEAM:"+userId +":"+ teamName)) {
            System.out.println("客户端发送"+"CREATE_TEAM:"+userId +":"+ teamName);
        } else {
            System.out.println("创建团队请求发送失败");
        }

        // 处理服务器的响应
        return ;
    }
    // 发送加入团队请求
    public boolean sendJoinTeamRequest(String userId, String teamName) {
        // 发送加入团队请求到服务器
        if (sendMessage("JOIN_TEAM:"+userId +":"+ teamName)) {
            System.out.println("客户端发送"+"JOIN_TEAM:"+userId +":"+ teamName);
        } else {
            System.out.println("加入团队请求发送失败");
        }

        // 处理服务器的响应
        return handleJoinTeamResponse();
    }






    // 处理服务器返回的加入团队响应
    private boolean handleJoinTeamResponse() {
        try {
            String response = in.readLine();  // 读取服务器的响应
            //debug信息
            System.out.println(response);
            if (response.startsWith("JOIN_GROUP_SUCCESS:")) {
                System.out.println("加入团队成功: " + response.split(":")[1]);
                return true;
            } else if (response.startsWith("JOIN_GROUP_FAILURE")) {
                System.out.println("加入团队失败: " + response);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error while handling server response: " + e.getMessage());
        }
        return false;
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


    public void setSettingController(SettingController settingController){
        this.settingController=settingController;
    }
    public void setAddFriendController(AddfriendsController addfriendsController){
        this.addfriendsController=addfriendsController;
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
                System.out.println("发送给服务器"+message);
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
    public void requestTeamMessageHistory(int targetTeamId){sendMessage("GET_TEAM_MESSAGE_HISTORY:" + targetTeamId);}
    // 创建会议，服务器返回 meeting_id
    public void createMeeting(String meetingName, String password) {
        sendMessage("CREATE_MEETING:" + meetingName + ":" + password);
    }


    // 加入会议
    public void joinMeeting(String meetingId, String password) {
        sendMessage("JOIN_MEETING:" + meetingId + ":" + password);
    }

    // 离开会议
    public void leaveMeeting(String meetingId) {
        sendMessage("LEAVE_MEETING:" + meetingId);
    }

    //    开始接收
    public void startReceiveMessages() {
        new Thread(() -> {
            try {
                String message;
                List<String> history = new ArrayList<>();
                Map<String, String> friendList = new HashMap<>();
                Map<String,String>teamList=new HashMap<>();
                while ((message = in.readLine()) != null) {

                    System.out.println(message);


                    if (message.equals("END_OF_MESSAGE_HISTORY")) {
                        if (messageListener != null) {
                            messageListener.onHistoryReceived(history);
                        }
                        history.clear();
                    } else if (message.equals("END_OF_FRIEND_LIST")) {
                        if (messageListener != null) {
                            messageListener.onFriendListReceived(friendList);
                            System.out.println("好友列表接收完毕");
                        }
                        friendList.clear();
                    } else if (message.equals("END_OF_TEAM_LIST")) {
                        if(messageListener!=null){
                            messageListener.onTeamListReceived(teamList);
                            System.out.println("群聊列表接收完毕");
                        }
                        teamList.clear();
                    } else if (message.startsWith("时间:")) {
                        history.add(message);
                    } else if (message.startsWith("好友ID:")) {
                        String[] parts = message.split(", 好友名: ");
                        if (parts.length == 2) {
                            String friendId = parts[0].replace("好友ID: ", "").trim();
                            String friendName = parts[1].trim();
                            friendList.put(friendName, friendId);

                        }
                    }else if (message.startsWith("团队ID:")) {
                        String[] parts = message.split(", 群聊名: ");
                        if (parts.length == 2) {
                            String teamId = parts[0].replace("团队ID: ", "").trim();
                            String teamName = parts[1].trim();
                            System.out.println("获取到的团队id"+teamId+"团队名称"+teamName);
                            teamList.put(teamName, teamId);


                        }
                    } else if (message.startsWith("私聊消息: 来自用户")) {
                        if (messageListener != null) {
                            messageListener.onMessageReceived(message);
                        }


                    }
                    else if (message.startsWith("CREATE_GROUP_SUCCESS:")) {
                        String[] parts = message.split(":",2);
                        if(parts.length==2){
                        String teamName=parts[1].trim();
                        //这里写加入群聊的函数
                        this.sendJoinTeamRequest(this.getUserId(),teamName);
                            if (messageListener != null) {
                                messageListener.onCreateGroup(teamName,true);  // 调用监听器回调方法
                            }
                        }
                        else{
                            System.err.println("CREATE_GROUP_SUCCESS 消息格式不正确: " + message);

                        }
                    } else if (message.startsWith("FAILURE:")) {
                        String[] parts = message.split(":",2);


                        if(parts.length==2){
                            String wrongMessage=parts[1].trim();
                        if(messageListener!=null){
                            messageListener.onCreateGroup(wrongMessage,false);
                        }
                    }
                    } else if(message.startsWith("JOIN_GROUP_SUCCESS:")){
                      //  这里在界面更新消息和群聊，服务器返回的信息是加入群聊成功服务器out.println("JOIN_GROUP_SUCCESS:"+teamName);
                        String[] parts = message.split("CREATE_GROUP_SUCCESS:");
                        String teamName = parts[1].trim();
                        // 这里写加入群聊的函数
                      //这行不知道要不要 
                      // this.sendJoinTeamRequest(this.getUserId(), teamName);
                    }

                     else if (message.startsWith("FRIEND_LIST:")) {
                        // 处理服务器返回的好友列表
                        String friendsData = message.substring("FRIEND_LIST:".length()).trim();
                        String[] friends = friendsData.split(";");
                        for (String friend : friends) {
                            if (!friend.trim().isEmpty()) {
                                String[] friendInfo = friend.split(",");
                                if (friendInfo.length == 2) {
                                    String friendId = friendInfo[0].trim();
                                    String friendName = friendInfo[1].trim();
                                    friendList.put(friendName, friendId);
                                }
                            }
                        }
                        // 好友列表接收完毕，调用回调函数通知前端UI更新
                        if (messageListener != null) {
                            messageListener.onFriendListReceived(friendList);
                        }
                    } 

  
                         
                     else if (message.startsWith("SUCCESS: 会议 ")) {
                        String meetingId = message.split(":")[2].trim();
                        System.out.println("会议创建成功，会议号为: " + meetingId);
                        videoStreamClient.startVideoStream(meetingId, serverIp, 5555);
                    } else if (message.startsWith("SUCCESS: 已加入会议: ")) {
                        String meetingId = message.split(":")[2].trim();
                        System.out.println("会议加入成功，会议号为: " + meetingId);
                        // 连接视频流服务器并开始传输视频
                        videoStreamClient.joinMeeting(meetingId, serverIp, 5555);  // 视频流端口是 5555
                    } else if (message.startsWith("SUCCESS: 用户信息修改成功: ")) {
                        settingController.fail();
                    }else if (message.startsWith("Failure: 用户信息修改失败: ")) {
                        settingController.success();
                        System.out.println("用户信息修改成功");
                    }else if (message.startsWith("SUCCESS搜索到：")) {
                        handleSearchFriendResponse(message);
                        System.out.println("搜索用户成功");
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
    public boolean searchFriend(String friendId) {
        // 发送搜索好友请求到服务器
        UserDao userDao = new UserDao();
        String userId = friendId;  // 你想要检查的 userId


        if (userDao.isUserExists(userId)) {
            System.out.println("客户端发送搜索好友请求: " + friendId);
            return true;  // 搜索请求发送成功
        } else {
            System.out.println("搜索好友请求发送失败");
            return false;  // 搜索请求发送失败
        }
    }
    private boolean handleSearchFriendResponse(String response) {
        System.out.println(response);
        String [] parts = response.split(":");
        String userName = parts[1];
        System.out.println("好友存在: " + userName);  // 输出好友ID

        // 弹出成功的Aler

        addfriendsController.success("好友存在: " + userName);
        return true;  // 好友存在
    }


    // 发送好友请求
    public boolean sendFriendRequest(String friendId, String message) {
        if (sendMessage("ADD_FRIEND:" + this.userId + ":" + friendId + ":" + message)) {
            System.out.println("发送好友请求: " + friendId + " 消息: " + message);

            // 弹出成功的Alert
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("请求已发送");
                alert.setHeaderText(null);
                alert.setContentText("好友请求已发送给client: " + friendId);
                alert.showAndWait();
            });

            return true;  // 请求发送成功
        } else {
            System.out.println("好友请求发送失败");
            return false;  // 请求发送失败
        }
    }

    // 发送同意好友请求到服务器
    public boolean acceptFriendRequest(String requesterId, String currentUserId) {
        return sendMessage("ACCEPT_FRIEND:" + requesterId + ":" + currentUserId);
    }


    // 发送拒绝好友请求到服务器
    public boolean rejectFriendRequest(String requesterId,String currentUserId) {
        return sendMessage("REJECT_FRIEND:" + requesterId + currentUserId);
    }
    // 发送请求从服务器获取好友列表
    public boolean requestFriendList() {
        return sendMessage("GET_FRIENDS:" + this.userId);
    }




}



