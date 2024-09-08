package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class CreateTeamController implements Client.MessageListener{

    @FXML
    private TextField teamNameField;

    @FXML
    private Button createTeamButton;

    private Client client;

    @FXML
    private void initialize() {
        this.client = ClientManager.getClient();
        client.setMessageListener(this);
        createTeamButton.setOnAction(event -> createTeam());
    }

    // 点击创建团队按钮，处理创建团队的逻辑
    private void createTeam() {
        String teamName = teamNameField.getText().trim();
        if (teamName == null || teamName.trim().isEmpty()) {
            showAlert("无效的团队名称", "请输入有效的团队名称");
            return;
        }
        client.sendCreateTeamRequest(client.getUserId(),teamName);


    }

    //收到新消息时调用
    @Override
    public void onMessageReceived(String message) {
    }
    //收到历史信息时调用
    @Override
    public void onHistoryReceived(List<String> history) {

    }
    //创建群聊成功
    @Override
    public void onCreateGroup(String wrongMessage,boolean success) {
        Platform.runLater(()->{ if(success)
        this.showAlert("成功","创建群聊成功");
        else this.showAlert("失败",wrongMessage);
        });
    }
    @Override
    public void onFriendListReceived(Map<String,String>friendList){

    }



    // 显示警告对话框
    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void  setClient(Client client){
        this.client=client;
    }


}
