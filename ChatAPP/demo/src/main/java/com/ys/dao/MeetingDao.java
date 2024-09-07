package com.ys.dao;

import com.ys.model.Meeting;
import com.ys.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetingDao {

    // 存储会议，自动生成 meeting_id 并返回该 id
    public int saveMeeting(Meeting meeting) {
        String query = "INSERT INTO meetings (team_id, meeting_name, password, scheduled_time, created_by, meeting_link) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, meeting.getTeamId());
            stmt.setString(2, meeting.getMeetingName());
            stmt.setString(3, meeting.getPassword());
            stmt.setTimestamp(4, meeting.getScheduledTime());
            stmt.setInt(5, meeting.getCreatedBy());
            stmt.setString(6, meeting.getMeetingLink());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                // 获取生成的 meeting_id
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);  // 返回自动生成的 meeting_id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // 返回 -1 表示插入失败
    }

    // 根据ID查找会议
    public Meeting getMeetingById(int meetingId) {
        String query = "SELECT * FROM meetings WHERE meeting_id = ?";
        Meeting meeting = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, meetingId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                meeting = new Meeting();
                meeting.setMeetingId(rs.getInt("meeting_id"));
                meeting.setTeamId(rs.getInt("team_id"));
                meeting.setMeetingName(rs.getString("meeting_name"));
                meeting.setPassword(rs.getString("password"));
                meeting.setScheduledTime(rs.getTimestamp("scheduled_time"));
                meeting.setCreatedBy(rs.getInt("created_by"));
                meeting.setMeetingLink(rs.getString("meeting_link"));
                meeting.setCreatedAt(rs.getTimestamp("created_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return meeting;
    }

    // 获取所有会议
    public List<Meeting> getAllMeetings() {
        String query = "SELECT * FROM meetings ORDER BY scheduled_time ASC";
        List<Meeting> meetingList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Meeting meeting = new Meeting();
                meeting.setMeetingId(rs.getInt("meeting_id"));
                meeting.setTeamId(rs.getInt("team_id"));
                meeting.setMeetingName(rs.getString("meeting_name"));
                meeting.setPassword(rs.getString("password"));
                meeting.setScheduledTime(rs.getTimestamp("scheduled_time"));
                meeting.setCreatedBy(rs.getInt("created_by"));
                meeting.setMeetingLink(rs.getString("meeting_link"));
                meeting.setCreatedAt(rs.getTimestamp("created_at"));

                meetingList.add(meeting);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return meetingList;
    }

    // 更新会议
    public boolean updateMeeting(Meeting meeting) {
        String query = "UPDATE meetings SET team_id = ?, meeting_name = ?, password = ?, scheduled_time = ?, created_by = ?, meeting_link = ? WHERE meeting_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, meeting.getTeamId());
            stmt.setString(2, meeting.getMeetingName());
            stmt.setString(3, meeting.getPassword());
            stmt.setTimestamp(4, meeting.getScheduledTime());
            stmt.setInt(5, meeting.getCreatedBy());
            stmt.setString(6, meeting.getMeetingLink());
            stmt.setInt(7, meeting.getMeetingId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除会议
    public boolean deleteMeeting(int meetingId) {
        String query = "DELETE FROM meetings WHERE meeting_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, meetingId);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
