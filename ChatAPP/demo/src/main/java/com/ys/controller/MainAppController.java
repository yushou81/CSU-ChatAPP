package com.ys.controller;

import javafx.fxml.FXML;
import javafx.event.*;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Stack;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;  ///
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainAppController {

    @FXML
    private Button messageButton;

    @FXML
    private StackPane mainStackPane;
    @FXML
    private AnchorPane chatPane;
    @FXML
    private AnchorPane contractListPane;
    @FXML
    private AnchorPane groupPane;
    @FXML
    private AnchorPane meetingPane;
    @FXML
    private AnchorPane fileManagementPane;
    @FXML
    private AnchorPane collectionPane;
    @FXML
    private AnchorPane settingPane;
    @FXML
    private AnchorPane workbenchPane;

    @FXML
    public   void initialize(){
        try {
            // 加载每个 AnchorPane 对应的 FXML 文件
            //注意，这个地方是空，WIP
            System.out.println(getClass().getResource("/fxml/chat.fxml"));
            chatPane = FXMLLoader.load(getClass().getResource("/fxml/chat.fxml"));
            System.out.println("能够加载到对应的chat.fxml");
            if (chatPane == null) {
                System.out.println("Failed to load chatPane.fxml");
            } else {
                System.out.println("chatPane loaded successfully");
            }

            contractListPane = FXMLLoader.load(getClass().getResource("/fxml/contractlist.fxml"));
            System.out.println("contractListPane加载成功");
            groupPane = FXMLLoader.load(getClass().getResource("/fxml/group.fxml"));
            System.out.println("groupPane加载成功");
            meetingPane = FXMLLoader.load(getClass().getResource("/fxml/meeting.fxml"));
            fileManagementPane = FXMLLoader.load(getClass().getResource("/fxml/filemanagement.fxml"));
            collectionPane = FXMLLoader.load(getClass().getResource("/fxml/collection.fxml"));
            settingPane = FXMLLoader.load(getClass().getResource("/fxml/setting.fxml"));
            workbenchPane = FXMLLoader.load(getClass().getResource("/fxml/workbench.fxml"));


            // 将所有 AnchorPane 添加到 StackPane 中
            mainStackPane.getChildren().addAll(chatPane, contractListPane, groupPane, meetingPane, fileManagementPane, collectionPane, settingPane, workbenchPane);

            // 初始化时隐藏所有 AnchorPane
            hideAllPanes();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("有啥加载不成功");
        }
    }





    @FXML
    public void handleContractListButtonClick(ActionEvent event){
        hideAllPanes();
        contractListPane.setVisible(true);
    }
    @FXML
    public void handleMessageButtonClick(ActionEvent event){
        hideAllPanes();
        chatPane.setVisible(true);
    }
    @FXML
    public void handleGrouButtonClick(ActionEvent event){
        hideAllPanes();
        groupPane.setVisible(true);
    }
    @FXML
    public void handleMeetingButtonClick(ActionEvent event){
        hideAllPanes();
        meetingPane.setVisible(true);
    }

    @FXML
    public void handleFileManagementButtonClick(ActionEvent event){
        hideAllPanes();
        fileManagementPane.setVisible(true);
    }
    @FXML
    public void handleWorkbenchButtonClick(ActionEvent event){
        hideAllPanes();
        workbenchPane.setVisible(true);
    }
    @FXML
    public void handleCollectionButtonClick(ActionEvent event){
        hideAllPanes();
        collectionPane.setVisible(true);
    }
    @FXML
    public void handleSettingButtonClick(ActionEvent event){
        hideAllPanes();
        settingPane.setVisible(true);
    }
    private void hideAllPanes() {
        chatPane.setVisible(false);
        contractListPane.setVisible(false);
        groupPane.setVisible(false);
        meetingPane.setVisible(false);
        fileManagementPane.setVisible(false);
        collectionPane.setVisible(false);
        workbenchPane.setVisible(false);
        settingPane.setVisible(false);
        // 隐藏其他 AnchorPane
    }






}
