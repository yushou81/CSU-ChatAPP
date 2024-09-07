package com.ys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class TakepartinController {
    public Button back;

    public void Backhome(ActionEvent actionEvent) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
            AnchorPane view1 = FXMLLoader.load(getClass().getResource("/fxml/mainapp.fxml"));
            StackPane stackPane = new StackPane();
            stackPane.getChildren().addAll(view1,view);
            stackPane.setVisible(true);
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(stackPane);
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
}
