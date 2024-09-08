package com.ys.service.server;

import java.io.*;
import java.net.Socket;

public class VideoStreamHandler implements Runnable {
    private Socket clientSocket;

    public VideoStreamHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream videoIn = new DataInputStream(clientSocket.getInputStream());

            String meetingId = videoIn.readUTF();  // 首先读取会议号
            System.out.println("会议号: " + meetingId);

            while (true) {
                // 接收视频帧
                int frameSize = videoIn.readInt();
                byte[] frameBytes = new byte[frameSize];
                videoIn.readFully(frameBytes);

                // 在此处处理视频帧，例如解码或转发到其他客户端
                System.out.println("接收到一帧视频数据，大小: " + frameSize + " bytes");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
