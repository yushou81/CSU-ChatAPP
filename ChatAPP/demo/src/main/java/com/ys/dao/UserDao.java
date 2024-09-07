package com.ys.dao;



import com.ys.model.User;
import com.ys.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    // 用户注册
    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 用户登录
    public User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setEmail(rs.getString("email"));
                return user;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 查找好友联系人（通过用户ID查找）
    public List<User> findContactsByUserId(int userId) {
        List<User> contacts = new ArrayList<>();
        String query = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
//                user.setPhoneNumber(rs.getString("phone_number"));
//                user.setRole(rs.getString("role"));
//                user.setProfilePicture(rs.getString("profile_picture"));
                contacts.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contacts;
    }

    // 根据用户ID查找单个用户
    public User findUserById(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";
        User user = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
//                user.setPhoneNumber(rs.getString("phone_number"));
//                user.setRole(rs.getString("role"));
//                user.setProfilePicture(rs.getString("profile_picture"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    // 添加好友
    public boolean addFriend(int userId, int friendId) {
        String query = "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取好友列表
    public List<User> getFriends(int userId) {
        List<User> friends = new ArrayList<>();
        String query = "SELECT u.* FROM users u JOIN user_friends uf ON u.user_id = uf.friend_id WHERE uf.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
//                user.setPhoneNumber(rs.getString("phone_number"));
//                user.setRole(rs.getString("role"));
//                user.setProfilePicture(rs.getString("profile_picture"));
                friends.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return friends;
    }

}
