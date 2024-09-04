package main.java.com.ys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;

public class MainApp extends Application {

    // 定义图像的统一大小
    private static final double IMAGE_WIDTH = 24;
    private static final double IMAGE_HEIGHT = 24;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建主布局
        BorderPane root = new BorderPane();

        // 创建带有图像的按钮
        Button btnChat = createButton("", "chat.png");
        Button btnTeam = createButton("", "team.png");
        Button btnMeeting = createButton("", "meeting.png");
        Button btnVideo = createButton("", "video.png");
        Button btnFile = createButton("", "file.png");
        Button btnWork = createButton("", "work.png");
        Button btnFavorite = createButton("", "favorite.png");
        Button btnSet = createButton("", "set.png");

        btnChat.getStyleClass().add("button-style");
        btnTeam.getStyleClass().add("button-style");
        btnMeeting.getStyleClass().add("button-style");
        btnVideo.getStyleClass().add("button-style");
        btnFile.getStyleClass().add("button-style");
        btnWork.getStyleClass().add("button-style");
        btnFavorite.getStyleClass().add("button-style");
        btnSet.getStyleClass().add("button-style");

        VBox menu = new VBox(10, btnChat, btnTeam, btnMeeting, btnVideo, btnFile, btnWork, btnFavorite, btnSet);
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


    private Button createButton(String text, String imageName) {
        Button button = new Button(text);
        // 加载图像
        InputStream imageStream = getClass().getResourceAsStream("/png/" + imageName);
        if (imageStream == null) {
            System.err.println("Failed to load image: " + imageName);
            return button;
        }
        Image image = new Image(imageStream);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        // 设置图像到按钮
        button.setGraphic(imageView);
        button.setStyle("-fx-font-size: 14px; -fx-padding: 10px;"); // 可选：设置按钮样式
        return button;
    }


    private void loadView(BorderPane root, String fxmlFile) {
        try {
            // 使用相对路径加载 FXML 文件
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            root.setCenter(view); // 将界面加载到右侧区域
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
