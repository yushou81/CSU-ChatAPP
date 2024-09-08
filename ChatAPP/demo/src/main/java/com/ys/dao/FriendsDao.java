package com.ys.dao;

import com.ys.model.User;
import com.ys.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendsDao {

    // 添加好友请求
    public boolean addFriend(String userId, String friendId, String message) {
        String query = "INSERT INTO addfriends (user_id, friend_id, message) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            stmt.setString(2, friendId);
            stmt.setString(3, message);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查找好友
    public User searchFriend(String friendId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        User friend = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, friendId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                friend = new User();
                friend.setUser_id(rs.getInt("user_id"));
                friend.setUsername(rs.getString("username"));
                friend.setEmail(rs.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friend;
    }
}
