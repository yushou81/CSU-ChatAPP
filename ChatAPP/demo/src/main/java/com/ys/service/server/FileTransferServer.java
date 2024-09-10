package com.ys.service.server;

import com.ys.dao.FileDao;
import com.ys.dao.MessageDao;
import com.ys.model.FileEntity;
import com.ys.model.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileTransferServer implements Runnable {

    private static final int FILE_PORT = 6666;  // 文件传输端口
    private static final String UPLOAD_DIR = "uploads/";  // 文件保存目录
    private static Map<String, Socket> userSockets = new ConcurrentHashMap<>();  // 保存用户的Socket

    private FileDao fileDao = new FileDao();  // 数据库操作类
    private MultiClientServerWithThreadPool multiClientServerWithThreadPool;

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(FILE_PORT)) {
            System.out.println("File Transfer Server is running on port " + FILE_PORT);

            // 如果文件夹不存在则创建
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdir();
            }

            while (true) {
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                System.out.println("File transfer client connected");

                // 启动新线程处理每个客户端的文件请求
                new Thread(() -> handleFile(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setMultiClientServerWithThreadPool(MultiClientServerWithThreadPool multiClientServerWithThreadPool){
        this.multiClientServerWithThreadPool =multiClientServerWithThreadPool;
    }

    // 处理文件请求的方法，读取传来的第一个 int 值
    private void handleFile(Socket socket) {
        try (DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {
            int action = dataIn.readInt();  // 1 = 上传文件，2 = 下载文件
            String userId = dataIn.readUTF();  // 读取用户ID
            String receiverId = dataIn.readUTF();
            if (action == 1) {
                // 处理文件上传
                String fileName = dataIn.readUTF();  // 文件名
                long fileSize = dataIn.readLong();  // 文件大小
                String fileType = dataIn.readUTF();  // 文件类型
                String filePath = UPLOAD_DIR + fileName;  // 文件路径

                receiveFile(socket, fileName);  // 接收文件内容

                MessageDao messageDao = new MessageDao();
                //保存消息
                Message msg = new Message();
                msg.setFileUrl(filePath);
                msg.setMessageContent(fileName);
                msg.setSenderId(Integer.parseInt(userId));
                msg.setReceiverId(Integer.parseInt(receiverId));
                msg.setMessageType("file");  // 假设这里为文本类型
                messageDao.saveMessage(msg);
                multiClientServerWithThreadPool.sendPrivateMessage(receiverId," 发送者ID: " + msg.getSenderId() + " 内容: " + msg.getMessageContent()+"消息类型: "+msg.getMessageType()+"文件地址："+msg.getFileUrl());

                // 保存文件信息到数据库
                FileEntity fileEntity = new FileEntity();
                fileEntity.setUserId(Integer.parseInt(userId));
                fileEntity.setFileName(fileName);
                fileEntity.setFileSize(fileSize);
                fileEntity.setFileType(fileType);
                fileEntity.setFilePath(filePath);
                fileDao.addFile(fileEntity);  // 保存文件信息到数据库
            } else if (action == 2) {
                // 处理文件下载
                String fileName = dataIn.readUTF();  // 文件名
                sendFile(socket, fileName);  // 发送文件内容
            }

            // 将用户的Socket加入到userSockets
            userSockets.put(userId, socket);

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    // 接收文件
    private void receiveFile(Socket socket, String fileName) throws IOException {
        InputStream inputStream = socket.getInputStream();
        File file = new File(UPLOAD_DIR + fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("File received: " + fileName);
    }

    // 发送文件
// 发送文件
    private void sendFile(Socket socket, String fileName) throws IOException {
        try (DataInputStream dataIn = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {

            // 接收客户端发送的文件名
            File file = new File(UPLOAD_DIR + fileName);

            // 检查文件是否存在
            if (file.exists() && file.isFile()) {
                System.out.println("Sending file: " + fileName);

                // 发送文件大小
                dataOut.writeLong(file.length());

                // 发送文件内容
                try (FileInputStream fileIn = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileIn.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, bytesRead);
                    }
                    dataOut.flush();
                    System.out.println("File sent successfully");
                }
            } else {
                // 文件不存在，发送-1表示错误
                System.out.println("File not found: " + fileName);
                dataOut.writeLong(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handleFileRequest(Socket socket) {
        try (DataInputStream dataIn = new DataInputStream(socket.getInputStream());
             ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream())) {

            int action = dataIn.readInt();  // 读取操作类型，3代表获取文件列表
            if (action == 3) {
                int userId = dataIn.readInt();  // 读取用户ID
                List<FileEntity> fileList = fileDao.getFilesByUserId(userId);  // 从数据库获取文件列表

                // 将文件列表发送给客户端
                objOut.writeObject(fileList);
                objOut.flush();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }



}
