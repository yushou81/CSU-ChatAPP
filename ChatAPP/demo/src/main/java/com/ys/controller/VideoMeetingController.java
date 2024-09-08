package com.ys.controller;

import com.ys.service.client.VideoStreamClient;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import org.bytedeco.javacv.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class VideoMeetingController {

    @FXML
    private ImageView videoImageView;  // 用于在 JavaFX 中显示捕获的视频帧

    private VideoStreamClient videoStreamClient;
    private boolean isStreaming = false;

    // 初始化控制器
    public void initialize() {
        videoStreamClient = new VideoStreamClient();  // 创建 VideoClient 实例
    }

    // 开始会议并捕获视频
    @FXML
    public void startMeeting(String meetingId) {
        isStreaming = true;
        new Thread(() -> {
            try {
                // 使用 OpenCVFrameGrabber 从摄像头捕捉视频
                OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // 使用默认摄像头
                grabber.start();

                Frame frame;
                while (isStreaming && (frame = grabber.grab()) != null) {
                    // 将 Frame 转换为 BufferedImage
                    Java2DFrameConverter converter = new Java2DFrameConverter();
                    BufferedImage bufferedImage = converter.convert(frame);

                    // 在 JavaFX 的 ImageView 中显示视频帧
                    Platform.runLater(() -> {
                        videoImageView.setImage(SwingFXUtils.toFXImage(bufferedImage, null));
                    });

                    // 将视频帧转换为字节流并发送给 VideoClient
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // 通过 VideoClient 发送视频帧
                    videoStreamClient.sendVideoFrame(meetingId, imageBytes);

                    // 控制帧率，避免过多资源占用
                    Thread.sleep(100);  // 控制帧率为 10 FPS
                }

                grabber.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 停止视频会议
    @FXML
    public void stopMeeting() {
        isStreaming = false;
        videoStreamClient.closeConnection();  // 停止视频流传输并关闭连接
    }
}
