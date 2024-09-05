package com.ys.controller;
import javafx.fxml.FXML;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class ContractListController implements Initializable {

    @FXML
    private Button favoritefriends;

    @FXML
    private Button newfriends;

    @FXML
    private Button Createteambtn;

    @FXML
    private Button Createteambtn1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialization code here
    }

    @FXML
    private void favoritefriends(ActionEvent event) {
        // Action for "常用联系人" button
        System.out.println("常用联系人 button pressed");
    }

    @FXML
    private void newfriends(ActionEvent event) {
        // Action for "新的朋友" button
        System.out.println("新的朋友 button pressed");
    }

    @FXML
    private void Myfriends(ActionEvent event) {
        // Action for "我的好友" button
        System.out.println("我的好友 button pressed");
    }

    @FXML
    private void addfriends(ActionEvent event) {
        // Action for "添加好友" button
        System.out.println("添加好友 button pressed");
    }
}