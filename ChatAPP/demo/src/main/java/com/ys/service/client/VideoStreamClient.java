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
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class VideoStreamClient {
    private VideoMeetingController videoMeetingController;
    private DatagramSocket udpSocket;
    private OpenCVFrameGrabber videoGrabber;
    private AudioFormat audioFormat;
    private TargetDataLine microphone;
    private SourceDataLine speakers;
    private InetAddress serverAddress;
    private int serverPort;
    private boolean isCameraOn = false;//调试时关闭的
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
            if (isMicrophoneOn) {
                startMicrophone();
                openSpeaker();
            }

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
                    playAudioFrame(audioData);
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

            if (microphone != null) {
                microphone.open(audioFormat);
                microphone.start();
                System.out.println("麦克风启动成功: " + microphone.getLineInfo());
            } else {
                System.out.println("麦克风获取失败");
            }

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
        byte[] buffer = new byte[40960];  // 或者更大的值
        try {
            int bytesRead;
            bytesRead = microphone.read(buffer, 0, buffer.length);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("音频读取失败");
        }
        return buffer;
    }

    // 保存音频数据到文件
    private void saveAudioToFile() {
        try {
            System.out.println("开始录制");
            File audioFile = new File("recorded_audio.wav");
            AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

            // 使用 AudioInputStream 包装 TargetDataLine
            AudioInputStream audioStream = new AudioInputStream(microphone);

            // 录制 10 秒后保存文件
            long recordDuration = 10 * 1000; // 10秒
            long startTime = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    System.out.println("开始录音...");
                    AudioSystem.write(audioStream, fileType, audioFile);
                    while (System.currentTimeMillis() - startTime < recordDuration) {
                        // 持续录音直到到达设定的时长
                    }
                    System.out.println("录音完成，保存到文件: " + audioFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    stopMicrophone(); // 录音结束后停止麦克风
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int MAX_UDP_PACKET_SIZE = 60000;  // 定义每个数据包的最大大小，略小于65535以保证UDP传输稳定性

    // 发送视频帧，带有会议ID和时间戳
    private void sendVideoFrame(String meetingId, BufferedImage image, long timestamp) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // 拆分数据包
            int totalParts = (int) Math.ceil(imageBytes.length / (double) MAX_UDP_PACKET_SIZE);

            for (int part = 0; part < totalParts; part++) {
                int start = part * MAX_UDP_PACKET_SIZE;
                int length = Math.min(imageBytes.length - start, MAX_UDP_PACKET_SIZE);  // 当前分片大小

                System.out.println("videooparts"+part+"length"+length);

                ByteBuffer buffer = ByteBuffer.allocate(20 +4 + 4 + 4 + 8 + 4 + 4 + length);  // 20字节会议ID, 4字节标志位, 8字节时间戳, 4字节总分片数, 4字节当前分片号

                byte[] idBytes = Arrays.copyOf(meetingId.getBytes(), 20);  // 确保会议ID是20字节
                buffer.put(idBytes);
                buffer.putInt(1);
                buffer.putInt(4 + 8 + 4 + 4 + length);
                buffer.putInt(1);  // 1 表示视频数据
                buffer.putLong(timestamp);  // 写入时间戳
                buffer.putInt(totalParts);  // 总分片数
                buffer.putInt(part);  // 当前分片编号
                buffer.put(imageBytes, start, length);  // 写入当前分片数据

                DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
                udpSocket.send(packet);  // 发送当前分片数据包
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 发送音频帧，带有会议ID和时间戳
    private void sendAudioFrame(String meetingId, byte[] audioData, long timestamp) {
        try {

            // 拆分数据包
            int totalParts = (int) Math.ceil(audioData.length / (double) MAX_UDP_PACKET_SIZE);

            for (int part = 0; part < totalParts; part++) {

                int start = part * MAX_UDP_PACKET_SIZE;
                int length = Math.min(audioData.length - start, MAX_UDP_PACKET_SIZE);  // 当前分片大小
                System.out.println("length"+length);


                ByteBuffer buffer = ByteBuffer.allocate(20 + 4 + 4 + 4 + 8 + 4 + 4 + length);  // 20字节会议ID, 4字节标志位, 8字节时间戳, 4字节总分片数, 4字节当前分片号

                byte[] idBytes = Arrays.copyOf(meetingId.getBytes(), 20);  // 确保会议ID是20字节
                buffer.put(idBytes);
                buffer.putInt(1);
                buffer.putInt( 4 + 8 + 4 + 4 + length);
                buffer.putInt(2);  // 2 表示音频数据
                buffer.putLong(timestamp);  // 写入时间戳
                buffer.putInt(totalParts);  // 总分片数
                buffer.putInt(part);  // 当前分片编号
                buffer.put(audioData, start, length);  // 写入当前分片数据

                DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.capacity(), serverAddress, serverPort);
                udpSocket.send(packet);  // 发送当前分片数据包
            }
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
                    Map<Integer, byte[]> videoPartsMap = new HashMap<>();  // 存储视频分片
                    Map<Integer, byte[]> audioPartsMap = new HashMap<>();  // 存储音频分片
                    int videoTotalParts = -1;  // 视频分片总数
                    int videoPartsReceived = 0;  // 已接收到的视频分片数
                    int audioTotalParts = -1;  // 音频分片总数
                    int audioPartsReceived = 0;  // 已接收到的音频分片数
                    long currentVideoTimestamp = 0;
                    long currentAudioTimestamp = 0;

                    while (isRunning) {
                        byte[] bufferReceive = new byte[65535];  // 接收视频和音频的数据包
                        DatagramPacket receivePacket = new DatagramPacket(bufferReceive, bufferReceive.length);
                        udpSocket.receive(receivePacket);  // 接收数据包
                        ByteBuffer receivedBuffer = ByteBuffer.wrap(receivePacket.getData());

                        // 解析数据包
                        int dataType = receivedBuffer.getInt();  // 获取数据类型，1为视频，2为音频

                        long timestamp = receivedBuffer.getLong();  // 获取时间戳

                        // 视频数据
                        if (dataType == 1) {
                            if (timestamp != currentVideoTimestamp) {
                                // 如果是新的帧，重置视频分片计数
                                videoPartsMap.clear();
                                currentVideoTimestamp = timestamp;
                                videoPartsReceived = 0;
                                videoTotalParts = -1;
                            }

                            videoTotalParts = receivedBuffer.getInt();  // 视频总分片数
                            int partNumber = receivedBuffer.getInt();  // 当前视频分片号

                            byte[] partData = new byte[receivePacket.getLength() - 44];  // 计算当前分片数据长度（减去头部信息）
                            receivedBuffer.get(partData);  // 获取视频分片数据
                            videoPartsMap.put(partNumber, partData);  // 存储视频分片数据

                            videoPartsReceived++;  // 记录收到的视频分片数

                            // 当所有视频分片都接收到后，重组完整帧
                            if (videoPartsReceived == videoTotalParts) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                for (int i = 0; i < videoTotalParts; i++) {
                                    byteArrayOutputStream.write(videoPartsMap.get(i));  // 将所有视频分片合并
                                }

                                byte[] completeFrameData = byteArrayOutputStream.toByteArray();
                                BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream(completeFrameData));

                                if (receivedImage != null) {
                                    videoMeetingController.updateVideoFrame(receivedImage);  // 显示视频帧
                                } else {
                                    System.out.println("接收到的视频帧无法解析");
                                }

                                // 清空视频分片数据
                                videoPartsMap.clear();
                                videoPartsReceived = 0;
                                videoTotalParts = -1;
                            }
                        }
                        // 音频数据
                        else if (dataType == 2) {
                            if (timestamp != currentAudioTimestamp) {
                                // 如果是新的音频帧，重置音频分片计数
                                audioPartsMap.clear();
                                currentAudioTimestamp = timestamp;
                                audioPartsReceived = 0;
                                audioTotalParts = -1;
                            }

                            audioTotalParts = receivedBuffer.getInt();  // 音频总分片数
                            int partNumber = receivedBuffer.getInt();  // 当前音频分片号

                            byte[] partData = new byte[receivePacket.getLength() - 44];  // 计算当前分片数据长度（减去头部信息）
                            receivedBuffer.get(partData);  // 获取音频分片数据
                            audioPartsMap.put(partNumber, partData);  // 存储音频分片数据

                            audioPartsReceived++;  // 记录收到的音频分片数

                            // 当所有音频分片都接收到后，重组完整帧
                            if (audioPartsReceived == audioTotalParts) {
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                for (int i = 0; i < audioTotalParts; i++) {
                                    byteArrayOutputStream.write(audioPartsMap.get(i));  // 将所有音频分片合并
                                }

                                byte[] completeAudioData = byteArrayOutputStream.toByteArray();
                                playAudioFrame(completeAudioData);  // 播放音频

                                // 清空音频分片数据
                                audioPartsMap.clear();
                                audioPartsReceived = 0;
                                audioTotalParts = -1;
                            }
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

    public void openSpeaker() throws LineUnavailableException {
        System.out.println("打开扬声器");
        if (audioFormat == null) {
            audioFormat = new AudioFormat(44100, 16, 2, true, true);  // 初始化为立体声、16位、44100Hz
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        // 检查音频线是否支持该格式
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("不支持的音频格式");
            return;
        }
        speakers = (SourceDataLine) AudioSystem.getLine(info);
        speakers.open(audioFormat);  // 打开音频线
        speakers.start();
    }
    // 播放音频帧
    private void playAudioFrame(byte[] audioData) {
        try {
            int frameSize = audioFormat.getFrameSize();  // 获取每帧的大小
            int lengthToWrite = (audioData.length / frameSize) * frameSize;  // 计算符合帧大小的字节数
            // 播放音频数据，确保写入的字节数是 frameSize 的整数倍
//            speakers.write(audioData, 0, lengthToWrite);
            speakers.write(audioData, 0, audioData.length);
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
