package com.ys.service.client;

import com.ys.controller.VideoMeetingController;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.sound.sampled.*;

public class VideoStreamClient {
    private VideoMeetingController videoMeetingController;
    private DatagramSocket udpSocket;
    private OpenCVFrameGrabber videoGrabber;
    private AudioFormat audioFormat;
    private TargetDataLine microphone;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean isCameraOn = true;
    private boolean isMicrophoneOn = true;
    private boolean isRunning = true;

    // 构造函数，传递 VideoMeetingController 实例
    public void setVideoMeetingController(VideoMeetingController controller) {
        this.videoMeetingController = controller;
    }

    // 开始视频流传输
    public void startVideoStream(String meetingId, String serverIp, int serverPort) {
        try {
            udpSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIp);
            this.serverPort = serverPort;

            // 启动摄像头和麦克风的采集和发送
            new Thread(() -> {
                try {
                    startCaptureAndSendFrames(meetingId);
                } catch (FrameGrabber.Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 捕获视频和音频帧并发送到服务器
    private void startCaptureAndSendFrames(String meetingId) throws FrameGrabber.Exception {
        try {
            if (isCameraOn) startCamera();
            if (isMicrophoneOn) startMicrophone();

            while (isRunning) {
                long currentTime = System.currentTimeMillis();  // 当前时间戳

                if (isCameraOn) {
                    // 获取视频帧并发送
                    Frame frame = videoGrabber.grab();
                    if (frame != null) {
                        BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);
                        videoMeetingController.updateVideoFrame(bufferedImage);
                        sendVideoFrame(meetingId, bufferedImage, currentTime);
                    }
                }

                if (isMicrophoneOn) {
                    // 获取音频帧并发送
                    byte[] audioData = captureAudio();
                    sendAudioFrame(meetingId, audioData, currentTime);
                }

                Thread.sleep(100);  // 控制帧率
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopCamera();
            stopMicrophone();
        }
    }

    // 启动摄像头
    private void startCamera() throws FrameGrabber.Exception {
        videoGrabber = new OpenCVFrameGrabber(0);
        videoGrabber.start();
        System.out.println("摄像头启动成功");
    }

    // 停止摄像头
    private void stopCamera() throws FrameGrabber.Exception {
        if (videoGrabber != null) {
            videoGrabber.stop();
            videoGrabber.release();
            System.out.println("摄像头已停止");
        }
    }

    // 启动麦克风
    private void startMicrophone() {
        try {
            audioFormat = new AudioFormat(44100, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(audioFormat);
            microphone.start();
            System.out.println("麦克风启动成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 停止麦克风
    private void stopMicrophone() {
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            System.out.println("麦克风已停止");
        }
    }

    // 捕获音频数据
    private byte[] captureAudio() {
        byte[] buffer = new byte[4096];
        microphone.read(buffer, 0, buffer.length);
        return buffer;
    }

    // 发送视频帧，带有时间戳
// 发送视频帧，带有会议ID和时间戳
    private void sendVideoFrame(String meetingId, BufferedImage image, long timestamp) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // 包装会议ID、时间戳和图像数据
            int totalCapacity = 20 + 4 + 8 + imageBytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(totalCapacity);  // 20字节用于会议ID，8字节用于存储时间戳
            byte[] idBytes = Arrays.copyOf(meetingId.getBytes(), 20);  // 确保会议ID是20字节
            buffer.put(idBytes);
            buffer.putInt(1);
            buffer.putLong(timestamp);  // 写入时间戳
            buffer.put(imageBytes);  // 写入图像数据

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
            udpSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 发送音频帧，带有会议ID和时间戳
    private void sendAudioFrame(String meetingId, byte[] audioData, long timestamp) {
        try {
            // 确保 ByteBuffer 的容量足够大
            int totalCapacity = 20 + 4 + 8 + audioData.length;  // 20字节用于会议ID，8字节用于存储时间戳，加上音频数据的大小
            ByteBuffer buffer = ByteBuffer.allocate(totalCapacity);  // 动态分配足够容量的缓冲区

            byte[] meetingIdBytes = meetingId.getBytes();  // 将会议ID转换为字节数组
            byte[] meetingIdPadded = Arrays.copyOf(meetingIdBytes, 20);  // 确保会议ID长度为20字节
            byte[] idBytes = Arrays.copyOf(meetingId.getBytes(), 20);  // 确保会议ID是20字节
            buffer.put(idBytes);
            buffer.putInt(1);
            buffer.putLong(timestamp);  // 写入时间戳
            buffer.put(audioData);  // 写入音频数据

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
            udpSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 设置摄像头状态
    public void setCameraStatus(boolean status) {
        this.isCameraOn = status;
    }

    // 设置麦克风状态
    public void setMicrophoneStatus(boolean status) {
        this.isMicrophoneOn = status;
    }

    // 发送加入会议的请求并启动接收线程
    public void joinMeeting(String meetingId, String serverIp, int serverPort) {
        try {
            udpSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverIp);
            this.serverPort = serverPort;

            // 发送加入会议请求
            ByteBuffer buffer = ByteBuffer.allocate(24);  // 假设会议ID为20字节 + 4字节的“加入会议”标识
            buffer.put(meetingId.getBytes());  // 会议ID
            buffer.putInt(0);  // 0表示这是加入会议的请求，而不是视频帧

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
            udpSocket.send(packet);
            System.out.println("加入会议请求已发送: " + meetingId);

            // 启动接收视频和音频的线程
            new Thread(() -> {
                try {
                    while (isRunning) {
                        byte[] bufferReceive = new byte[65535];  // 接收视频和音频的数据包
                        DatagramPacket receivePacket = new DatagramPacket(bufferReceive, bufferReceive.length);

                        udpSocket.receive(receivePacket);  // 接收数据包
                        ByteBuffer receivedBuffer = ByteBuffer.wrap(receivePacket.getData());

                        // 提取时间戳和数据
                        long timestamp = receivedBuffer.getLong();  // 提取时间戳
                        byte[] frameData = new byte[receivePacket.getLength() - 8];  // 剩余部分是视频/音频数据
                        receivedBuffer.get(frameData);

                        // 假设我们判断帧大小小于一定值的是音频，其他是视频
                        if (frameData.length > 1024) {
                            // 视频数据，显示视频帧
                            BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(frameData));
                            videoMeetingController.updateVideoFrame(receivedImage);  // 更新UI中的视频帧
                        } else {
                            // 音频数据，播放音频
                            playAudioFrame(frameData);  // 调用播放音频的方法
                        }

                        // 控制音视频同步，基于时间戳
                        long currentTime = System.currentTimeMillis();
                        long sleepTime = timestamp - currentTime;
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 播放音频帧
    private void playAudioFrame(byte[] audioData) {
        try {
            SourceDataLine line;
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            // 播放音频
            line.write(audioData, 0, audioData.length);
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 客户端退出会议的请求
    public void leaveMeeting(String meetingId, String serverIp, int serverPort) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(24);  // 假设会议ID为20字节 + 4字节的“退出会议”标识
            buffer.put(meetingId.getBytes());  // 会议ID
            buffer.putInt(-1);  // -1表示退出会议请求

            DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
            udpSocket.send(packet);
            System.out.println("退出会议请求已发送: " + meetingId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void connect(String meetingId, String serverIp, int serverPort) throws SocketException, UnknownHostException {
        udpSocket = new DatagramSocket();
        serverAddress = InetAddress.getByName(serverIp);
        this.serverPort = serverPort;
    }

    // 关闭连接，释放资源
    public void closeConnection() {
        try {
            isRunning = false;  // 停止发送线程

            if (udpSocket != null && !udpSocket.isClosed()) {
                udpSocket.close();
                System.out.println("UDP socket 已关闭");
            }

            stopCamera();
            stopMicrophone();

            System.out.println("音视频流已停止");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
