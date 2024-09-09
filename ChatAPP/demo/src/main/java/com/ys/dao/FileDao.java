package com.ys.dao;

import com.ys.utils.DatabaseConnection;
import com.ys.model.FileEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FileDao {
    // 添加新文件
    public void addFile(FileEntity file) throws SQLException {
        String query = "INSERT INTO file_table (user_id, file_name, file_size, file_type, file_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, file.getUserId());
            stmt.setString(2, file.getFileName());
            stmt.setLong(3, file.getFileSize());
            stmt.setString(4, file.getFileType());
            stmt.setString(5, file.getFilePath());

            stmt.executeUpdate();
        }
    }

    // 根据 file_id 获取文件
    public FileEntity getFileById(int fileId) throws SQLException {
        String query = "SELECT * FROM file_table WHERE file_id = ?";
        FileEntity file = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, fileId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    file = new FileEntity();
                    file.setFileId(rs.getInt("file_id"));
                    file.setUserId(rs.getInt("user_id"));
                    file.setFileName(rs.getString("file_name"));
                    file.setFileSize(rs.getLong("file_size"));
                    file.setFileType(rs.getString("file_type"));
                    file.setFilePath(rs.getString("file_path"));
                    file.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
                }
            }
        }


        return file;
    }

    // 根据 user_id 获取文件
    public FileEntity getFileByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM file_table WHERE user_id = ?";
        FileEntity file = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    file = new FileEntity();
                    file.setFileId(rs.getInt("file_id"));
                    file.setUserId(rs.getInt("user_id"));
                    file.setFileName(rs.getString("file_name"));
                    file.setFileSize(rs.getLong("file_size"));
                    file.setFileType(rs.getString("file_type"));
                    file.setFilePath(rs.getString("file_path"));
                    file.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
                }
            }
        }


        return file;
    }

    // 更新文件信息
    public void updateFile(FileEntity file) throws SQLException {
        String query = "UPDATE file_table SET file_name = ?, file_size = ?, file_type = ?, file_path = ? WHERE file_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, file.getFileName());
            stmt.setLong(2, file.getFileSize());
            stmt.setString(3, file.getFileType());
            stmt.setString(4, file.getFilePath());
            stmt.setInt(5, file.getFileId());

            stmt.executeUpdate();
        }
    }

    // 删除文件
    public void deleteFile(int fileId) throws SQLException {
        String query = "DELETE FROM file_table WHERE file_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, fileId);

            stmt.executeUpdate();
        }
    }
}
