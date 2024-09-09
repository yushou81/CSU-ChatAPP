package com.ys.controller;

import com.ys.dao.FriendsDao;
import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AddfriendsController {

    private Client client;
    private FriendsDao friendsDao = new FriendsDao();

    @FXML
    private TextField friendIdField; // 输入好友ID的文本框
    @FXML
    private TextField messageField;  // 输入邀请信息的文本框

    public AddfriendsController() {
        this.client = ClientManager.getClient();
        client.setAddFriendController(this);
    }

    // 搜索好友按钮点击处理
    @FXML
    public void handleSearchFriend(ActionEvent actionEvent) {
        String friendId = friendIdField.getText();
        System.out.println("搜索");
        if (friendId.isEmpty()) {
            showAlert("错误", "请输入好友ID！");
            return;
        }
        client.searchFriend(friendId);
        if (client.searchFriend(friendId)) {
            showAlert("成功", "好友请求已发送！");
        } else {
            showAlert("错误", "发送好友请求失败！");
        }
    }

    // 添加好友按钮点击处理
    @FXML
    public void handleAddFriend(ActionEvent actionEvent) {
        String friendId = friendIdField.getText();
        String message = messageField.getText();

        if (friendId.isEmpty()) {
            showAlert("错误", "请输入好友ID！");
            return;
        }

        if (message.isEmpty()) {
            showAlert("错误", "请输入邀请信息！");
            return;
        }

        // 通过Client发送好友请求
        if (client.sendFriendRequest(friendId, message)) {
            showAlert("成功", "好友请求已发送！");
        } else {
            showAlert("错误", "发送好友请求失败！");
        }
    }


    // 返回按钮点击处理
    @FXML


    // 提示框
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void success(String message){
        Platform.runLater(() -> {
            showAlert("搜索成功", message);
        });
    }
}
