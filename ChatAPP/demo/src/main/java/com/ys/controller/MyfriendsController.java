package com.ys.controller;

import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Map;

public class MyfriendsController {

    @FXML
    private ListView<HBox> friendsListView; // 使用 ListView<HBox> 来显示每个好友信息

    private Client client;

    // ObservableLists 用于 ListView 的展示
    private ObservableList<HBox> friendsItems = FXCollections.observableArrayList();

    public MyfriendsController() {
        try {
            this.client = ClientManager.getClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void initialize() {
        // 注册好友列表的监听器
        client.setMessageListener(new Client.MessageListener() {
            @Override
            public void onMessageReceived(String message) {
                // 不处理普通消息
            }

            @Override
            public void onHistoryReceived(java.util.List<String> history) {
                // 不处理历史消息
            }

            @Override
            public void onFriendListReceived(Map<String, String> friendList) {
                // 当收到好友列表时，更新UI
                Platform.runLater(() -> {
                    friendsItems.clear();  // 清空现有的好友列表
                    for (Map.Entry<String, String> entry : friendList.entrySet()) {
                        String friendName = entry.getKey();
                        String[] details = entry.getValue().split(",");
                        String friendId = details[0];
                        String friendEmail = details[1];

                        // 创建HBox显示好友信息
                        HBox hBox = new HBox(10); // 10 是元素之间的间距

                        StackPane usernamePane = new StackPane(new Text("用户名: " + friendName));
                        usernamePane.setPrefWidth(200); // 设置固定宽度，确保列宽一致

                        StackPane userIdPane = new StackPane(new Text("id: " + friendId));
                        userIdPane.setPrefWidth(200); // 设置固定宽度

                        StackPane userEmailPane = new StackPane(new Text("邮箱: " + friendEmail));
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
                });
            }

            @Override
            public void onCreateGroup(String teamName, boolean success) {

            }

            @Override
            public void onTeamListReceived(Map<String, String> groupList) {

            }
        });

        // 初始加载好友列表
        refreshFriendsList();
    }

    // 刷新好友列表
    public void refreshFriendsList() {
        // 向服务器请求好友列表
        client.getmyFriendList();
    }

    // 手动触发刷新
    public void refresh(javafx.event.ActionEvent actionEvent) {
        refreshFriendsList();
    }

    // 处理服务器返回的好友列表响应
    public void receivemyFriend(String response) {
        // 可以不需要该方法，因为消息监听器已经处理好友列表更新了
    }
}
