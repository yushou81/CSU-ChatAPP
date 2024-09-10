package com.ys.controller;

import com.ys.service.client.VideoAudioClient;
import com.ys.service.client.VideoAudioClientManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import org.bytedeco.javacv.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class VideoMeetingController {

    public Button closeMeetingBtn;
    public Button micCloseBtn;
    public Button micOpenBtn;
    public Button cameraOpenBtn;
    public Button cameraCloseBtn;
    public Label meetingIdLabel;
    @FXML
    private ImageView image;
    @FXML
    private ImageView videoImageView;  // 用于在 JavaFX 中显示捕获的视频帧
    @FXML
    private ImageView localVideoImageView;

    private VideoAudioClient videoAudioClient;
    private String meetingId;
    private boolean isStreaming = true;
    private boolean isCameraOn = true;
    private boolean isMicrophoneOn = true;

    public VideoMeetingController() throws Exception {
        this.videoAudioClient = VideoAudioClientManager.getClient();

    }
    // 初始化控制器
    public void initialize() {
        videoAudioClient.setVideoMeetingController(this);  // 创建 VideoClient 实例
        meetingId= videoAudioClient.getMeetingId();
        meetingIdLabel.setText("会议ID:"+meetingId);
    }
    public void updateLocalVideoFrame(BufferedImage bufferedImage) {
        // 在 JavaFX 应用线程上更新 UI
        Platform.runLater(() -> {
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);

            localVideoImageView.setImage(fxImage);  // 更新 ImageView
        });

    }

    // 显示捕获到的视频帧
    public void updateVideoFrame(BufferedImage bufferedImage) {
        // 在 JavaFX 应用线程上更新 UI
        Platform.runLater(() -> {
            Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            videoImageView.setImage(fxImage);  // 更新 ImageView
        });
    }

    public void closeMeeting(ActionEvent actionEvent) throws IOException {
        closeMeetingBtn.getScene().getWindow().hide(); // 关闭窗口
        videoAudioClient.leaveMeeting();
    }

    public void micOpenBtn(ActionEvent actionEvent) {
        System.out.println("micopen");
        micOpenBtn.setVisible(true);
        micCloseBtn.setVisible(false);
        videoAudioClient.setMicrophoneStatus(true);
    }

    public void micCloseBtn(ActionEvent actionEvent) {
        micCloseBtn.setVisible(true);
        micOpenBtn.setVisible(false);
        videoAudioClient.setMicrophoneStatus(false);
    }

    public void cameraOpenBtn(ActionEvent actionEvent) {
        System.out.println("这是关闭");
        cameraOpenBtn.setVisible(true);
        cameraCloseBtn.setVisible(false);
        videoAudioClient.setCameraStatus(true);
    }

    public void cameraCloseBtn(ActionEvent actionEvent) {
        System.out.println("这是关闭");
        cameraCloseBtn.setVisible(true);
        cameraOpenBtn.setVisible(false);
        videoAudioClient.setCameraStatus(false);
    }

}
