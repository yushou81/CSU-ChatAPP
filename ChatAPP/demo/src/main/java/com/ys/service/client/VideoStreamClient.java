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
