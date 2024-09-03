package com.ys;
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

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建主布局
        BorderPane root = new BorderPane();

        // 左侧菜单按钮
        Button btnChat = new Button("Chat");
        Button btnTeam = new Button("Team");
        Button btnMeeting = new Button("Meeting");

        VBox menu = new VBox(10, btnChat, btnTeam, btnMeeting);
        root.setLeft(menu);

        // 创建场景并设置到舞台
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("DingDing Clone");
        primaryStage.show();

        // 按钮事件处理，加载不同的界面到右侧区域
        btnChat.setOnAction(e -> loadView(root, "chat.fxml"));
        btnTeam.setOnAction(e -> loadView(root, "team.fxml"));
        btnMeeting.setOnAction(e -> loadView(root, "meeting.fxml"));
    }
    private void loadView(BorderPane root, String fxmlFile) {
        try {


            // 使用相对路径加载 FXML 文件
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/"+ fxmlFile));
            root.setCenter(view); // 将界面加载到右侧区域
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}




