package entity;

public class Registration {
    private String registrationId;
    private String userId;
    private String activityId;
    private String status;

    public Registration(String registrationId, String userId, String activityId, String status) {
        this.registrationId = registrationId;
        this.userId = userId;
        this.activityId = activityId;
        this.status = status;
    }

    public String getRegistrationId() { return registrationId; }
    public String getUserId() { return userId; }
    public String getActivityId() { return activityId; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
