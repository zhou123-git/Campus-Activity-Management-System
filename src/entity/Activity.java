package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Activity {
    private String activityId;
    private String activityName;
    private String description;
    private String publisherId;
    private int maxNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long publishedAt;
    private String status; // 活动状态: pending(未审核) approved(已通过) rejected(未通过)
    private String location; // 活动地点

    public Activity(String activityId, String activityName, String description, String publisherId, int maxNum, LocalDateTime startTime, LocalDateTime endTime, long publishedAt) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.publisherId = publisherId;
        this.maxNum = maxNum;
        this.startTime = startTime;
        this.endTime = endTime;
        this.publishedAt = publishedAt;
        this.status = "approved"; // 默认为已通过状态
    }
    
    public Activity(String activityId, String activityName, String description, String publisherId, int maxNum, LocalDateTime startTime, LocalDateTime endTime, long publishedAt, String status) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.publisherId = publisherId;
        this.maxNum = maxNum;
        this.startTime = startTime;
        this.endTime = endTime;
        this.publishedAt = publishedAt;
        this.status = status;
    }

    public String getActivityId() { return activityId; }
    public String getActivityName() { return activityName; }
    public String getDescription() { return description; }
    public String getPublisherId() { return publisherId; }
    public int getMaxNum() { return maxNum; }
    public void setMaxNum(int n) { maxNum = n; }
    public LocalDateTime getStartTime(){ return startTime; }
    public void setStartTime(LocalDateTime t){ startTime = t; }
    public LocalDateTime getEndTime(){ return endTime; }
    public void setEndTime(LocalDateTime t){ endTime = t; }
    public long getPublishedAt(){ return publishedAt; }
    public void setPublishedAt(long p){ publishedAt = p; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    // 为兼容前端显示，提供字符串格式的时间
    public String getStartTimeString() {
        return startTime != null ? startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }
    
    public String getEndTimeString() {
        return endTime != null ? endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
    }

    public void setActivityName(String activityName) { this.activityName = activityName; }
    public void setDescription(String description) { this.description = description; }
}