package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.*;

import javafx.scene.input.MouseEvent;
import javafx.concurrent.Task;
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
    private String currentFriend;  // 当前聊天的好友
    private String currentFriendID;
    Map<String, String> friendMap;

    private Map<String, ObservableList<String>> chatMessages = new HashMap<>();  // 用于存储每个好友的聊天记录
    public ChatController() {
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
                Platform.runLater(() -> {
                    System.out.println("plat"+message);
                    String[] parts = messageCopy.split(" 的私聊消息: ");
                    if (parts.length == 2) {
                        String [] partss = parts[0].split("来自用户 ");
                        if(partss.length==2){
                            String senderId = partss[1].trim();
                            String privateMessage = parts[1].trim();
                            String senderName = getFriendNameById(senderId);
                            System.out.println(senderId+"h"+privateMessage+"q"+senderName);
                            receiveMessage(senderName, privateMessage);
                        }
                    }
                });
            }

            @Override
            public void onHistoryReceived(List<String> history) {
                System.out.println("接收历史消息完毕");
                List<String> historyCopy = new ArrayList<>(history);
                // 历史消息处理
                Platform.runLater(() -> {
                    System.out.println(historyCopy);
                    if (currentFriend != null) {
                        System.out.println("开始设置历史消息");
                        chatMessages.get(currentFriend).addAll(historyCopy);
                        showMessagesForFriend(currentFriend);
                    }
                });
            }

            @Override
            public void onFriendListReceived(Map<String, String> friendList) {
                //在另一个线程进入到platform时，friendlist会被清空不知道为什么
                Map<String, String> friendListCopy = new HashMap<>(friendList);
                Platform.runLater(() -> {
                    loadFriendList(friendListCopy);
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
        System.out.println("加载好友列表");
        friendListView.setItems(friends);


        // 初始化每个好友的聊天记录
        for (String friend : friends) {
            chatMessages.put(friend, FXCollections.observableArrayList());
        }

        // 设置好友列表点击事件
        friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentFriend = (String) newValue;
            currentFriendID = friendMap.get(currentFriend);

            // 如果聊天记录为空，则从服务器加载历史记录
            if (chatMessages.get(currentFriend).isEmpty()) {
                client.requestMessageHistory(Integer.parseInt(currentFriendID));
            } else {
                showMessagesForFriend(currentFriend);
            }
        });
    }
  
    // 发送私人消息
    @FXML
    private void sendMessage() {
        String message = inputArea.getText();
        if (!message.isEmpty() && currentFriend != null) {
            chatMessages.get(currentFriend).add("我: " + message);
            inputArea.clear();
            System.out.println("发送私聊"+message);
            client.sendMessage("PRIVATE:" + currentFriendID + ":" + message);

        }
    }



    // 接收消息并显示，在on里面调用
    private void receiveMessage(String fromFriend, String message) {
        chatMessages.get(fromFriend).add(fromFriend + ": " + message);
        if (fromFriend.equals(currentFriend)) {
            showMessagesForFriend(currentFriend);
        }
    }


    private void showMessagesForFriend(String friend) {
        messageListView.setItems(chatMessages.get(friend));
    }

    private String getFriendNameById(String friendId) {
//        还没写好
        String friendName = friendMap.get(friendId);
        return friendName;
    }



    //发送文件，已经绑定文件按钮fx:id fileButton
    @FXML
    private void sendFile(MouseEvent event){

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

}
