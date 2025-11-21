package entity;

public class Activity {
    private String activityId;
    private String activityName;
    private String description;
    private String publisherId;
    private int maxNum;
    private String eventTime;
    private String startTime;
    private String endTime;
    private long publishedAt;
    private String status; // 活动状态: pending(未审核) approved(已通过) rejected(未通过)

    public Activity(String activityId, String activityName, String description, String publisherId, int maxNum, String eventTime) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.publisherId = publisherId;
        this.maxNum = maxNum;
        this.eventTime = eventTime;
        this.status = "approved"; // 默认为已通过状态
    }
    public Activity(String activityId, String activityName, String description, String publisherId, int maxNum, String eventTime, String startTime, String endTime, long publishedAt) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.publisherId = publisherId;
        this.maxNum = maxNum;
        this.eventTime = eventTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.publishedAt = publishedAt;
        this.status = "approved"; // 默认为已通过状态
    }
    
    public Activity(String activityId, String activityName, String description, String publisherId, int maxNum, String eventTime, String startTime, String endTime, long publishedAt, String status) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.description = description;
        this.publisherId = publisherId;
        this.maxNum = maxNum;
        this.eventTime = eventTime;
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
    public String getEventTime(){ return eventTime; }
    public void setEventTime(String t){ eventTime = t; }
    public String getStartTime(){ return startTime; }
    public void setStartTime(String t){ startTime = t; }
    public String getEndTime(){ return endTime; }
    public void setEndTime(String t){ endTime = t; }
    public long getPublishedAt(){ return publishedAt; }
    public void setPublishedAt(long p){ publishedAt = p; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setActivityName(String activityName) { this.activityName = activityName; }
    public void setDescription(String description) { this.description = description; }
}