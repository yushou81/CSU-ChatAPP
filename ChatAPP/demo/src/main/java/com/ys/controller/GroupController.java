package com.ys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class GroupController {

    @FXML
    private AnchorPane rootPane; // 主 AnchorPane
    public StackPane mainStackPane;
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
    private AnchorPane myTeamPane = new AnchorPane();
    private AnchorPane joinTeamPane = new AnchorPane();
    private AnchorPane createTeamPane = new AnchorPane();

    // 初始化方法
    @FXML
    public   void initialize(){
        try {
            // 加载每个 AnchorPane 对应的 FXML 文件

            myTeamPane = FXMLLoader.load(getClass().getResource("/fxml/myteam.fxml"));

            if (myTeamPane == null) {
                System.out.println("Failed to load chatPane.fxml");
            } else {
                System.out.println("chatPane loaded successfully");
            }

            joinTeamPane = FXMLLoader.load(getClass().getResource("/fxml/jointeam.fxml"));
//            createTeamPane = FXMLLoader.load(getClass().getResource("/fxml/createteam.fxml"));


            System.out.println("groupmainStackPane: " + mainStackPane);


            // 可能的 NullPointerException 源

            // 将所有 AnchorPane 添加到 StackPane 中
            mainStackPane.getChildren().addAll(myTeamPane,joinTeamPane,createTeamPane);

            // 初始化时隐藏所有 AnchorPane
            hideAllPanes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 点击“我的团队”按钮时显示对应的画面
    @FXML
    private void Myteam() {
        hideAllPanes();
        myTeamPane.setVisible(true);
    }

    // 点击“加入团队”按钮时显示对应的画面
    @FXML
    private void Jointeam() {
        hideAllPanes();
        joinTeamPane.setVisible(true);
    }

    // 点击“创建团队”按钮时显示对应的画面
    @FXML
    private void Createteam() {
        hideAllPanes();
        createTeamPane.setVisible(true);
    }
    private void hideAllPanes() {
        myTeamPane.setVisible(false);
        joinTeamPane.setVisible(false);
        createTeamPane.setVisible(false);

        // 隐藏其他 AnchorPane
    }
}
