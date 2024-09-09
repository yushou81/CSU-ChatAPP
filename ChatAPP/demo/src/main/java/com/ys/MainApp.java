package com.ys;
import com.ys.controller.LoginController;
import com.ys.service.client.ClientManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import com.ys.controller.ChatController;
import com.ys.service.client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.IplImage;

public class MainApp extends Application {
    private Client client;  // 客户端连接实例

    @Override
    public void start(Stage primaryStage) throws Exception {

        //         初始化客户端并连接服务器
        client = new Client();


        client.connect("100.67.111.153", 8080);  // 替换为你的服务器IP和端口号

//        100.64.83.48
//        192.168.221.164
        //100.69.24.231

        ClientManager.setClient(client);


        try {
            // 加载 FXML 文件

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainapp.fxml"));


            Parent root = loader.load();
          
            // 创建场景
            Scene scene = new Scene(root);


            // 设置窗口为透明
//            primaryStage.initStyle(StageStyle.TRANSPARENT);
            // 设置 Stage 的样式为无边框
            // primaryStage.initStyle(StageStyle.UNDECORATED);
            // 设置场景和标题 之后需要删除
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 按钮事件处理，加载不同的界面到右侧区域
//        btnChat.setOnAction(e -> loadView(root, "chat.fxml"));
//        btnTeam.setOnAction(e -> loadView(root, "group.fxml"));
//        btnMeeting.setOnAction(e -> loadView(root, "meeting.fxml"));
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
