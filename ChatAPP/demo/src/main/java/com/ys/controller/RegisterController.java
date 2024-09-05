//package com.ys.controller;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//
//public class RegisterController {
//
//    @FXML
//    private TextField usernameField;
//
//    @FXML
//    private PasswordField passwordField;
//
//    @FXML
//    private TextField emailField;
//
//    private RestTemplate restTemplate = new RestTemplate();
//
//    @FXML
//    public void handleRegister() {
//        String username = usernameField.getText();
//        String password = passwordField.getText();
//        String email = emailField.getText();
//
//        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
//            showAlert("输入错误", "请填写所有必填字段！");
//            return;
//        }
//
//        // 创建用户对象
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//        user.setEmail(email);
//
//        // 发送注册请求到后端 API
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<User> request = new HttpEntity<>(user, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                "http://localhost:8080/api/users/register",
//                HttpMethod.POST,
//                request,
//                String.class
//        );
//
//        showAlert("注册结果", response.getBody());
//    }
//
//    private void showAlert(String title, String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}
