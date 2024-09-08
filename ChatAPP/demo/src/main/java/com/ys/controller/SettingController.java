package com.ys.controller;

import com.ys.service.client.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SettingController {

        @FXML
        private TextField usernameField;

        @FXML
        private Label usernameLabel;

        @FXML
        private TextField passwordField;

        @FXML
        private Label passwordLabel;

        @FXML
        private Button changeIcon;

        @FXML
        private Button confirmButton;

        @FXML
        private Button returnButton;

        private Client client;


        // 初始化
        public void initialize() {
                confirmButton.setOnAction(event -> modify());
        }

        private void modify() {

        }

        // 更换头像按钮
        @FXML
        private void handleChangeIcon() {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择头像");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("图像文件", "*.png", "*.jpg", "*.jpeg", "*.gif")
                );
                File selectedFile = fileChooser.showOpenDialog(new Stage());
                if (selectedFile != null) {
                        System.out.println("选择的头像文件: " + selectedFile.getAbsolutePath());

                        //保存到数据库
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!pass!!!!!!!!!!!!!!!!!!!!!!!!
                }
        }

        // 确定按钮
        @FXML
        private void handleConfirm() {
                String newUsername = usernameField.getText();
                String newPassword = passwordField.getText();

                System.out.println("新的用户名: " + newUsername);
                System.out.println("新的密码: " + newPassword);

                // 保存用户名和密码
                //!!!!!!!!!!!!!!!!!!!!!pass!!!!!!!!!!!!!!!!!!!!!!
        }

        // 取消按钮
        @FXML
        private void handleReturn() {

        }
}


