package com.ys.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingRoom {
    private String meetingId;
    private String meetingName;
    private String password;
    private Set<String> participants;
    private static final int MAX_PARTICIPANTS = 10;  // 限制会议人数为10

    // 使用 set 方法初始化属性
    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getParticipants() {
        if (participants == null) {
            participants = ConcurrentHashMap.newKeySet();
        }
        return participants;
    }

    public boolean addParticipant(String userId) {
        if (getParticipants().size() < MAX_PARTICIPANTS) {
            participants.add(userId);
            return true;
        } else {
            return false;  // 会议人数已满
        }
    }

    public void removeParticipant(String userId) {
        getParticipants().remove(userId);
    }

    public boolean isFull() {
        return getParticipants().size() >= MAX_PARTICIPANTS;
    }

    public boolean isEmpty() {
        return getParticipants().isEmpty();
    }
}
