package com.ys.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

//加入会议按钮事件
public class MeetingController {
    public Button initiateBtn;
    public Button liveBtn;
    public Button getinBtn;


    public void joinMeeting(ActionEvent actionEvent) {

        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/takepartin.fxml"));
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view,1280,854);
            newStage1.setScene(newScene);

            // 设置窗口为透明
            newStage1.initStyle(StageStyle.TRANSPARENT);
            // 设置 Stage 的样式为无边框
            newStage1.initStyle(StageStyle.UNDECORATED);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Button creatMeetingBtn;

    public void LiveBrocast(ActionEvent actionEvent) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/openLive.fxml"));
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view,1280,854);
            newStage1.setScene(newScene);

            // 设置窗口为透明
            newStage1.initStyle(StageStyle.TRANSPARENT);
            // 设置 Stage 的样式为无边框
            newStage1.initStyle(StageStyle.UNDECORATED);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void creatMeeting(ActionEvent actionEvent) {
        try {
            AnchorPane view = FXMLLoader.load(getClass().getResource("/fxml/creatMeeting.fxml"));
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view,1280,854);
            newStage1.setScene(newScene);

            // 设置窗口为透明
            newStage1.initStyle(StageStyle.TRANSPARENT);
            // 设置 Stage 的样式为无边框
            newStage1.initStyle(StageStyle.UNDECORATED);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
