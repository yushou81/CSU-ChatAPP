
package com.ys.controller;



import com.ys.dao.UserDao;
import com.ys.model.User;
import com.ys.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField emailField;

    private UserDao userDao = new UserDao();

    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showAlert("错误", "请填写所有字段！");
            return;
        }

        User user = new User(username, password, email);
        boolean success = userDao.registerUser(user);

        if (success) {
            showAlert("成功", "用户注册成功！");
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
}
