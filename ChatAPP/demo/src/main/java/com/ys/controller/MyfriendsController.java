package com.ys.controller;

import com.ys.dao.FriendsDao;
import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.List;

public class MyfriendsController {

    @FXML
    private ListView<HBox> friendsListView; // 使用 ListView<HBox> 来显示每个好友信息

    private FriendsDao friendsDao = new FriendsDao();
    private Client client;

    // ObservableLists 用于 ListView 的展示
    private ObservableList<HBox> friendsItems = FXCollections.observableArrayList();

    public MyfriendsController() {
        this.client = ClientManager.getClient();
    }

    @FXML
    public void initialize() {
        // 初始加载好友列表
        refreshFriendsList();
    }

    // 刷新好友列表
    public void refreshFriendsList() {
        friendsItems.clear();  // 清空现有好友列表

        // 获取当前用户的所有好友
        String currentUserId = client.getUserId();
        List<String> friendIds = friendsDao.getAllFriendsIds(currentUserId);
        List<User> friendsDetails = friendsDao.getFriendDetails(friendIds);

        // 遍历所有好友信息
        for (User friend : friendsDetails) {
            // 创建一个 HBox 来水平排列 username、userid、useremail
            HBox hBox = new HBox(10); // 10 是元素之间的间距

            // 创建显示 username、userid 和 useremail 的 Text，并放入 StackPane 以固定宽度
            StackPane usernamePane = new StackPane(new Text(friend.getUsername()));
            usernamePane.setPrefWidth(200); // 设置固定宽度，确保列宽一致

            StackPane userIdPane = new StackPane(new Text(String.valueOf(friend.getUser_id())));
            userIdPane.setPrefWidth(200); // 设置固定宽度

            StackPane userEmailPane = new StackPane(new Text(friend.getEmail()));
            userEmailPane.setPrefWidth(300); // 设置固定宽度

            // 垂直分隔线1
            Separator separator1 = new Separator();
            separator1.setOrientation(javafx.geometry.Orientation.VERTICAL);

            // 垂直分隔线2
            Separator separator2 = new Separator();
            separator2.setOrientation(javafx.geometry.Orientation.VERTICAL);

            // 添加左右扩展，让组件之间均匀分布
            Region spacer1 = new Region();
            Region spacer2 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            // 将 username、分隔线、userid、分隔线、useremail 添加到 HBox 中
            hBox.getChildren().addAll(usernamePane, separator1, userIdPane, separator2, userEmailPane);
            hBox.setAlignment(Pos.CENTER_LEFT); // 将内容左对齐

            // 将 HBox 添加到 ObservableList 中
            friendsItems.add(hBox);
        }

        // 设置 ListView 的数据
        friendsListView.setItems(friendsItems);
    }

    public void refresh(ActionEvent actionEvent) {
        initialize();
    }
}