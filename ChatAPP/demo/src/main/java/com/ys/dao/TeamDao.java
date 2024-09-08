package com.ys.dao;

import com.ys.model.Team;
import com.ys.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeamDao {

    // 创建团队
    public boolean createTeam(int createdBy, String teamName) {
        String query = "INSERT INTO teams (team_name, created_by) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teamName);
            stmt.setInt(2, createdBy);
            // 打印 SQL 执行状态
            System.out.println("Executing SQL: " + query);
            System.out.println("Parameters: teamName = " + teamName + ", createdBy = " + createdBy);

            int rowsInserted = stmt.executeUpdate();

            System.out.println("Rows inserted: " + rowsInserted);
            return rowsInserted > 0;

        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 加入团队
    public boolean joinTeam(int userId, String teamName) {
        // 查询团队 ID 的 SQL 语句
        String selectTeamIdQuery = "SELECT team_id FROM teams WHERE team_name = ?";
        // 插入团队成员的 SQL 语句
        String insertTeamMemberQuery = "INSERT INTO team_members (team_id, user_id, role) VALUES (?, ?, 'member')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectTeamIdQuery)) {

            // 查询团队 ID
            selectStmt.setString(1, teamName);
            ResultSet rs = selectStmt.executeQuery();

            // 如果团队存在
            if (rs.next()) {
                int teamId = rs.getInt("team_id");

                // 使用获取到的团队 ID 插入团队成员
                try (PreparedStatement insertStmt = conn.prepareStatement(insertTeamMemberQuery)) {
                    insertStmt.setInt(1, teamId);
                    insertStmt.setInt(2, userId);

                    int rowsInserted = insertStmt.executeUpdate();

                    return rowsInserted > 0;
                }
            } else {
                // 团队不存在
                System.err.println("Team with name '" + teamName + "' does not exist.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 获取团队成员
    public List<Integer> getTeamMembers(String teamName) {
        List<Integer> members = new ArrayList<>();
        String query = "SELECT user_id FROM team_members WHERE team_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teamName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                members.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return members;
    }

    // 获取团队信息
    public Team getTeamInfo(String teamName) {
        String query = "SELECT * FROM teams WHERE team_name = ?";
        Team team = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, teamName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                team = new Team();
                team.setTeamId(rs.getInt("team_id"));
                team.setTeamName(rs.getString("team_name"));
                team.setCreateBy(rs.getString("created_by")); // 根据实际数据类型调整
                team.setCreateAt(rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return team;
    }
}
