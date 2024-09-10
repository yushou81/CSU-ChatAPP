package com.ys.dao;

import com.ys.model.Avatar;
import com.ys.utils.DatabaseConnection;
import com.ys.model.Avatar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AvatarDao {

    // 添加新的用户头像
    public void addAvatar(Avatar avatar) throws SQLException {
        String query = "INSERT INTO avatar_table (user_id, avatar_path) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, avatar.getUserId());
            stmt.setString(2, avatar.getAvatarPath());

            stmt.executeUpdate();
        }
    }

    // 根据 user_id 获取用户头像
    public Avatar getAvatarByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM avatar_table WHERE user_id = ?";
        Avatar avatar = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    avatar = new Avatar();
                    avatar.setAvatarId(rs.getInt("avatar_id"));
                    avatar.setUserId(rs.getInt("user_id"));
                    avatar.setAvatarPath(rs.getString("avatar_path"));
                    avatar.setUploadTime(rs.getTimestamp("upload_time").toLocalDateTime());
                }
            }
        }

        return avatar;
    }

    // 更新用户头像路径
    public void updateAvatar(Avatar avatar) throws SQLException {
        String query = "UPDATE avatar_table SET avatar_path = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, avatar.getAvatarPath());
            stmt.setInt(2, avatar.getUserId());

            stmt.executeUpdate();
        }
    }

    // 删除用户头像
    public void deleteAvatar(int userId) throws SQLException {
        String query = "DELETE FROM avatar_table WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            stmt.executeUpdate();
        }
    }
}

