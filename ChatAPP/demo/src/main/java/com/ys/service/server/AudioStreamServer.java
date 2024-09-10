package com.ys.service.server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class AudioStreamServer {
    private DatagramSocket udpSocket;
    private Map<String, List<ClientInfo>> meetingsMap = new HashMap<>();
    private int bufferSize = 65535;

    public AudioStreamServer(int audioPort) throws IOException {
        udpSocket = new DatagramSocket(audioPort);
        System.out.println("音频服务端启动，端口：" + audioPort);
    }

    public void start() {
        new Thread(() -> {
            try {
                while (true) {
                    byte[] buffer = new byte[bufferSize];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    handlePacket(packet);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handlePacket(DatagramPacket packet) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
        InetAddress clientAddress = packet.getAddress();
        int clientPort = packet.getPort();

        // 读取会议ID
        String meetingId = readMeetingId(buffer);
        // 读取包类型 (0=加入会议, 1=音频数据)
        int packetType = buffer.getInt();

        if (packetType == 0) {
            handleJoinMeeting(meetingId, clientAddress, clientPort);
        } else if (packetType == 2) {
            byte[] audioData = new byte[packet.getLength() - 24];
            buffer.get(audioData);
            broadcastToMeeting(meetingId, audioData, clientAddress, clientPort);
        } else if (packetType == -1) {
            handleLeaveMeeting(meetingId, clientAddress, clientPort);
        }
    }

    private String readMeetingId(ByteBuffer buffer) {
        byte[] idBytes = new byte[20];
        buffer.get(idBytes);
        return new String(idBytes).trim();
    }

    private void handleJoinMeeting(String meetingId, InetAddress clientAddress, int clientPort) {
        meetingsMap.computeIfAbsent(meetingId, k -> new ArrayList<>()).add(new ClientInfo(clientAddress, clientPort));
        System.out.println("客户端加入会议: " + meetingId);
    }

    private void handleLeaveMeeting(String meetingId, InetAddress clientAddress, int clientPort) {
        List<ClientInfo> clients = meetingsMap.get(meetingId);
        if (clients != null) {
            clients.removeIf(client -> client.getAddress().equals(clientAddress) && client.getPort() == clientPort);
            System.out.println("客户端退出会议: " + meetingId);
        }
    }

    private void broadcastToMeeting(String meetingId, byte[] audioData, InetAddress senderAddress, int senderPort) throws IOException {
        List<ClientInfo> clients = meetingsMap.get(meetingId);
        if (clients == null) return;

        for (ClientInfo client : clients) {
            if (!client.getAddress().equals(senderAddress) || client.getPort() != senderPort) {
                DatagramPacket packet = new DatagramPacket(audioData, audioData.length, client.getAddress(), client.getPort());
                udpSocket.send(packet);
            }
        }
    }

    private static class ClientInfo {
        private final InetAddress address;
        private final int port;

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
