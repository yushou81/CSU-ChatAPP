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

    public Button closeMeetingBtn;
    public Button micCloseBtn;
    public Button micOpenBtn;
    public Button cameraOpenBtn;
    public Button cameraCloseBtn;
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

    // 显示捕获到的视频帧
    public void updateVideoFrame(BufferedImage bufferedImage) {
        // 在 JavaFX 应用线程上更新 UI
        Platform.runLater(() -> {
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            videoImageView.setImage(fxImage);  // 更新 ImageView
        });
    }

    public void closeMeeting(ActionEvent actionEvent) {
        closeMeetingBtn.getScene().getWindow().hide(); // 关闭窗口
        videoStreamClient.leaveMeeting();
    }

    public void micOpenBtn(ActionEvent actionEvent) {
        micOpenBtn.setVisible(false);
        micCloseBtn.setVisible(true);
        videoStreamClient.setMicrophoneStatus(true);
    }

    public void micCloseBtn(ActionEvent actionEvent) {
        micCloseBtn.setVisible(false);
        micOpenBtn.setVisible(true);
        videoStreamClient.setMicrophoneStatus(false);
    }

    public void cameraOpenBtn(ActionEvent actionEvent) {
        System.out.println("这是关闭");
        cameraOpenBtn.setVisible(false);
        cameraCloseBtn.setVisible(true);
        image.setVisible(false);
        videoStreamClient.setCameraStatus(true);
    }

    public void cameraCloseBtn(ActionEvent actionEvent) {
        System.out.println("这是关闭");
        cameraCloseBtn.setVisible(false);
        cameraOpenBtn.setVisible(true);image.setVisible(true);
        videoStreamClient.setCameraStatus(false);
    }
}
