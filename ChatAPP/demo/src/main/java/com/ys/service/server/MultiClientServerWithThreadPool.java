package com.ys.service.server;

import com.ys.dao.*;

import com.ys.model.MeetingRoom;

import com.ys.model.Message;
import com.ys.model.User;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.ys.service.MeetingService;
import com.ys.service.server.VideoStreamServer;
public class MultiClientServerWithThreadPool {
    // 使用一个线程安全的集合来存储客户端的Socket
    private static Map<String, Socket> userSockets = new ConcurrentHashMap<>();

    // 创建一个固定大小的线程池
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10); // 线程池大小设为10

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);  // 监听8080端口
            System.out.println("Server is listening on port 8080");

            // 启动处理视频流的服务器
            VideoStreamServer videoServer = new VideoStreamServer(5555); // 处理视频流的端口
            videoServer.start();

            MeetingDao meetingDao = new MeetingDao();
            MeetingService meetingService = new MeetingService(meetingDao);

            while (true) {
                // 接受客户端连接
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // 使用线程池处理客户端，并传递UserDao
                threadPool.submit(new ClientHandler(clientSocket, new UserDao(),meetingService));
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
        private TeamDao teamDao;
        private String userId;
        private MeetingService meetingService;

        public ClientHandler(Socket clientSocket, UserDao userDao,MeetingService meetingService) {
            this.clientSocket = clientSocket;
            this.userDao = userDao;
            this.meetingService = meetingService;
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
                // 登录成功后，处理私聊、消息广播和新功能
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("PRIVATE")) {
                        handlePrivateMessage(message);
                    } else if (message.startsWith("FIND_USER")) {
                        handleFindUser(message, out);
                    } else if (message.startsWith("GET_FRIENDS")) {
                        handleGetFriends(out);
                    } else if (message.startsWith("ADD_FRIEND")) {
                        handleAddFriend(message, out);
                    } else if (message.startsWith("GET_MESSAGE_HISTORY")) {
                        handleGetMessageHistory(message, out);
                    } else if (message.startsWith("CREATE_TEAM")){
                      handleCreateTeam(message,out);
                    }else if (message.startsWith("CREATE_MEETING")) {
                        System.out.println("接收到创建会议");
                        handleCreateMeeting(message, out);
                    } else if (message.startsWith("JOIN_MEETING")) {
                        handleJoinMeeting(message, out);
                    } else if (message.startsWith("LEAVE_MEETING")) {
                        handleLeaveMeeting(message, out);
                    }else if (message.startsWith("UPDATE_USER:")) {
                        handleModifyUserInfo(message, out);
                    }
                    else {
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

        // 修改 handlePrivateMessage 函数，加入消息存储功能
        private void handlePrivateMessage(String message) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String targetUserId = parts[1];
                String privateMessage = parts[2];

                // 发送私聊消息
                sendPrivateMessage(targetUserId, "来自用户 " + userId + " 的私聊消息: " + privateMessage);
                // 存储消息到数据库
                MessageDao messageDao = new MessageDao();
                Message msg = new Message();
                msg.setSenderId(Integer.parseInt(userId));
                msg.setReceiverId(Integer.parseInt(targetUserId));
                msg.setMessageContent(privateMessage);
                msg.setMessageType("text");  // 假设这里为文本类型
                messageDao.saveMessage(msg);
            } else {
                System.out.println("私聊消息格式错误！");
            }
        }

        // 处理查找用户
        private void handleFindUser(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 2) {
                int userIdToFind = Integer.parseInt(parts[1]);
                User user = userDao.findUserById(userIdToFind);

                if (user != null) {
                    out.println("用户ID: " + user.getUser_id() + ", 用户名: " + user.getUsername() + ", 邮箱: " + user.getEmail());
                } else {
                    out.println("用户未找到");
                }
            } else {
                out.println("查找用户信息格式错误！");
            }
        }

        // 新增 handleGetMessageHistory 用于获取聊天记录
        private void handleGetMessageHistory(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 2) {
                String targetUserId = parts[1];

                // 获取两人聊天记录
                MessageDao messageDao = new MessageDao();
                List<Message> messages = messageDao.getMessagesBetweenUsers(Integer.parseInt(userId), Integer.parseInt(targetUserId));

                if (messages.isEmpty()) {
                    out.println("没有找到聊天记录");
                } else {
                    for (Message msg : messages) {
                        out.println("时间: " + msg.getSentAt() + " 发送者ID: " + msg.getSenderId() + " 内容: " + msg.getMessageContent());
                        System.out.println("发送消息: " + msg.getMessageContent());  // 日志，确保每条消息被发送
                    }
                }
                out.println("END_OF_MESSAGE_HISTORY");  // 结束符，标识聊天记录发送完毕

                // 强制刷新输出流，确保所有消息被发送
                out.flush();
                System.out.println("输出完成");
            } else {
                out.println("聊天记录请求格式错误！");
            }
        }


        // 处理获取好友列表
        private void handleGetFriends(PrintWriter out) {
            List<User> friends = userDao.getFriends(Integer.parseInt(userId));

            if (friends.isEmpty()) {
                System.out.println("好友列表为空");
                out.println("好友列表为空");
            } else {
                for (User friend : friends) {
                    System.out.println("发送好友ID: " + friend.getUser_id() + ", 好友名: " + friend.getUsername());
                    out.println("好友ID: " + friend.getUser_id() + ", 好友名: " + friend.getUsername());
                }
            }
            System.out.println("END_OF_FRIEND_LIST");
            out.println("END_OF_FRIEND_LIST"); // 结束符，标识好友列表发送完毕
        }

        // 处理添加好友


      
        private void handleCreateTeam(String message,PrintWriter out){
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String teamName=parts[2];
                String userID=parts[1];
                boolean success = teamDao.createTeam(userID,teamName);

                if (success) {
                    out.println("CREATE_GROUP_SUCCESS:"+teamName);
                } else {
                    out.println("FAILURE: 创建群聊失败");
                }
            } else {
                out.println("FAILURE: 创建群聊信息格式错误");
            }
        }
        private void handleJoinTeam(String message,PrintWriter out){
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String teamName=parts[2];
                String userID=parts[1];
                boolean success = teamDao.joinTeam(userID,teamName);

                if (success) {
                    out.println("JOIN_GROUP_SUCCESS:"+teamName);
                } else {
                    out.println("FAILURE: 加入群聊失败");
                }
            } else {
                out.println("FAILURE: 加入群聊信息格式错误");
            }
        }

        // 处理创建会议，服务器生成 meeting_id
        private void handleCreateMeeting(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String meetingName = parts[1];
                String password = parts[2];


                String meetingId = meetingService.createMeeting(meetingName, password, Integer.parseInt(userId));
                if (meetingId != null) {
                    out.println("SUCCESS: 会议 " + meetingName + " 已创建, 会议ID: " + meetingId);
                } else {
                    out.println("FAILURE: 创建会议失败");
                }
            } else {
                out.println("FAILURE: 创建会议信息格式错误");
            }
        }

        // 处理加入会议，验证密码和人数限制
        private void handleJoinMeeting(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 3) {
                String meetingId = parts[1];
                String password = parts[2];

                boolean success = meetingService.joinMeeting(meetingId, userId, password);
                if (success) {
                    out.println("SUCCESS: 已加入会议: " + meetingId);
                } else {
                    out.println("FAILURE: 密码错误或会议已满");
                }
            } else {
                out.println("FAILURE: 加入会议信息格式错误");
            }
        }

        // 处理离开会议
        private void handleLeaveMeeting(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 2) {
                String meetingId = parts[1];

                meetingService.leaveMeeting(meetingId, userId);
                out.println("SUCCESS: 已离开会议 " + meetingId);
            } else {
                out.println("FAILURE: 离开会议信息格式错误");
            }
        }

        // 发送好友请求
        private void handleAddFriend(String message, PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 4) {
                String requesterId = parts[1];
                String friendId = parts[2];
                String requestMessage = parts[3];

                // 使用 FriendsDao 添加好友请求
                FriendsDao friendsDao = new FriendsDao();
                boolean success = friendsDao.addFriend(requesterId, friendId, requestMessage);

                if (success) {
                    out.println("SUCCESS"); // 给请求发送者确认请求已发送
                    // 如果目标用户在线，通知其有新的好友请求
                    if (userSockets.containsKey(friendId)) {
                        PrintWriter friendOut = null;
                        try {
                            friendOut = new PrintWriter(userSockets.get(friendId).getOutputStream(), true);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        friendOut.println("FRIEND_REQUEST:" + requesterId + ":" + requestMessage); // 通知好友
                    }
                } else {
                    out.println("FAILURE: 添加好友请求失败");
                }
            } else {
                out.println("FAILURE: 请求格式错误");
            }
        }
        // 处理好友请求的响应
        private void handleFriendRequestResponse(String message,PrintWriter out) {
            String[] parts = message.split(":");
            if (parts.length == 2) {
                String requesterId = parts[1];

                FriendsDao friendsDao = new FriendsDao();
                if (message.startsWith("ACCEPT_FRIEND:")) {
                    // 接受好友请求
                    friendsDao.addFriendToUserFriends(userId, requesterId);
                    friendsDao.addFriendToUserFriends(requesterId, userId);
                    friendsDao.deleteFriendRequest(requesterId, userId); // 删除好友请求记录
                    out.println("SUCCESS: 好友请求已接受");
                } else if (message.startsWith("REJECT_FRIEND:")) {
                    // 拒绝好友请求
                    friendsDao.deleteFriendRequest(requesterId, userId); // 删除好友请求记录
                    out.println("SUCCESS: 好友请求已拒绝");
                }
            }

        }
        // 处理获取好友列表的请求
        private void handleGetFriends(String userId,PrintWriter out) {
            List<String> friendIds = FriendsDao.getAllFriendsIds(userId);
            List<User> friendsDetails = FriendsDao.getFriendDetails(friendIds);

            StringBuilder friendListBuilder = new StringBuilder("FRIEND_LIST:");
            for (User friend : friendsDetails) {
                friendListBuilder.append(friend.getUsername())
                        .append(",")
                        .append(friend.getUser_id())
                        .append(",")
                        .append(friend.getEmail())
                        .append(";");
            }

            // 向客户端发送好友列表
            out.println(friendListBuilder.toString());
        }
        private void handleModifyUserInfo(String message, PrintWriter out){


            String[] parts = message.split(":");
            if(parts.length==4){
                int userid= Integer.parseInt(parts[1]);
                String newusername = parts[2];
                String newPassword = parts[3];

                userDao.updateUsernameAndPassword(userid,newusername,newPassword);
            }



        }


            String[] parts = message.split(":");
            if(parts.length==4){
                int userid= Integer.parseInt(parts[1]);
                String newusername = parts[2];
                String newPassword = parts[3];
                
                userDao.updateUsernameAndPassword(userid,newusername,newPassword);
            }
            
            
            
        }

    }
}

