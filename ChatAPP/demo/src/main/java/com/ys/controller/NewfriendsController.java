package com.ys.controller;

import com.ys.dao.FriendsDao;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class NewfriendsController {

    @FXML
    private ListView<HBox> friendsRequestListView;  // 使用 ListView<HBox> 来显示每一行的内容

    private FriendsDao friendsDao = new FriendsDao(); // 创建FriendsDao的实例
    private Client client;

    // ObservableLists 用于 ListView 的展示
    private ObservableList<HBox> friendsRequestItems = FXCollections.observableArrayList();

    public NewfriendsController() {
        this.client = ClientManager.getClient();
    }

    @FXML
    public void initialize() {
        // 从数据库获取好友请求
        String currentUserId = client.getUserId(); // 使用实际的当前用户ID
        String friendRequestId = friendsDao.getFriendsRequestId(currentUserId);

        if (friendRequestId != null) {
            // 如果有好友请求，获取消息
            String message = friendsDao.getFriendRequestMessage(currentUserId);

            // 创建一个 HBox 来水平排列 userId、message 和按钮
            HBox hBox = new HBox(10);  // 10 是元素之间的间距

            // 创建 VBox 来让 Text 竖直居中
            VBox userIdBox = new VBox();
            userIdBox.setAlignment(Pos.CENTER);  // 设置竖直居中
            Text userIdText = new Text(friendRequestId);
            userIdBox.getChildren().add(userIdText);

            // 垂直分隔线1
            Separator separator1 = new Separator();
            separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);

            VBox messageBox = new VBox();
            messageBox.setAlignment(Pos.CENTER);  // 设置竖直居中
            Text messageText = new Text(message);
            messageBox.getChildren().add(messageText);

            // 垂直分隔线2
            Separator separator2 = new Separator();
            separator2.setOrientation(javafx.geometry.Orientation.VERTICAL);

            // 创建同意和拒绝按钮
            Button acceptButton = new Button("同意");
            acceptButton.setOnAction(event -> {
                System.out.println("接受好友请求: " + friendRequestId);
                // 可以调用其他DAO方法进行处理
            });

            Button rejectButton = new Button("拒绝");
            rejectButton.setOnAction(event -> {
                System.out.println("拒绝好友请求: " + friendRequestId);
                // 可以调用其他DAO方法进行处理
            });

            // 添加左右扩展，让组件之间均匀分布
            Region spacer1 = new Region();
            Region spacer2 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            // 将 VBox（包含 userIdText 和 messageText）、分隔线以及按钮添加到 HBox 中
            hBox.getChildren().addAll(userIdBox, separator1, spacer1, messageBox, separator2, spacer2, acceptButton, rejectButton);

            // 将 HBox 添加到 ObservableList 中
            friendsRequestItems.add(hBox);
        }

        // 设置 ListView 的数据
        friendsRequestListView.setItems(friendsRequestItems);
    }
}
