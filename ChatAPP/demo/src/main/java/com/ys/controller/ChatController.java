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


    //需要在点击contractList中内容时切换chatPane内容和nameTitle内容

    @FXML
    private ListView<?> contractList; // 可以将泛型类型替换为实际的数据类型，如 ListView<String>。
    @FXML
    private StackPane nameTitle;
    @FXML
    private TextArea textArea;
    @FXML
    private Separator separator;
    @FXML
    private Button sendMessageButton;
    @FXML
    AnchorPane chatPane;

    @FXML
    public void initialize() {
        // 初始化控件或绑定数据
        sendMessageButton.setOnAction(event -> handleSendMessage());
    }


    @FXML
    private TextArea messageInput;  // 输入消息框

    private Client client;  // 注入的客户端实例


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
<<<<<<< HEAD


            //这一部分要等服务器处理规范进行修改


            System.out.println("Message sent: " + message);
            textArea.clear(); // 清空文本区
        } else {
=======
            client.sendMessage(message);  // 通过客户端发送消息
            messageInput.clear();  // 清空输入框
        }else {
>>>>>>> 200ef183738ae52d1b4efb8eb569585763783e4d
            System.out.println("Message cannot be empty.");
        }
    }

    // 更新聊天框
    private void updateChatDisplay(String message) {
//        chatDisplay.appendText(message + "\n");
    }



    //更新头像
    private void updateAvatar(){

    }
    //发送信息
    private void sendMessage(){

    }




}
