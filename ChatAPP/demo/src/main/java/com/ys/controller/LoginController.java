package com.ys.controller;

import com.ys.dao.UserDao;
import com.ys.model.User;
import com.ys.service.client.Client;
import com.ys.service.client.ClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoginController {

    private Client client;  // 注入的客户端实例
    public LoginController(){
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
        // 启动一个线程接收服务器的消息并更新UI
//        new Thread(() -> {
//            try {
//                String message;
//                while ((message = client.receiveMessage()) != null) {
//                    updateChatDisplay(message);  // 更新聊天记录
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
    }

    public Button registerBtn;
    public Button loginBtn;

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;
    // 记录鼠标按下时的偏移量
    private double xOffset = 0;
    private double yOffset = 0;

    private UserDao userDao = new UserDao();

    //前往注册按钮
    public void Register(ActionEvent actionEvent) {
        register();
        registerBtn.getScene().getWindow().hide();
    }

    //打开注册界面
    private void register() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            // 创建一个新的Stage
            Stage newStage1 = new Stage();

            // 设置新Stage的场景，将加载的FXML视图作为根节点
            Scene newScene = new Scene(view);
            newStage1.setScene(newScene);

            // 设置新Stage的标题（可选）
            newStage1.setTitle("新窗口");

            // 显示新Stage
            newStage1.show();


            System.out.println("新窗口已显示");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //登录按钮
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        System.out.println(username+password);

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("错误", "请填写用户名和密码！");
            return;
        }

        // 调用客户端的login方法尝试登录
        if (client.login(username, password)) {
            System.out.println("登录成功，您可以开始发送消息了！");
            showAlert("成功", "登录成功！");
            openMainApp();  // 登录成功后打开主应用界面
            loginBtn.getScene().getWindow().hide();  // 隐藏登录窗口
        } else {
            // 如果登录失败，提示用户，并让他们重新输入
            showAlert("错误", "用户名或密码不正确，请重新输入！");
        }
    }
    // 打开主应用界面
    private void openMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainapp.fxml"));
            Parent root = loader.load();
            Stage mainStage = new Stage();
            Scene mainScene = new Scene(root);

//            mainScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
//            mainStage.initStyle(StageStyle.TRANSPARENT);



            mainStage.setScene(mainScene);
            mainStage.setTitle("主应用");
            mainStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //报错
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void initialize() {


        // 为rootPane添加鼠标事件来实现窗口拖动功能
        rootPane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        rootPane.setOnMouseDragged(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

}
