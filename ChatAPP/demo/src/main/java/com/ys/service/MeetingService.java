package com.ys.service;

import com.ys.dao.MeetingDao;
import com.ys.model.MeetingRoom;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingService {
    private static Map<String, MeetingRoom> meetingRooms = new ConcurrentHashMap<>();
    private MeetingDao meetingDao;

    public MeetingService(MeetingDao meetingDao) {
        this.meetingDao = meetingDao;
    }

    // 创建会议并保存到数据库，返回服务器生成的 meeting_id
    public String createMeeting(String meetingName, String password, int createdBy) {
        MeetingRoom meetingRoom = new MeetingRoom();
        meetingRoom.setMeetingName(meetingName);
        meetingRoom.setPassword(password);

        // 将会议信息存储到数据库
        com.ys.model.Meeting meeting = new com.ys.model.Meeting();
        meeting.setTeamId(1);  // 示例 team_id
        meeting.setMeetingName(meetingName);
        meeting.setPassword(password);
        meeting.setScheduledTime(new Timestamp(System.currentTimeMillis()));
        meeting.setCreatedBy(createdBy);
        meeting.setMeetingLink(null);  // 示例会议链接

        // 通过数据库生成 meeting_id
        int meetingId = meetingDao.saveMeeting(meeting);
        if (meetingId != -1) {
            meetingRoom.setMeetingId(String.valueOf(meetingId));
            meetingRooms.put(String.valueOf(meetingId), meetingRoom);
            return String.valueOf(meetingId);
        } else {
            return null;  // 失败时返回 null
        }
    }

    // 加入会议，验证密码和人数限制
    public boolean joinMeeting(String meetingId, String userId, String password) {
        MeetingRoom meetingRoom = meetingRooms.get(meetingId);
        if (meetingRoom != null) {
            if (!meetingRoom.getPassword().equals(password)) {
                return false;  // 密码错误
            }
            if (meetingRoom.isFull()) {
                return false;  // 会议已满
            }
            return meetingRoom.addParticipant(userId);
        } else {
            return false;  // 会议不存在
        }
    }

    // 离开会议
    public void leaveMeeting(String meetingId, String userId) {
        MeetingRoom meetingRoom = meetingRooms.get(meetingId);
        if (meetingRoom != null) {
            meetingRoom.removeParticipant(userId);
            if (meetingRoom.isEmpty()) {
                meetingRooms.remove(meetingId);  // 如果会议为空，则移除会议
            }
        }
    }

    // 获取会议
    public MeetingRoom getMeeting(String meetingId) {
        return meetingRooms.get(meetingId);
    }
}
