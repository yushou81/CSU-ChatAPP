package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class CreatMeetingController {
    public TextField meetingNameField;

    public TextField passwordField;
    public Button cancel;
    public Button creation;
    private Client client;
    public CreatMeetingController() {
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
    }



    public void createMeetingBtn(ActionEvent actionEvent) {
        // 获取用户输入的会议名称和密码
        String meetingName = meetingNameField.getText().trim();
        String password = passwordField.getText().trim();

        // 验证输入是否为空
        if (meetingName.isEmpty() || password.isEmpty()) {
            showAlert("输入错误", "会议名称和密码不能为空！");
            return;
        }

        // 调用client的createMeeting方法创建会议
        client.createMeeting(meetingName, password);

        // 显示提示
        showAlert("会议创建成功", "您的会议已成功创建！");

        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/videoMeeting.fxml"));
            Stage newStage1 = new Stage();
            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view,1264,840);
            newStage1.setScene(newScene);
            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");
            // 显示新Stage
            newStage1.show();
            //隐藏旧的Stage
            creation.getScene().getWindow().hide();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 显示警告或提示框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void Hide(ActionEvent actionEvent) {
        cancel.getScene().getWindow().hide();
    }

    public void Creat1(ActionEvent actionEvent) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/videoMeeting.fxml"));
            Stage newStage1 = new Stage();
            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view,1264,840);
            newStage1.setScene(newScene);
            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");
            // 显示新Stage
            newStage1.show();
            //隐藏旧的Stage
            creation.getScene().getWindow().hide();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
