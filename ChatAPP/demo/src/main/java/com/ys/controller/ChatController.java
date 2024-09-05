package com.ys.controller;

//kjashfhaskdhfkfhabsk
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Separator;
public class ChatController {

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
    public void initialize() {
        // 初始化控件或绑定数据
        sendMessageButton.setOnAction(event -> handleSendMessage());
    }

    private void handleSendMessage() {
        // 处理发送消息的逻辑
        String message = textArea.getText(); // 获取文本区中的文本
        if (!message.isEmpty()) {
            System.out.println("Message sent: " + message);
            textArea.clear(); // 清空文本区
        } else {
            System.out.println("Message cannot be empty.");
        }
    }




    //更新头像
    private void updateAvatar(){

    }
    //发送信息
    private void sendMessage(){

    }




}
