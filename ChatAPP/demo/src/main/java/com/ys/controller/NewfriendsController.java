package com.ys.controller;

import com.ys.dao.FriendsDao;
import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class NewfriendsController {

    @FXML
    private ListView<HBox> friendsRequestListView;  // 使用 ListView<HBox> 来显示每一行的内容

    private FriendsDao friendsDao = new FriendsDao(); // 创建FriendsDao的实例
    private Client client;
    private MyfriendsController myfriendsController; // 引入 MyfriendsController

    // ObservableLists 用于 ListView 的展示
    private ObservableList<HBox> friendsRequestItems = FXCollections.observableArrayList();

    // 无参构造函数（JavaFX 需要它来初始化控制器）
    public NewfriendsController() {
        this.client = ClientManager.getClient();
    }

    // 提供一个方法来设置 MyfriendsController
    public void setMyfriendsController(MyfriendsController myfriendsController) {
        this.myfriendsController = myfriendsController;
    }

    @FXML

    public void initialize() {
        String currentUserId = client.getUserId(); // 获取当前用户ID
        client.setNewfriendsController(this);

        // 向服务器发送请求获取好友请求列表
        client.sendMessage("GET_NEW_FRIEND:" + currentUserId);
        System.out.println("发送信息了");


    }

    public void receiveNewFriend(String response) {
        // 异步接收服务器返回的数据
        if (response.startsWith("FRIEND_REQUEST_LIST:")) {
            System.out.println("newcontroller这里接收到信息");
            // 使用逗号分隔请求ID和请求消息
            String[] requestEntries = response.substring("FRIEND_REQUEST_LIST:".length()).split(",");
            System.out.println("解析后的好友请求条目数量: " + requestEntries.length);  // 检查解析的条目数

            for (int i = 0; i < requestEntries.length; i += 2) {
                if (i + 1 < requestEntries.length) {
                    String friendRequestId = requestEntries[i];
                    String friendRequestMessage = requestEntries[i + 1];
                    System.out.println("好友请求ID: " + friendRequestId + "，消息: " + friendRequestMessage);
                }
            }

            Platform.runLater(() -> {
                // 先清空列表
                friendsRequestItems.clear();

                // 解析并创建每个请求的UI
                for (int i = 0; i < requestEntries.length; i += 2) {  // 每次读取两个元素：requestId 和 requestMessage
                    if (i + 1 < requestEntries.length) {  // 确保有成对的ID和消息
                        String friendRequestId = requestEntries[i];
                        String friendRequestMessage = requestEntries[i + 1];
                        System.out.println("Request ID: " + friendRequestId);
                        System.out.println("Request Message: " + friendRequestMessage);
                        HBox hBox = createFriendRequestItem(friendRequestId, friendRequestMessage);
                        friendsRequestItems.add(hBox);
                    }
                }

                // 设置 ListView 的数据
                friendsRequestListView.setItems(friendsRequestItems);
            });
        } else if (response.equals("NO_FRIEND_REQUESTS")) {
            Platform.runLater(() -> {
                // 如果没有好友请求，显示提示
                friendsRequestItems.clear();
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                hBox.getChildren().add(new Text("没有新的好友请求"));
                friendsRequestItems.add(hBox);

                friendsRequestListView.setItems(friendsRequestItems);
            });
        }
    }



    // Helper method to create a friend request item
    private HBox createFriendRequestItem(String friendRequestId, String friendRequestMessage) {
        HBox hBox = new HBox(10);  // 设置每行的间隔

        // 显示请求ID
        StackPane userIdPane = new StackPane(new Text("请求 ID: " + friendRequestId));
        userIdPane.setPrefWidth(150); // 设置固定宽度

        // 显示请求消息
        StackPane messagePane = new StackPane(new Text("消息: " + friendRequestMessage));
        messagePane.setPrefWidth(250); // 设置固定宽度

        Button acceptButton = new Button("同意");
        acceptButton.setOnAction(event -> {
            handleAcceptFriend(client.getUserId(), friendRequestId);
        });

        Button rejectButton = new Button("拒绝");
        rejectButton.setOnAction(event -> {
            handleRejectFriend(client.getUserId(), friendRequestId);
        });

        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        hBox.getChildren().addAll(userIdPane, messagePane, spacer1, acceptButton, rejectButton);
        return hBox;
    }



    // 处理同意按钮逻辑
    private void handleAcceptFriend(String currentUserId, String friendRequestId) {

        if (client.acceptFriendRequest(friendRequestId, currentUserId)) {

            friendsDao.deleteFriendRequest(friendRequestId, currentUserId);


            showAlert("成功", "好友请求已接受！");

            // 刷新 MyfriendsController 中的好友列表
            if (myfriendsController != null) {
                // 确保在 JavaFX 应用线程上刷新 UI
                Platform.runLater(() -> myfriendsController.refreshFriendsList());
            }

            // 刷新好友请求列表
            refreshFriendsRequestList();
        } else {
            showAlert("错误", "无法接受好友请求，请稍后再试。");
        }
    }


    // 处理拒绝按钮逻辑
    private void handleRejectFriend(String currentUserId, String friendRequestId) {
        // 发送拒绝请求到服务器
        if (client.rejectFriendRequest(friendRequestId ,currentUserId)) {
            // 从 addfriends 表中删除好友请求记录
            friendsDao.deleteFriendRequest(friendRequestId, currentUserId);

            showAlert("成功", "你已拒绝该好友请求。");

            // 刷新好友请求列表
            refreshFriendsRequestList();
        } else {
            showAlert("错误", "无法拒绝好友请求，请稍后再试。");
        }
    }


    // 刷新好友请求列表
    private void refreshFriendsRequestList() {
        friendsRequestItems.clear();  // 清空现有列表
        initialize();  // 重新加载数据
    }

    // 显示提示信息
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refresh(ActionEvent actionEvent) {
        initialize();
    }
}
