package com.ys.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

public class ContractListController {

    @FXML
    private Button addfriends;

    @FXML
    private Button favoritefriends;

    @FXML
    private Button newfriends;

    @FXML
    private Button myfridends;

    @FXML
    private AnchorPane rootPane; // 主 AnchorPane

    @FXML
    private StackPane mainStackPane;

    @FXML
    private AnchorPane favoritefriendspane;

    @FXML
    private AnchorPane myfriendspane;

    @FXML
    private AnchorPane addfriendspane;

    @FXML
    private AnchorPane newfriendspane;

    // 初始化方法
    @FXML
    public void initialize() {
        try {
            // 加载每个 AnchorPane 对应的 FXML 文件
            if (favoritefriendspane == null) {
                favoritefriendspane = FXMLLoader.load(getClass().getResource("/fxml/favoritefriends.fxml"));
            }

            if (myfriendspane == null) {
                myfriendspane = FXMLLoader.load(getClass().getResource("/fxml/myfriends.fxml"));
            }

            if (addfriendspane == null) {
                addfriendspane = FXMLLoader.load(getClass().getResource("/fxml/addfriends.fxml"));
            }

            if (newfriendspane == null) {
                newfriendspane = FXMLLoader.load(getClass().getResource("/fxml/newfriends.fxml"));
            }

            // 检查 mainStackPane 是否为 null
            if (mainStackPane == null) {
                System.out.println("mainStackPane 尚未初始化！");
                return;
            }

            // 将所有 AnchorPane 添加到 StackPane 中
            mainStackPane.getChildren().addAll(favoritefriendspane, myfriendspane, newfriendspane, addfriendspane);

            // 初始化时隐藏所有 AnchorPane
            hideAllPanes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void favoritefriends(ActionEvent event) {
        hideAllPanes();
        favoritefriendspane.setVisible(true);
    }

    @FXML
    private void newfriends(ActionEvent event) {
        hideAllPanes();
        newfriendspane.setVisible(true);
    }

    @FXML
    private void Myfriends(ActionEvent event) {
        hideAllPanes();
        myfriendspane.setVisible(true);
    }

    @FXML
    private void addfriends(ActionEvent event) {
        hideAllPanes();
        addfriendspane.setVisible(true);
    }

    private void hideAllPanes() {
         addfriendspane.setVisible(false);
         myfriendspane.setVisible(false);
         favoritefriendspane.setVisible(false);
         newfriendspane.setVisible(false);
    }
}
