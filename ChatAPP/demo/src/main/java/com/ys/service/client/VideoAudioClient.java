package com.ys.service.client;


import com.ys.controller.VideoMeetingController;
import org.bytedeco.javacv.*;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.ByteBuffer;

public class VideoAudioClient {
    private DatagramSocket videoSocket;
    private DatagramSocket audioSocket;
    private InetAddress serverAddress;
    private int videoPort;
    private int audioPort;
    private boolean isRunning = true;
    private SourceDataLine speakers;
    private VideoMeetingController videoMeetingController;


    private TargetDataLine microphone;
    private OpenCVFrameGrabber videoGrabber;

    public void setVideoMeetingController(VideoMeetingController controller) {
        this.videoMeetingController = controller;
    }

    public VideoAudioClient(String serverIp, int videoPort, int audioPort) throws Exception {
        this.serverAddress = InetAddress.getByName(serverIp);
        this.videoPort = videoPort;
        this.audioPort = audioPort;

        videoSocket = new DatagramSocket();
        audioSocket = new DatagramSocket();
    }

    public void start(String meetingId) {
        new Thread(() -> sendVideo(meetingId)).start();
        new Thread(() -> sendAudio(meetingId)).start();
        new Thread(() -> receiveVideo(meetingId)).start();
        new Thread(() -> receiveAudio(meetingId)).start();
    }

    private void sendVideo(String meetingId) {
        try {
            videoGrabber = new OpenCVFrameGrabber(0);
            videoGrabber.start();

            while (isRunning) {
                Frame frame = videoGrabber.grab();
                if (frame != null) {
                    BufferedImage bufferedImage = new Java2DFrameConverter().convert(frame);

                    //本地显示
                    videoMeetingController.updateLocalVideoFrame(bufferedImage);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "jpg", baos);
                    byte[] videoData = baos.toByteArray();

                    sendPacket(videoSocket, videoData, meetingId, videoPort, 1);
                }
                Thread.sleep(50);  // 控制帧率
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAudio(String meetingId) {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[4096];
            while (isRunning) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byte[] audioData = new byte[bytesRead];
                    System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                    sendPacket(audioSocket, audioData, meetingId, audioPort, 2);
                }
                Thread.sleep(50);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveVideo(String meetingId) {
        try {
            while (isRunning) {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                videoSocket.receive(packet);

                ByteBuffer receivedBuffer = ByteBuffer.wrap(packet.getData());
                long timestamp = receivedBuffer.getLong(24);  // 读取时间戳
                byte[] videoData = new byte[packet.getLength() - 32];  // 视频数据
                receivedBuffer.get(videoData);

                long currentTime = System.currentTimeMillis();
                long delay = timestamp - currentTime;  // 计算当前时间与时间戳的差异

                if (delay > 0) {
                    Thread.sleep(delay);  // 延时播放，使得音视频同步
                }
                // 播放视频...
                BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(videoData));
                videoMeetingController.updateVideoFrame(receivedImage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveAudio(String meetingId) {
        try {
            openSpeaker();
            while (isRunning) {
                byte[] buffer = new byte[65535];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                audioSocket.receive(packet);

                ByteBuffer receivedBuffer = ByteBuffer.wrap(packet.getData());
                long timestamp = receivedBuffer.getLong(24);  // 读取时间戳
                byte[] audioData = new byte[packet.getLength() - 32];  // 音频数据
                receivedBuffer.get(audioData);

                long currentTime = System.currentTimeMillis();
                long delay = timestamp - currentTime;  // 计算延迟时间

                if (delay > 0) {
                    Thread.sleep(delay);  // 延时播放音频帧
                }
                // 播放音频...
                playAudioFrame(audioData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPacket(DatagramSocket socket, byte[] data, String meetingId, int port, int type) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(32 + data.length);  // 32字节包含会议ID、数据类型和时间戳
        buffer.put(meetingId.getBytes());
        buffer.putInt(type);
        buffer.putLong(System.currentTimeMillis());  // 将当前时间戳添加到数据包
        buffer.put(data);

        DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, port);
        socket.send(packet);
    }
    public void openSpeaker() throws LineUnavailableException {
        System.out.println("打开扬声器");

        AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);  // 初始化为立体声、16位、44100Hz

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        // 检查音频线是否支持该格式
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("不支持的音频格式");
            return;
        }
        speakers = (SourceDataLine) AudioSystem.getLine(info);
        if (speakers != null) {
            speakers.open(audioFormat);  // 打开音频线
            speakers.start();
            System.out.println("扬声器启动成功: " + speakers.getLineInfo());
        } else {
            System.out.println("无法获取扬声器设备");
        }
    }
    private void playAudioFrame(byte[] audioData) {
        try {
             AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);  // 初始化为立体声、16位、44100Hz

            int frameSize = audioFormat.getFrameSize();  // 获取每帧的大小
            int lengthToWrite = (audioData.length / frameSize) * frameSize;  // 计算符合帧大小的字节数
            // 播放音频数据，确保写入的字节数是 frameSize 的整数倍
//            speakers.write(audioData, 0, lengthToWrite);
            speakers.write(audioData, 0, audioData.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
