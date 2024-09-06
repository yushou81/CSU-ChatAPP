package com.ys.controller;


//kjashfhaskdhfkfhabsk
import com.ys.service.client.Client;

import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class ChatController {


    @FXML
    private ListView<String> contractList; // 可以将泛型类型替换为实际的数据类型，如 ListView<String>。
    @FXML
    private StackPane nameTitle;//顶部名称和头像区域
    @FXML
    private TextArea messageInput;//消息输入框
    @FXML
    private StackPane messageArea;  // 不同联系人聊天内容区域

    @FXML
    private Separator separator;
    @FXML
    private Button sendMessageButton;//发送按钮
    @FXML
    AnchorPane chatPane;//整个聊天功能区域

    @FXML
    public void initialize() {
        // 初始化控件或绑定数据
        sendMessageButton.setOnAction(event -> handleSendMessage());
        //显示联系人
        //这一部分联系人要从服务器读取
        ObservableList<String> contacts = FXCollections.observableArrayList("联系人1", "联系人2", "联系人3");
        contractList.setItems(contacts);

        // 为 ListView 添加选择事件监听器
        contractList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 当选择联系人时，动态加载相应的聊天界面
                loadChatPane(newValue);
            }
        });




    }

    // 注入的客户端实例，用于发送和接受信息
    private Client client;


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
    public void setClient(Client client) {
        this.client = client;

        // 启动一个线程接收服务器的消息并更新UI
        new Thread(() -> {
            try {
                String message;
                while ((message = client.receiveMessage()) != null) {
                    updateChatDisplay(message);  // 更新聊天记录
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 发送消息给服务器
    @FXML
    public void handleSendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            client.sendMessage(message);  // 通过客户端发送消息
            messageInput.clear();  // 清空输入框
        }else {
            System.out.println("Message cannot be empty.");
        }
    }

    // 更新聊天框
    private void updateChatDisplay(String message) {
        System.out.println(message);

//        chatDisplay.appendText(message + "\n");

    }



    //更新头像
    private void updateAvatar(){

    }
    //发送信息
    private void sendMessage(){

    }





}
