package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TakepartinController {
    public Button back;
    public TextField meetingIdField;
    public Button Oview;
    public TextField passwordField;
    public Button cancel;
    private Client client;
    public TakepartinController() {
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
    }

    public void Backhome(ActionEvent actionEvent) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
            AnchorPane view1 = FXMLLoader.load(getClass().getResource("/fxml/mainapp.fxml"));
            view1.getChildren().add(view);
            AnchorPane.setLeftAnchor(view, 100.0);
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view1);
            newStage1.setScene(newScene);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();
            //隐藏旧的Stage
            back.getScene().getWindow().hide();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void joinMeeting(ActionEvent actionEvent) {
        // 获取用户输入的会议名称和密码
        String meetingId = meetingIdField.getText().trim();
        String password = passwordField.getText().trim();

        // 验证输入是否为空
        if (meetingId.isEmpty() || password.isEmpty()) {
            showAlert("输入错误", "会议名称和密码不能为空！");
            return;
        }

        // 调用client的createMeeting方法创建会议
        client.joinMeeting(meetingId, password);

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
            Oview.getScene().getWindow().hide();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 返回到主界面（可选）
//        Backhome(actionEvent);
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



    public void Openview(ActionEvent actionEvent) {

    }
}
