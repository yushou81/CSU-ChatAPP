package com.ys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class OpenLiveController {
    public Button back;
//    private StackPane root;
//
//    public void text(){
//        try {
//            root = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
//            root.setVisible(false);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void Backhome(ActionEvent actionEvent) {
        try {
            //root = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
            AnchorPane view1 = FXMLLoader.load(getClass().getResource("/fxml/mainapp.fxml"));
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
            Stage newStage1 = new Stage();

            view1.getChildren().add(view);
            AnchorPane.setLeftAnchor(view, 100.0);
            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view1);
            newStage1.setScene(newScene);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();
            back.getScene().getWindow().hide();


            //隐藏旧的Stage

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
