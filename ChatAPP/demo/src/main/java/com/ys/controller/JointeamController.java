package com.ys.controller;

import com.ys.service.client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;

import static java.lang.System.in;
import static java.lang.System.out;

public class JointeamController {

    @FXML
    private TextField teamName;

    @FXML
    private Button joinTeamButton;
    private Client client;
    @FXML

    private void initialize() throws Exception {

        this.client= ClientManager.getClient();

        joinTeamButton.setOnAction(event -> joinTeam());
    }


    //点击加入团队按钮，函数
    private void joinTeam() {
        String teamId = teamName.getText().trim();
        if (teamId == null || teamId.trim().isEmpty()) {
            // 处理无效的团队 ID
            out.println("debug:"+"无效的团队");
            showAlert("无效的ID", "请输入有效的团队id");

            return;
        }

        //

        // 发送加入团队的请求到服务器
        if (client.sendJoinTeamRequest(client.getUserId(), teamName.getText())) {
            showAlert("成功", "成功加入团队");
        } else {
            showAlert("失败", "加入团队失败");
        }

    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void setClient(Client client) {
        this.client = client;
    }
}
