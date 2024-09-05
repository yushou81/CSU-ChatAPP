package com.ys.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class TeamController {

    @FXML
    private AnchorPane rootPane; // 主 AnchorPane

    @FXML
    private Button Myteambtn;

    @FXML
    private Button Jointeambtn;

    @FXML
    private Button Createteambtn;

    @FXML
    private ImageView Myteamicon;

    @FXML
    private ImageView Jointeamicon;

    @FXML
    private ImageView Createteamicon;

    // 定义三个 BorderPane，用于展示不同的画面内容
    private BorderPane myTeamPane = new BorderPane();
    private BorderPane joinTeamPane = new BorderPane();
    private BorderPane createTeamPane = new BorderPane();

    // 初始化方法
    @FXML
    public void initialize() {
        // 初始化时可以设置各个 Pane 的内容，或者加载不同的 FXML 文件
        myTeamPane.setStyle("-fx-background-color: #f0f0f0;");
        joinTeamPane.setStyle("-fx-background-color: #e0e0e0;");
        createTeamPane.setStyle("-fx-background-color: #d0d0d0;");
    }

    // 点击“我的团队”按钮时显示对应的画面
    @FXML
    private void Myteam() {
        rootPane.getChildren().clear(); // 清空当前 AnchorPane
        rootPane.getChildren().add(myTeamPane); // 添加我的团队视图
    }

    // 点击“加入团队”按钮时显示对应的画面
    @FXML
    private void Jointeam() {
        rootPane.getChildren().clear(); // 清空当前 AnchorPane
        rootPane.getChildren().add(joinTeamPane); // 添加加入团队视图
    }

    // 点击“创建团队”按钮时显示对应的画面
    @FXML
    private void Createteam() {
        rootPane.getChildren().clear(); // 清空当前 AnchorPane
        rootPane.getChildren().add(createTeamPane); // 添加创建团队视图
    }
}
