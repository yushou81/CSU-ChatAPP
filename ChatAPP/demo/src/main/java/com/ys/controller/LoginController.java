package com.ys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    public Button registerBtn;

    public void Register(ActionEvent actionEvent) {
        register();
        registerBtn.getScene().getWindow().hide();

    }

    private void register() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            // 创建一个新的Stage
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view);
            newStage1.setScene(newScene);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();


            System.out.println("新窗口已显示");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void Denglu(ActionEvent actionEvent) {



        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainapp.fxml"));
            Parent root  = loader.load();
            Stage newStage2 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(root);
            newStage2.setScene(newScene);

            // 设置新Stage的标题（可选）
            newStage2.setTitle("新窗口");

            // 显示新Stage
            newStage2.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 创建场景并设置到舞台

    }
}
