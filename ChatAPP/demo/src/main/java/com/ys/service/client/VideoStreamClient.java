package com.ys.service.client;

import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class VideoStreamClient {
    private SocketChannel videoChannel;
    public void startVideoStream(String meetingId, String serverIp, int serverPort) {
        try {
            // 连接到视频服务器
            videoChannel = SocketChannel.open();
            videoChannel.configureBlocking(true); // 阻塞模式，确保数据按顺序传输
            videoChannel.connect(new InetSocketAddress(serverIp, serverPort));
            System.out.println("成功连接到视频服务器，端口: " + serverPort);

            // 启动摄像头并开始捕获视频流
            OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // 使用默认摄像头
            grabber.start();

            Frame frame;
            while ((frame = grabber.grab()) != null) {
                // 将 Frame 转换为 BufferedImage
                Java2DFrameConverter converter = new Java2DFrameConverter();
                BufferedImage bufferedImage = converter.convert(frame);

                // 将视频帧转换为字节流
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // 通过 sendVideoFrame 发送视频帧
                sendVideoFrame(meetingId, imageBytes);

                // 控制帧率，避免占用过多资源
                Thread.sleep(100);  // 控制帧率为 10 FPS
            }

            grabber.stop();
            closeConnection();  // 视频流结束后，关闭连接

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 发送视频帧给服务端
    public void sendVideoFrame(String meetingId, byte[] frameData) {
        try {
            // 发送会议ID
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put(meetingId.getBytes());
            buffer.flip();
            videoChannel.write(buffer);
            buffer.clear();

            // 发送帧大小
            ByteBuffer frameBuffer = ByteBuffer.allocate(4 + frameData.length);
            frameBuffer.putInt(frameData.length);
            frameBuffer.put(frameData);
            frameBuffer.flip();

            // 发送视频帧数据
            while (frameBuffer.hasRemaining()) {
                videoChannel.write(frameBuffer);
            }
            frameBuffer.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 关闭连接
    public void closeConnection() {
        try {
            if (videoChannel != null && videoChannel.isOpen()) {
                videoChannel.close();
                System.out.println("视频连接关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
