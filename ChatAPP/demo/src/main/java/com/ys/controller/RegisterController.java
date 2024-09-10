
package com.ys.controller;



import com.ys.dao.UserDao;
import com.ys.model.User;
import com.ys.service.client.ClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import com.ys.service.client.Client;


public class RegisterController {
    public Button loginBtn;
    public Button registerBtn;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    private UserDao userDao = new UserDao();
    // 获取共享的Client实例
    private Client client;

    public RegisterController() throws Exception {
        // 使用ClientManager来获取共享的Client实例
        this.client = ClientManager.getClient();
    }

    // 注册按钮的处理方法
    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("错误", "请填写所有字段！");
            return;
        }

        // 通过Client完成注册操作
        boolean success = client.register(username, password, email);

        if (success) {
            showAlert("成功", "用户注册成功！");
            try {
                // 加载登录界面
                Parent view = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
                Stage newStage1 = new Stage();
                Scene newScene = new Scene(view);
                newStage1.setScene(newScene);
                newStage1.setTitle("登录");

                // 显示登录界面
                newStage1.show();

                System.out.println("登录窗口已显示");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // 隐藏注册窗口
            registerBtn.getScene().getWindow().hide();
        } else {
            showAlert("错误", "注册失败，请稍后再试！");
        }
    }




    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleBackLogin(ActionEvent actionEvent) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
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
        loginBtn.getScene().getWindow().hide();

    }
}
