package com.ys.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TeamDao {

    // 数据库连接设置
    private static final String URL = "jdbc:mysql://localhost:3306/chatapp"; // 修改为您的数据库URL
    private static final String USER = "root"; // 数据库用户名
    private static final String PASSWORD = "123456"; // 数据库密码

    public boolean createTeam(String userID, String teamName) {
        // SQL 语句
        String sql = "INSERT INTO teams (team_name, created_by) VALUES (?, ?)";

        // 获取数据库连接
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 设置参数
            pstmt.setString(1, teamName);
            pstmt.setInt(2, Integer.parseInt(userID));

            // 执行 SQL 语句
            int rowsAffected = pstmt.executeUpdate();

            // 检查是否插入成功
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean joinTeam(String userID, String teamName) {
        // SQL 语句：插入 team_members 表
        String insertMemberSql = "INSERT INTO team_members (team_name, user_id, role) VALUES (?, ?, 'member')";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement insertMemberStmt = conn.prepareStatement(insertMemberSql)) {

            // 将用户加入到团队
            insertMemberStmt.setString(1, teamName);
            insertMemberStmt.setInt(2, Integer.parseInt(userID));

            int rowsAffected = insertMemberStmt.executeUpdate();

            // 检查是否插入成功
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



}
