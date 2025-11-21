package entity;

public class User {
    private String userId;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private String role; // 新增角色字段，默认为"user"

    public User(String userId, String username, String password, String email, String avatar) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
        this.role = "user"; // 默认角色为普通用户
    }

    public User(String userId, String username, String password, String email) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = null;
        this.role = "user"; // 默认角色为普通用户
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setRole(String role) { this.role = role; }
}