package com.ys.controller;

import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SettingController {

        @FXML
        private TextField usernameField;
        @FXML
        private TextField passwordField;
        @FXML
        private TextField identifyPasswordField;
        @FXML
        private Button confirmButton;
        private Client client;
        public SettingController() {
                // 使用ClientManager来获取共享的Client实例
                this.client = ClientManager.getClient();

        }
        public void initialize(){
                client.setSettingController(this);
        }
        // 确定按钮
        @FXML
        private void handleConfirm() {
                System.out.println("确认");
                String newUsername = usernameField.getText();
                String newPassword = passwordField.getText();
                String identifyNewPassword = identifyPasswordField.getText();

                // 检查两次密码是否一致
                if (!newPassword.equals(identifyNewPassword)) {
                        showAlert("密码错误", "两次输入的密码不一致，请重新输入。", AlertType.ERROR);
                        return;
                }

                // 确保用户名和密码都不为空
                if (newUsername.isEmpty() || newPassword.isEmpty()) {
                        showAlert("输入不完整", "用户名或密码不能为空！", AlertType.WARNING);
                        return;
                }

                // 调用 client 的 sendMessage 发送用户名和密码
                String message = "UPDATE_USER:" +client.getUserId()+":" +newUsername + ":" + newPassword;
                client.sendMessage(message);

                // 显示操作成功提示框
//                showAlert("更新成功", "用户名和密码已更新。", AlertType.INFORMATION);

                System.out.println("新的用户名: " + newUsername);
                System.out.println("新的密码: " + newPassword);
                System.out.println("已发送更新请求给服务器。");
        }
        // 用于弹出提示框
        public void showAlert(String title, String message, AlertType alertType) {
                Alert alert = new Alert(alertType);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();  // 显示对话框，等待用户点击确定
        }
        public void success(){
                Platform.runLater(() -> {
                        showAlert("更新成功", "用户名和密码已更新。", AlertType.INFORMATION);
                });

        }
        public void fail(){
                Platform.runLater(() -> {
                        showAlert("更新失败", "用户名重复。", AlertType.INFORMATION);
                });

        }
}



