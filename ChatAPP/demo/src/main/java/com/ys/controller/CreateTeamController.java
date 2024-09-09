package com.ys.controller;

import com.ys.service.client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CreateTeamController {

    @FXML
    private TextField teamNameField;

    @FXML
    private Button createTeamButton;

    private Client client;



    // 点击创建团队按钮，处理创建团队的逻辑
    @FXML
    private void createTeam() {
        String teamName = teamNameField.getText().trim();
        if (teamName == null || teamName.trim().isEmpty()) {
            showAlert("无效的团队名称", "请输入有效的团队名称");
            return;
        }

        // 发送创建团队的请求到服务器
        if (client.sendCreateTeamRequest(client.getUserId(),teamName)) {
            showAlert("成功", "团队创建成功");
        } else {
            showAlert("失败", "团队创建失败");
        }
    }

    // 发送创建团队请求到服务器


    // 显示警告对话框
    private void showAlert(String title, String content) {
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
