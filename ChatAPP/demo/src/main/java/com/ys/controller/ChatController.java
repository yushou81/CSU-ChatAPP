package com.ys.controller;


//kjashfhaskdhfkfhabsk
import com.ys.service.client.Client;

import com.ys.service.client.ClientManager;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
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

    private Map<String, ObservableList<String>> chatMessages = new HashMap<>();  // 用于存储每个好友的聊天记录
    public ChatController() {
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
    }


    @FXML
    public void initialize() {
        //显示联系人
        //这一部分联系人要从服务器读取
        Map<String, String> friendMap = client.getFriendList();
        // 获取所有的键 (user_id)，并将其转换为String数组
        String[] userNames = getUserNames(friendMap);

        // 添加好友列表的点击事件
        friendListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            currentFriend = (String) newValue;  // 切换到选中的好友
            currentFriendID = friendMap.get(currentFriend);
            System.out.println(currentFriend);
            showMessagesForFriend(currentFriend);
        });

        // 假设这是你的好友列表
        ObservableList<String> friends = FXCollections.observableArrayList(userNames);
        friendListView.setItems(friends);

        // 预先初始化每个好友的聊天记录
        for (String friend : friends) {
            chatMessages.put(friend, FXCollections.observableArrayList());
        }


    }


    // 将Map中的键提取为String数组
    public static String[] getUserNames(Map<String, String> map) {
        Set<String> keys = map.keySet();  // 获取所有的键 (user_id)
        return keys.toArray(new String[0]);  // 将Set转换为String数组
    }



    // 显示选中好友的聊天记录
    private void showMessagesForFriend(String friend) {
        messageListView.setItems(chatMessages.get(friend));  // 切换到该好友的消息记录
    }
    // 发送消息
    @FXML
    private void sendMessage() {
        String message = inputArea.getText();
        if (message.isEmpty() || currentFriend == null) {
            return;
        }

        // 将消息添加到当前聊天窗口（右侧显示）
        chatMessages.get(currentFriend).add("我: " + message);
        inputArea.clear();  // 清空输入框

        // 这里可以发送消息到服务器
         client.sendMessage("PRIVATE:" + currentFriendID + ":" + message);
    }

    // 处理接收到的消息
    public void receiveMessage(String fromFriend, String message) {
        // 将消息添加到该好友的聊天记录中
        chatMessages.get(fromFriend).add(fromFriend + ": " + message);

        // 如果当前窗口正在显示该好友的聊天内容，更新UI
        if (fromFriend.equals(currentFriend)) {
            messageListView.setItems(chatMessages.get(fromFriend));  // 更新消息列表
        }
    }
//    处理好友列表
    public String[] extractUsernames(String[] friendList) {
        String[] usernames = new String[friendList.length];  // 创建新的数组来存储用户名
        for (int i = 0; i < friendList.length; i++) {
            String[] parts = friendList[i].split(": ");  // 通过 ": " 分割字符串
            if (parts.length == 2) {
                usernames[i] = parts[1].trim();  // 提取用户名并去掉多余空格
            }
        }
        return usernames;
    }







    // 动态加载对应联系人的聊天界面
    private void loadChatPane(String contactName) {
        //       try {
        // 假设为每个联系人加载不同的聊天界面，可以根据联系人ID加载不同的FXML文件
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + contactName + "Chat.fxml"));
//            AnchorPane chatPane = loader.load();

        // 替换 messageArea 的内容
//            messageArea.getChildren().clear();
//            messageArea.getChildren().add(chatPane);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    }


    // 设置客户端实例
//    public void setClient(Client client) {
//        this.client = client;
//
//        // 启动一个线程接收服务器的消息并更新UI
//        new Thread(() -> {
//            try {
//                String message;
//                while ((message = client.receiveMessage()) != null) {
//                    updateChatDisplay(message);  // 更新聊天记录
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }

    // 发送消息给服务器
    @FXML
    public void handleSendMessage() {
        String message = inputArea.getText();
        if (!message.isEmpty()) {


            //这一部分要等服务器处理规范进行修改

            client.sendMessage(message);  // 通过客户端发送消息
            inputArea.clear();  // 清空输入框
        }else {

            System.out.println("Message cannot be empty.");
        }
    }

//    // 更新聊天框
//    private void updateChatDisplay(String message) {
//        System.out.println(message);
//
////        chatDisplay.appendText(message + "\n");
//
//    }



//    //更新头像
//    private void updateAvatar(){
//
//    }
//    //发送信息
//    private void sendMessage(){
//
//    }





}
