package com.ys.service.client;

import java.io.*;
import java.net.Socket;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.ys.model.FileEntity;
import javafx.scene.control.ListView;

public class FileClient {
    private static final String SERVER_ADDRESS = "100.64.83.48";
    private static final int FILE_PORT = 6666;  // 文件传输端口

    // 获取文件类型的方法
    private static String getFileType(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "unknown" : fileName.substring(dotIndex + 1);
    }
    public static void sendFile(String userId,String receiverId, File file) {
        try (Socket socket = new Socket(SERVER_ADDRESS, FILE_PORT)) {
            System.out.println("Connected to file server for file upload");

            // 获取文件名、文件大小、文件类型和文件路径
            String fileName = file.getName();
            long fileSize = file.length();
            String fileType = getFileType(file);
            String filePath = file.getParent();

            // 发送文件的元数据
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeInt(1);
            dataOut.writeUTF(userId);  // 发送用户ID
            dataOut.writeUTF(receiverId);
            dataOut.writeUTF(fileName);  // 发送文件名
            dataOut.writeLong(fileSize);  // 发送文件大小
            dataOut.writeUTF(fileType);  // 发送文件类型
            dataOut.writeUTF(filePath);  // 发送文件目录

            // 发送文件内容
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 BufferedOutputStream bufferedOut = new BufferedOutputStream(socket.getOutputStream())) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    bufferedOut.write(buffer, 0, bytesRead);
                }
                bufferedOut.flush();
            }

            System.out.println("File uploaded: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 从服务器下载文件
    public static void receiveFileAsync(String userId, String fileName, String saveDir) {
        // 创建一个新线程来处理文件接收
        new Thread(() -> {
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir();
            }
            try (Socket socket = new Socket(SERVER_ADDRESS, FILE_PORT);
                 DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {
                System.out.println("Connected to file server for file download");

                // 发送下载请求
                dataOut.writeInt(2);  // 2 表示下载文件
                dataOut.writeUTF(userId);  // 发送用户ID
                dataOut.writeUTF(userId);
                dataOut.writeUTF(fileName);  // 发送文件名

                long fileSize = dataIn.readLong();  // 接收文件大小
                System.out.println("File size to be downloaded: " + fileSize);


                if (fileSize > 0) {
                    System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

                    // 准备接收文件
                    File file = new File(saveDir + File.separator + fileName);
                    try (FileOutputStream fileOut = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;

                        // 读取文件内容并写入本地文件
                        while (totalBytesRead < fileSize && (bytesRead = dataIn.read(buffer)) != -1) {
                            fileOut.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }

                        System.out.println("File received: " + fileName);
                    }
                } else {
                    System.out.println("File not found on server.");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();  // 启动线程
    }
    public static void fetchFileList(int userId, ListView<FileEntity> fileListView) {
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, FILE_PORT);
                 DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                 ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream())) {

                // 发送请求获取文件列表
                dataOut.writeInt(3);  // 操作3：获取文件列表
                dataOut.writeInt(userId);  // 传递用户ID
                dataOut.flush();

                // 接收文件列表
                List<FileEntity> fileList = (List<FileEntity>) objIn.readObject();

                // 更新JavaFX界面
                ObservableList<FileEntity> observableFileList = FXCollections.observableArrayList(fileList);
                fileListView.setItems(observableFileList);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }


}
