package com.ys.controller;

import com.ys.service.client.VideoStreamClient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

import com.ys.service.client.VideoStreamClientManager;
import javafx.fxml.FXML;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import org.bytedeco.javacv.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class VideoMeetingController {

    public Button endButton;
    public Button voiceButtonNO;
    public Button voiceButtonYes;
    public Button cameraButton;
    public Button cameraOffButton;
    @FXML
    private ImageView image;
    @FXML
    private ImageView videoImageView;  // 用于在 JavaFX 中显示捕获的视频帧

    private VideoStreamClient videoStreamClient;
    private boolean isStreaming = false;
    private boolean isCameraOn = true;
    private boolean isMicrophoneOn = true;


    public VideoMeetingController(){
        this.videoStreamClient = VideoStreamClientManager.getClient();
    }
    // 初始化控制器
    public void initialize() {
        videoStreamClient.setVideoMeetingController(this);  // 创建 VideoClient 实例
    }

    // 启动视频会议的按钮点击事件
    @FXML
    public void startVideoMeeting() {
        String meetingId = "12345";  // 示例会议ID
        String serverIp = "localhost";  // 服务器IP地址
        int serverPort = 5555;  // 服务器端口
        videoStreamClient.startVideoStream(meetingId, serverIp, serverPort);
    }

    // 显示捕获到的视频帧
    public void updateVideoFrame(BufferedImage bufferedImage) {
        // 在 JavaFX 应用线程上更新 UI
        Platform.runLater(() -> {
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            videoImageView.setImage(fxImage);  // 更新 ImageView
        });
    }
    // 启动摄像头
    @FXML
    public void turnOnCamera(ActionEvent actionEvent) {
        isCameraOn = true;
        videoStreamClient.setCameraStatus(true);
        cameraButton.setVisible(false);
        cameraOffButton.setVisible(true);
    }

    // 关闭摄像头
    @FXML
    public void turnOffCamera(ActionEvent actionEvent) {
        isCameraOn = false;
        videoStreamClient.setCameraStatus(false);
        cameraButton.setVisible(true);
        cameraOffButton.setVisible(false);
    }

    // 打开麦克风
    @FXML
    public void unmuteMic(ActionEvent actionEvent) {
        isMicrophoneOn = true;
        videoStreamClient.setMicrophoneStatus(true);
        voiceButtonNO.setVisible(false);
        voiceButtonYes.setVisible(true);
    }

    // 静音麦克风
    @FXML
    public void muteMic(ActionEvent actionEvent) {
        isMicrophoneOn = false;
        videoStreamClient.setMicrophoneStatus(false);
        voiceButtonNO.setVisible(true);
        voiceButtonYes.setVisible(false);
    }

    // 停止视频会议
    @FXML
    public void stopMeeting() {
        isStreaming = false;
        videoStreamClient.closeConnection();  // 停止视频流传输并关闭连接
    }

    public void End(ActionEvent actionEvent) {
        endButton.getScene().getWindow().hide(); // 关闭窗口
    }

    public void voiceclick(ActionEvent actionEvent) {
        voiceButtonNO.setVisible(false);voiceButtonYes.setVisible(true);
    }

    public void voiceclick1(ActionEvent actionEvent) {
        voiceButtonNO.setVisible(true);voiceButtonYes.setVisible(false);
    }

    public void Closeview(ActionEvent actionEvent) {
        cameraButton.setVisible(false);cameraOffButton.setVisible(true);image.setVisible(false);
    }

    public void Openview(ActionEvent actionEvent) {
        cameraOffButton.setVisible(false);cameraButton.setVisible(true);image.setVisible(true);
    }
}
