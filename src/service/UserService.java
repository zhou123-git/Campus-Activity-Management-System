package service;

import entity.User;
import db.DB;

import java.sql.*;
import java.util.Random;
import java.util.UUID;

public class UserService {
    /**
     * 生成随机八位数字ID
     */
    private String generateRandomId() {
        Random random = new Random();
        // 生成10000000-99999999之间的随机数
        int id = random.nextInt(90000000) + 10000000;
        return String.valueOf(id);
    }
    
    /**
     * 注册用户
     * 返回值含义：0=成功，1=用户名已存在，2=邮箱格式不正确，3=其它错误
     */
    public int register(String username, String password, String email) {
        // 简单邮箱格式校验
        if (email == null || email.trim().isEmpty()) {
            return 2;
        }
        String e = email.trim();
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!e.matches(emailRegex)) {
            return 2;
        }
        
        try (Connection conn = DB.getConn()) {
            // 检查用户名是否已存在
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM user WHERE username=?")) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (rs.next()) return 1;
            }
            
            // 生成随机八位数ID并确保不与现有ID冲突
            String userId = generateRandomId();
            boolean idExists = true;
            int attempts = 0;
            
            // 循环检查ID是否已存在，最多尝试10次
            while (idExists && attempts < 10) {
                try (PreparedStatement checkId = conn.prepareStatement("SELECT id FROM user WHERE id=?")) {
                    checkId.setString(1, userId);
                    ResultSet rs = checkId.executeQuery();
                    if (rs.next()) {
                        // ID已存在，生成新的ID
                        userId = generateRandomId();
                        attempts++;
                    } else {
                        // ID不存在，可以使用
                        idExists = false;
                    }
                }
            }
            
            // 如果尝试了10次还是没有找到唯一的ID，则返回错误
            if (idExists) {
                return 3; // 无法生成唯一ID
            }
            
            // 插入新用户
            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO user(id,username,password,email,avatar,role) VALUES (?,?,?,?,?,?)")) {
                insert.setString(1, userId);
                insert.setString(2, username);
                insert.setString(3, password);
                insert.setString(4, e);
                insert.setString(5, null);
                insert.setString(6, "user"); // 默认角色为普通用户
                insert.executeUpdate();
                return 0;
            }
        } catch(Exception e1) {
            e1.printStackTrace(); 
            return 3;
        }
    }

    public User login(String username, String password) {
        try (Connection conn = DB.getConn();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE username=? AND password=?");) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("avatar"));
                user.setRole(rs.getString("role")); // 设置用户角色
                return user;
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }
    // 构造User全部加avatar字段（数据库avatar字段）
    public User getUserInfo(String userId) {
        try(Connection conn = DB.getConn(); PreparedStatement stmt = conn.prepareStatement("SELECT * FROM user WHERE id=?")){
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                User user = new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("avatar"));
                user.setRole(rs.getString("role")); // 设置用户角色
                return user;
            }
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

    // 新增：用户资料更新
    public boolean updateUser(String userId, String username, String email) {
        return updateUser(userId, username, email, null);
    }
    // 新增：用户资料更新
    public boolean updateUser(String userId, String username, String email, String avatar) {
        try(Connection conn = DB.getConn()) {
            // 检查新用户名是否已被其他用户使用
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM user WHERE username=? AND id!=?")) {
                check.setString(1, username);
                check.setString(2, userId);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    // 用户名已被其他用户使用
                    return false;
                }
            }
            
            // 更新用户信息
            try (PreparedStatement p = conn.prepareStatement("UPDATE user SET username=?,email=?,avatar=? WHERE id=?")) {
                p.setString(1, username);
                p.setString(2, email);
                p.setString(3, avatar);
                p.setString(4, userId);
                return p.executeUpdate() > 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    // 新增：用户密码修改
    public boolean changePassword(String userId, String oldPwd, String newPwd) {
        try(Connection conn = DB.getConn(); PreparedStatement check = conn.prepareStatement("SELECT id FROM user WHERE id=? AND password=?"); PreparedStatement up = conn.prepareStatement("UPDATE user SET password=? WHERE id=?")){
            check.setString(1, userId);
            check.setString(2, oldPwd);
            ResultSet rs = check.executeQuery();
            if(!rs.next()) return false;
            up.setString(1, newPwd);
            up.setString(2, userId);
            up.executeUpdate();
            return true;
        }catch(Exception e){e.printStackTrace();return false;}
    }
}