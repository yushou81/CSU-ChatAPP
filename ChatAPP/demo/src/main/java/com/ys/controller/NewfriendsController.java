package com.ys.controller;

import com.ys.dao.FriendsDao;
import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
        // 从数据库获取所有好友请求
        String currentUserId = client.getUserId(); // 当前用户ID
        List<String> friendRequestIds = friendsDao.getAllFriendRequestIds(currentUserId); // 获取所有好友请求

        if (!friendRequestIds.isEmpty()) {
            for (String friendRequestId : friendRequestIds) {
                String message = friendsDao.getFriendRequestMessage(friendRequestId);

                // 创建一个 HBox 来水平排列 userId、message 和按钮
                HBox hBox = new HBox(10);  // 10 是元素之间的间距

                // 使用 StackPane 设置固定宽度来保证分割线对齐
                StackPane userIdPane = new StackPane(new Text(friendRequestId));
                userIdPane.setPrefWidth(150); // 设置固定宽度

                // 垂直分隔线1
                Separator separator1 = new Separator();
                separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);

                StackPane messagePane = new StackPane(new Text(message));
                messagePane.setPrefWidth(300); // 设置固定宽度

                // 垂直分隔线2
                Separator separator2 = new Separator();
                separator2.setOrientation(javafx.geometry.Orientation.VERTICAL);

                // 创建同意和拒绝按钮
                Button acceptButton = new Button("同意");
                acceptButton.setOnAction(event -> {
                    handleAcceptFriend(currentUserId, friendRequestId);
                });

                Button rejectButton = new Button("拒绝");
                rejectButton.setOnAction(event -> {
                    handleRejectFriend(currentUserId, friendRequestId);
                });

                // 添加左右扩展，让组件之间均匀分布
                Region spacer1 = new Region();
                Region spacer2 = new Region();
                HBox.setHgrow(spacer1, Priority.ALWAYS);
                HBox.setHgrow(spacer2, Priority.ALWAYS);

                // 将 StackPane、分隔线、按钮添加到 HBox 中
                hBox.getChildren().addAll(userIdPane, separator1, spacer1, messagePane, separator2, spacer2, acceptButton, rejectButton);

                // 将 HBox 添加到 ObservableList 中
                friendsRequestItems.add(hBox);
            }
        } else {
            // 如果没有好友请求，可以展示一个提示
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(new Text("没有新的好友请求"));
            friendsRequestItems.add(hBox);
        }

        // 设置 ListView 的数据
        friendsRequestListView.setItems(friendsRequestItems);
    }

    // 处理同意按钮逻辑
    // 处理同意按钮逻辑
    private void handleAcceptFriend(String currentUserId, String friendRequestId) {
        // 发送同意请求到服务器
        if (client.acceptFriendRequest(friendRequestId)) {
            // 从 addfriends 表中删除好友请求记录
            friendsDao.deleteFriendRequest(friendRequestId, currentUserId);

            // 获取好友的详细信息
            User friend = friendsDao.searchUser(friendRequestId);
            if (friend != null) {
                // 将好友关系插入到 user_friends 表中
                friendsDao.addFriendToUserFriends(currentUserId, friendRequestId);
                showAlert("成功", "你和 " + friend.getUsername() + " 成为了好友！");

                // 刷新 MyfriendsController 中的好友列表
                if (myfriendsController != null) {
                    // 确保在 JavaFX 应用线程上刷新 UI
                    Platform.runLater(() -> myfriendsController.refreshFriendsList());
                }
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
        if (client.rejectFriendRequest(friendRequestId)) {
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
}
