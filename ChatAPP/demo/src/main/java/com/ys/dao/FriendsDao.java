package com.ys.dao;

import com.ys.model.User;
import com.ys.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    public User searchUser(String friendId) {
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

    // 获取当前用户收到的好友请求
    public String getFriendsRequestId(String userId) {
        String query = "SELECT user_id FROM addfriends WHERE friend_id = ?";
        String friendsRequestId = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                friendsRequestId = rs.getString("user_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsRequestId;
    }

    // 获取好友请求的消息
    public String getFriendRequestMessage(String userId) {
        String query = "SELECT message FROM addfriends WHERE friend_id = ?";
        String message = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                message = rs.getString("message");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return message;
    }

    // 获取某用户的所有好友的 friend_id
    public List<String> getAllFriendsIds(String userId) {
        String query = "SELECT friend_id FROM user_friends WHERE user_id = ?";
        List<String> friendIds = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            // 添加所有的 friend_id 到列表中
            while (rs.next()) {
                friendIds.add(rs.getString("friend_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendIds;
    }

    // 根据 friend_id 获取好友的详细信息 (email 和 username)
    public List<User> getFriendDetails(List<String> friendIds) {
        List<User> friendsDetails = new ArrayList<>();
        String query = "SELECT user_id, username, email FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (String friendId : friendIds) {
                stmt.setString(1, friendId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    User friend = new User();
                    friend.setUser_id(rs.getInt("user_id"));
                    friend.setUsername(rs.getString("username"));
                    friend.setEmail(rs.getString("email"));
                    friendsDetails.add(friend);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendsDetails;
    }
    // 删除好友请求记录
    public boolean deleteFriendRequest(String userId, String friendId) {
        String query = "DELETE FROM addfriends WHERE user_id = ? AND friend_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            stmt.setString(2, friendId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 添加好友关系到 user_friends 表
    public boolean addFriendToUserFriends(String userId, String friendId) {
        String query = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            stmt.setString(2, friendId);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<String> getAllFriendRequestIds(String userId) {
        List<String> friendRequestIds = new ArrayList<>();
        String query = "SELECT user_id FROM addfriends WHERE friend_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            // 遍历结果集并将 user_id（请求发起方）添加到列表中
            while (rs.next()) {
                friendRequestIds.add(rs.getString("user_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friendRequestIds;
    }
}
