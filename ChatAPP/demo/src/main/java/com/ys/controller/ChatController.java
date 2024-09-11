package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;

import com.ys.service.client.FileClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.*;

import javafx.scene.input.MouseEvent;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatController {
    public ListView friendListView;
    //需要在点击contractList中内容时切换chatPane内容和nameTitle内容
    @FXML
    private StackPane nameTitle;//顶部名称和头像区域
    @FXML
    private ListView<String> messageListView; // 右侧消息列表
    @FXML
    private TextArea inputArea;               // 输入框
    @FXML
    private Button sendButton;                // 发送按钮
    @FXML
    private Separator separator;

    @FXML
    AnchorPane chatPane;//整个聊天功能区域


    private Client client;
    private String currentFriend;  // 当前聊天的好友和团队
    private String currentFriendID;
    Map<String, String> friendMap;
    Map<String, String> updatedFriendMap;

    private Map<String, ObservableList<String>> chatMessages = new HashMap<>();  // 用于存储每个好友的聊天记录
    public ChatController() throws Exception {
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
    }

    public void initialize() {
        // 注册消息监听器
        client.setMessageListener(new Client.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                String messageCopy = new String(message);
                System.out.println(message);
                // 实时消息处理
//                团队: 时间:TEAM:团队:2:qw
                Platform.runLater(() -> {
                    if (messageCopy.startsWith("团队消息:")){
                        String[] parts = messageCopy.split(":");
                            String teamId = parts[1];
                            String teamName = "团队: "+parts[2];
                            String teamMessage = parts[3];
                        System.out.println("客户端你团队"+teamMessage);
                                receiveMessage(teamName, teamMessage);
                    }else{
                        String[] parts = messageCopy.split(" 的私聊消息: ");
                        if (parts.length == 2) {
                            String [] partss = parts[0].split("来自用户 ");
                            if(partss.length==2){
                                String senderId = partss[1].trim();
                                String privateMessage = parts[1].trim();
                                String senderName = getFriendNameById(senderId);
                                System.out.println("84hang"+senderName);
                                receiveMessage(senderName, privateMessage);
                            }
                        }
                    }
                });
            }

            @Override
            public void onHistoryReceived(List<String> history) {

                System.out.println("接收历史消息完毕");

                List<String> historyCopy = new ArrayList<>();
                // 遍历每一个消息，检查消息类型
                for (String message : history) {
                    System.out.println("收到了吗"+message);
                    // 检查消息类型是否为 "text"
                    if (message.contains("消息类型: text")) {
                        // 找到 "消息类型: text" 在字符串中的位置
                        int index = message.indexOf("消息类型: text");

                        // 删除从 "消息类型: text" 开始之后的所有内容
                        String modifiedMessage = message.substring(0, index).trim();
                        historyCopy.add(modifiedMessage);  // 保存修改后的消息
                    } else {
                        // 如果不是 text 消息，直接保存
                        historyCopy.add(message);
                    }
                }
                // 历史消息处理
                Platform.runLater(() -> {

//                    System.out.println(historyCopy);


                    System.out.println("debug historyCopy"+historyCopy);

                    if (currentFriend != null) {
                        System.out.println("开始设置历史消息");
                        chatMessages.get(currentFriend).clear();
                        chatMessages.get(currentFriend).addAll(historyCopy);
                        showMessagesForFriend(currentFriend);
                    }

                });
            }

            @Override
            public void onCreateGroup(String teamName,boolean success){
            }

            @Override
            public void onFriendListReceived(Map<String, String> friendList) {
                //在另一个线程进入到platform时，friendlist会被清空不知道为什么
                Map<String, String> friendListCopy = new HashMap<>(friendList);
                Platform.runLater(() -> {
                    loadFriendList(friendListCopy);
                });
            }
            @Override
            public void onTeamListReceived(Map<String,String> teamList){
                Map<String,String> teamListCopy=new HashMap<>();
                // 给每个团队名称加上 "团队:" 前缀
                for (Map.Entry<String, String> entry : teamList.entrySet()) {
                    String teamId = entry.getKey();
                    String teamName = entry.getValue();

                    teamListCopy.put(teamId, "团队:" + teamName);  // 加前缀
                }
                Platform.runLater(() -> {
                    //正常
                    loadTeamList(teamListCopy);
                });
            }


        });

        // 异步获取好友列表
        Task<Void> loadFriendsTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                client.getFriendList();
                startReceivingMessages();  // 开始接收实时消息
                return null;
            }
        };

        // 执行任务
        new Thread(loadFriendsTask).start();
    }

    private void startReceivingMessages() {
        client.startReceiveMessages();  // 启动接收实时消息的线程
    }

    // 将Map中的键提取为String数组
    public static String[] getUserNames(Map<String, String> map) {
        Set<String> keys = map.keySet();  // 获取所有的键 (user_id)
        return keys.toArray(new String[0]);  // 将Set转换为String数组
    }

    // 加载好友列表
    private void loadFriendList(Map<String, String> friendMap) {
        this.friendMap = new HashMap<>(friendMap);
        String[] userNames = getUserNames(friendMap);

        ObservableList<String> friends = FXCollections.observableArrayList(userNames);

        System.out.println("客户端加载好友列表");

        friendListView.setItems(friends);
        // 初始化每个好友的聊天记录
        for (String friend : friends) {
            chatMessages.put(friend, FXCollections.observableArrayList());
        }
    }
    private void loadTeamList(Map<String, String> teamMap) {
        updatedFriendMap = new HashMap<>(friendMap);

        for (Map.Entry<String, String> entry : teamMap.entrySet()) {
            String teamNameWithPrefix = "团队: " + entry.getKey();
            updatedFriendMap.put(teamNameWithPrefix, entry.getValue());
            System.out.println(teamNameWithPrefix);
        }

        // 创建团队名称列表并添加前缀
        ObservableList<String> currentItems = friendListView.getItems();
        ObservableList<String> teams = FXCollections.observableArrayList();
        for (String teamName : getUserNames(teamMap)) {
            teams.add("团队: " + teamName);
        }

        // 更新 ListView 内容，保留现有好友列表并添加团队
        currentItems.addAll(teams);

        System.out.println("客户端加载团队列表");

        // 初始化每个团队的聊天记录
        for (String team : teams) {
            System.out.println("241hang"+team);
            chatMessages.put(team, FXCollections.observableArrayList());
        }

        // 点击事件处理，并在选择团队时也处理点击事件
        friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentFriend = (String) newValue;
                System.out.println("debug203: " + currentFriend);
                currentFriendID = updatedFriendMap.get(currentFriend);

                if (currentFriend.startsWith("团队: ")) {

                    //这个参数为3或者4试试
                    String bufferCurrentFriend=currentFriend.substring(4);

                    //处理团队信息
                    client.requestTeamMessageHistory((bufferCurrentFriend));
                }

                else {

                // 确保 currentFriendID 不为 null
                if (currentFriendID != null) {
                    try {
                            client.requestMessageHistory(Integer.parseInt(currentFriendID));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid ID format for selected item: " + currentFriendID);
                        e.printStackTrace();
                    }
                }
            }}
        });
    }


    // 发送私人消息
    @FXML
    private void sendMessage() {
        String message = inputArea.getText();
        if (!message.isEmpty() && currentFriend != null) {
                if (currentFriend.startsWith("团队: ")) {
                    inputArea.clear();
                    String bufferCurrentFriend=(currentFriend.substring(4));
                    System.out.println("发送群聊" +message);
                    client.sendMessage("TEAM:"+currentFriendID+":"+currentFriend+":"+message+":消息类型"+"Text");
                }
                else {
//                    if(){}
                    System.out.println(message);
                    chatMessages.get(currentFriend).add("我: " + message);
                    inputArea.clear();
                    System.out.println("发送私聊" + message);
                    client.sendMessage("PRIVATE:" + currentFriendID + ":" + message);
                }
        }
    }

    // 接收消息并显示，在on里面调用
    private void receiveMessage(String fromFriend, String message) {
        System.out.println(fromFriend);
        String [] parts = message.split("消息类型");
        chatMessages.get(fromFriend).add(fromFriend + ": " + parts[0]);
        if (fromFriend.equals(currentFriend)) {
            showMessagesForFriend(currentFriend);
        }
    }

    // 在初始化时，绑定点击事件给消息列表
    private void showMessagesForFriend(String friend) {
        System.out.println("314showmessage"+friend);
        ObservableList<String> messages = chatMessages.get(friend);
        messageListView.setItems(messages);
        // 为每条消息绑定点击事件
        messageListView.setOnMouseClicked(event -> {
            String selectedMessage = messageListView.getSelectionModel().getSelectedItem();
            System.out.println("点击消息"+selectedMessage);
            if (selectedMessage != null) {
                handleMessageClick(selectedMessage);
            }
        });
    }

    private String getFriendNameById(String userId) {
        // 遍历 friendMap 的每个条目
        for (Map.Entry<String, String> entry : updatedFriendMap.entrySet()) {
            // 如果找到对应的 userId，返回对应的 username
            if (entry.getValue().equals(userId)) {
                return entry.getKey();
            }
        }
        // 如果没有找到，返回 null 或者你想要的默认值
        return null;
    }

    //发送文件，已经绑定文件按钮fx:id fileButton
    @FXML
    private void sendFile(ActionEvent actionEvent) {
        // 创建文件选择器
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File to Send");

        // 获取当前窗口上下文
        java.io.File selectedFile = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());

        if (selectedFile != null) {
            // 假设有 ClientManager 管理客户端信息
            String userId = client.getUserId();  // 获取用户ID

            if(currentFriend.startsWith("团队")){
                FileClient.sendTeamFile(userId,currentFriendID, selectedFile);
            }else{
                // 调用 FileClient 的 sendFile 函数发送文件
                FileClient.sendFile(userId,currentFriendID, selectedFile);
            }
        }
    }


    @FXML Button emojiButton;
    //添加emoji表情到输入文本框，已经绑定按钮fx:id emojiButton
    @FXML
    private void sendEmoji(MouseEvent event){

        // 创建一个 ContextMenu 来显示表情
        ContextMenu emojiMenu = new ContextMenu();

        String[] emojis = {"😊", "😂", "😍", "😎", "😭", "😡", "👍", "💡", "🎉", "❤️"};

        // 为每个表情符号创建一个 MenuItem，并将其添加到 ContextMenu 中
        for (String emoji : emojis) {
            MenuItem emojiItem = new MenuItem(emoji);
            emojiItem.setOnAction(e -> insertEmoji(emoji)); // 点击表情时插入到 TextArea 中
            emojiMenu.getItems().add(emojiItem);
        }
        // 显示 ContextMenu
        emojiMenu.show(emojiButton, event.getScreenX(), event.getScreenY());
    }
    // 插入表情符号到 TextArea 中
    private void insertEmoji(String emoji) {
        // 获取当前光标位置
        int caretPosition = inputArea.getCaretPosition();

        // 在光标位置插入表情
        inputArea.insertText(caretPosition, emoji);
    }
    // 触发 FileClient 的文件下载功能
    private void downloadFile(String fileUrl) {
        System.out.println("1123123123");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");

        // 设置保存文件的默认名称
        fileChooser.setInitialFileName(fileUrl);

        // 显示保存对话框
        java.io.File saveFile = fileChooser.showSaveDialog(chatPane.getScene().getWindow());

        if (saveFile != null) {
            // 调用 FileClient 接收文件并保存
            FileClient.receiveFileAsync(client.getUserId(), fileUrl, saveFile.getParent());
        }
    }
    // 处理点击的消息内容，若消息类型为 "file"，则下载文件
    private void handleMessageClick(String message) {
        // 从点击的消息中解析出信息
        if (message.contains("消息类型: file")) {
            String[] parts = message.split("uploads/");
            if (parts.length == 2) {
                String fileUrl = parts[1];  // 获取文件URL
                downloadFile(fileUrl);
            }
        }
    }

    public void refresh(ActionEvent actionEvent) {
        System.out.println("111111111111111111111");
        client.getFriendList();

    }
}
