package com.ys.controller;


//kjashfhaskdhfkfhabsk
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.input.MouseEvent;


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
            System.out.println(currentFriend+":"+currentFriendID);

            // 清空当前聊天记录，并加载新的聊天记录
            chatMessages.get(currentFriend).clear();
            client.getMessageHistory(Integer.parseInt(currentFriendID));

            showMessagesForFriend(currentFriend);
        });

        // 假设这是你的好友列表
        ObservableList<String> friends = FXCollections.observableArrayList(userNames);
        friendListView.setItems(friends);

        // 预先初始化每个好友的聊天记录
        for (String friend : friends) {
            chatMessages.put(friend, FXCollections.observableArrayList());
        }
        startReceiveMessageThread();
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

    // 接收消息线程
    private void startReceiveMessageThread() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = client.receiveMessage();  // 从服务器接收消息
                    if (message != null) {
                        handleMessageReceived(message);  // 处理接收到的消息
                    }
                }
            } catch (IOException e) {
                System.err.println("Error receiving message: " + e.getMessage());
            }
        }).start();
    }

    // 处理接收到的消息
    private void handleMessageReceived(String message) {
        // 假设服务器发送的消息格式是：来自用户 {userId} 的私聊消息: {privateMessage}
        if (message.startsWith("私聊消息：来自用户 ")) {
            // 解析消息内容
            String[] parts = message.split(" 的私聊消息: ");
            if (parts.length == 2) {
                String senderInfo = parts[0].replace("来自用户 ", "").trim();  // 提取用户ID
                String privateMessage = parts[1].trim();  // 提取私聊消息内容

                // 在UI线程上更新聊天界面
                Platform.runLater(() -> {
                    String senderName = getFriendNameById(senderInfo);  // 根据用户ID查找好友名
                    receiveMessage(senderName, privateMessage);  // 处理接收到的消息
                });
            }
        }
    }
    // 通过好友ID查找好友名
    private String getFriendNameById(String friendId) {
        for (Map.Entry<String, String> entry : client.getFriendList().entrySet()) {
            if (entry.getValue().equals(friendId)) {
                return entry.getKey();  // 返回好友名
            }
        }
        return "未知好友";  // 如果找不到好友ID，返回默认名字
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

    //这个函数不完全，需要大改
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

    //更新好友列表
    private void updateContractlist(){


    }


    //发送文件，已经绑定文件按钮fx:id fileButton
    @FXML
    private void sendFile(){

    }


    @FXML Button emojiButton;
    //添加emoji表情到输入文本框，已经绑定按钮fx:id emojiButton
    @FXML
    private void sendEmoji(MouseEvent event){

        // 创建一个 ContextMenu 来显示表情
        ContextMenu emojiMenu = new ContextMenu();

        // 定义一些常见表情符号
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
