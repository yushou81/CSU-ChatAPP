package com.ys.dao;

import com.ys.model.Message;
import com.ys.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDao {

    // 存储消息
    public boolean saveMessage(Message message) {
        String query = "INSERT INTO messages (sender_id, receiver_id, team_id, message_content, message_type, file_url, sent_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setInt(3, message.getTeamId() != null ? message.getTeamId() : null);  // 群聊时才有team_id
            stmt.setString(4, message.getMessageContent());
            stmt.setString(5, message.getMessageType());
            stmt.setString(6, message.getFileUrl());
            stmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查找两个人之间的聊天记录
    public List<Message> getMessagesBetweenUsers(int senderId, int receiverId) {
        String query = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY sent_at ASC";
        List<Message> messageList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message message = new Message();
                message.setMessageId(rs.getInt("message_id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setTeamId(rs.getInt("team_id"));
                message.setMessageContent(rs.getString("message_content"));
                message.setMessageType(rs.getString("message_type"));
                message.setFileUrl(rs.getString("file_url"));
                message.setSentAt(rs.getTimestamp("sent_at"));

                messageList.add(message);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messageList;
    }
}
