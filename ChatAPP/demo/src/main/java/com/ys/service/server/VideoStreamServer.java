package com.ys.service.server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class VideoStreamServer {
    private DatagramSocket udpSocket;
    private Map<String, List<ClientInfo>> meetingsMap = new HashMap<>();  // 会议ID -> 客户端列表
    private int bufferSize = 65535;  // UDP数据包的最大尺寸

    public VideoStreamServer(int videoPort) throws IOException {
        udpSocket = new DatagramSocket(videoPort);  // 创建UDP服务器Socket
        System.out.println("视频流服务器已启动，等待客户端连接...");
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] buffer = new byte[bufferSize];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // 接收来自客户端的视频数据包
                    udpSocket.receive(packet);
                    handlePacket(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 处理收到的数据包
    private void handlePacket(DatagramPacket packet) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        // 读取会议ID
        String meetingId = readMeetingId(byteBuffer);
        // 读取标志位，用于判断是加入会议的请求还是视频帧数据
        int packetType = byteBuffer.getInt();  // 0 表示加入会议，1 表示视频数据

        if (packetType == 0) {
            // 处理加入会议的请求
            System.out.println("有人加入meetingid："+meetingId);
            handleJoinMeeting(meetingId, clientAddress, clientPort);
        } else if (packetType == 1) {
            // 处理视频帧数据
            byte[] frameData = readFrameData(byteBuffer);
            System.out.println("ByteBufferfram 剩余数据长度: " + frameData.length);
            // 检查 byteBuffer 中是否还有未读取的数据
//            System.out.println("剩余数据长度: " + byteBuffer.remaining());

            if (frameData != null) {
                broadcastToMeeting(meetingId, frameData, clientAddress, clientPort);
            }
        } else if (packetType == -1) {
            handleLeaveMeeting(meetingId, clientAddress, clientPort);
        }
    }
    // 处理加入会议请求
    private void handleJoinMeeting(String meetingId, InetAddress clientAddress, int clientPort) {
        List<ClientInfo> clients = meetingsMap.get(meetingId);

        if (clients == null) {
            System.out.println("meeting没人");
            clients = new ArrayList<>();
            meetingsMap.put(meetingId, clients);  // 创建新的会议
        }

        // 检查是否已经加入会议，如果未加入则添加
        Optional<ClientInfo> clientInfo = clients.stream()
                .filter(client -> client.getAddress().equals(clientAddress) && client.getPort() == clientPort)
                .findFirst();

        if (!clientInfo.isPresent()) {
            clients.add(new ClientInfo(clientAddress, clientPort));  // 新的客户端加入会议
            System.out.println("客户端加入会议: " + meetingId + " 地址: " + clientAddress + ":" + clientPort);
        }
    }

    // 读取会议ID的方法
    private String readMeetingId(ByteBuffer buffer) {
        byte[] idBytes = new byte[20];  // 假设会议ID不超过20字节
        buffer.get(idBytes);
        return new String(idBytes).trim();  // 返回会议ID
    }

    // 读取视频帧数据的方法
    private byte[] readFrameData(ByteBuffer buffer) {
        int frameSize = buffer.getInt();  // 读取帧的大小
        byte[] frameData = new byte[frameSize];
        buffer.get(frameData);  // 获取视频帧数据
        return frameData;
    }

    // 广播视频帧到同一会议中的其他客户端
    private void broadcastToMeeting(String meetingId, byte[] frameData, InetAddress senderAddress, int senderPort) throws IOException {
        List<ClientInfo> clients = meetingsMap.get(meetingId);
        if (clients == null) {
            clients = new ArrayList<>();
            meetingsMap.put(meetingId, clients);  // 创建新的会议
        }

        // 检查发送者是否已经加入会议，如果未加入则添加到会议中
        Optional<ClientInfo> senderInfo = clients.stream()
                .filter(client -> client.getAddress().equals(senderAddress) && client.getPort() == senderPort)
                .findFirst();

        if (!senderInfo.isPresent()) {
            clients.add(new ClientInfo(senderAddress, senderPort));  // 新的客户端加入会议
        }
        int i = 0;
        // 广播给会议中的其他客户端
        for (ClientInfo client : clients) {
            if (!client.getAddress().equals(senderAddress) || client.getPort() != senderPort) {

                System.out.println("发给音视频给用户："+i);
                i++;

                // 直接转发客户端发送的分块数据
                DatagramPacket packet = new DatagramPacket(frameData, frameData.length, client.getAddress(), client.getPort());
                udpSocket.send(packet);  // 转发数据包
            }
        }
    }
    // 服务端处理退出会议请求
    private void handleLeaveMeeting(String meetingId, InetAddress clientAddress, int clientPort) {
        List<ClientInfo> clients = meetingsMap.get(meetingId);
        if (clients != null) {
            clients.removeIf(client -> client.getAddress().equals(clientAddress) && client.getPort() == clientPort);
            System.out.println("客户端退出会议: " + meetingId + " 地址: " + clientAddress + ":" + clientPort);
        }
    }
    // 存储客户端信息
    private static class ClientInfo {
        private InetAddress address;
        private int port;

        public ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }
}
