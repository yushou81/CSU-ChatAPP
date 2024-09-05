package com.ys.controller;

import com.ys.dao.UserDao;
import com.ys.model.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.io.IOException;

public class LoginController {
    public Button registerBtn;
    public Button loginBtn;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private UserDao userDao = new UserDao();


    public void Register(ActionEvent actionEvent) {
        register();
        registerBtn.getScene().getWindow().hide();
    }

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

    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("错误", "请填写用户名和密码！");
            return;
        }

        User user = userDao.loginUser(username, password);

        System.out.println(user);
        //静态必过 user != null
        if (user != null) {
            showAlert("成功", "登录成功！");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/mainapp.fxml"));
                Parent root  = loader.load();
                Stage newStage2 = new Stage();

                // 设置新Stage的场景，将加载的FXML视图作为根节点
                Scene newScene = new Scene(root);
                newStage2.setScene(newScene);

                // 设置新Stage的标题（可选）
                newStage2.setTitle("新窗口");

                // 显示新Stage
                newStage2.show();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            loginBtn.getScene().getWindow().hide();
        } else {
            showAlert("错误", "用户名或密码不正确！");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
