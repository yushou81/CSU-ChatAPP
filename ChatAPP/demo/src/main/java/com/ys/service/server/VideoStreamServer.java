package com.ys.service.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class VideoStreamServer {
    private ServerSocketChannel serverChannel;
    private Selector selector;

    // 存储每个会议的客户端组，会议ID作为key，连接的客户端SocketChannel作为value
    private Map<String, List<SocketChannel>> meetingsMap = new HashMap<>();

    public VideoStreamServer(int videoPort) throws IOException {
        serverChannel = ServerSocketChannel.open(); // 开启 ServerSocketChannel
        serverChannel.bind(new InetSocketAddress(videoPort)); // 绑定视频端口
        serverChannel.configureBlocking(false); // 设置为非阻塞

        // 创建 Selector，用于管理多个 Channel 的事件
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT); // 注册接受连接事件
    }

    public void start() {
        System.out.println("视频流服务器已启动，等待客户端连接...");
        new Thread(() -> {
            try {
                while (true) {
                    selector.select(); // 阻塞直到有事件准备好

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = selectedKeys.iterator();

                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();

                        if (key.isAcceptable()) {
                            handleAccept();  // 处理新的客户端连接
                        } else if (key.isReadable()) {
                            handleRead(key);  // 处理视频数据读取
                        }
                        iter.remove();  // 移除已处理的事件
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleAccept() throws IOException {
        // 接受新的客户端连接
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);  // 注册读取事件
        System.out.println("新的客户端连接，准备接收视频流");
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 读取视频数据
        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            clientChannel.close(); // 客户端关闭连接
            return;
        }

        buffer.flip();
        String meetingId = readMeetingId(buffer);  // 从 buffer 中读取会议ID
        byte[] frameData = readFrameData(buffer);  // 从 buffer 中读取视频帧数据
//        buffer.get(data);
        // 将视频帧发送给同一会议的其他客户端
        if (meetingId != null && frameData != null) {
            broadcastToMeeting(meetingId, frameData, clientChannel);
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
    // 将视频帧广播给同一会议中的其他客户端
    private void broadcastToMeeting(String meetingId, byte[] frameData, SocketChannel sender) throws IOException {
        List<SocketChannel> clients = meetingsMap.get(meetingId);  // 获取同一会议的客户端列表
        if (clients == null) {
            clients = new ArrayList<>();
            meetingsMap.put(meetingId, clients);  // 如果会议还不存在，创建一个新的会议
        }

        if (!clients.contains(sender)) {
            clients.add(sender);  // 将新加入的客户端添加到会议中
        }

        // 将视频帧广播给该会议中的其他客户端
        for (SocketChannel client : clients) {
            if (client != sender) {
                ByteBuffer buffer = ByteBuffer.allocate(4 + frameData.length);
                buffer.putInt(frameData.length);  // 视频帧大小
                buffer.put(frameData);  // 视频帧数据
                buffer.flip();
                client.write(buffer);  // 发送给其他客户端
            }
        }
    }
}

