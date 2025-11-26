package service;

import entity.User;
import db.DB;

import java.sql.*;
import java.util.Random;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    // Store avatar as a path that Server can serve (resources/images/...)
    private static final String DEFAULT_AVATAR = "/resources/images/default-avatar.jpg";
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
                insert.setString(5, DEFAULT_AVATAR); // 设置默认头像（Server会直接提供该路径下的文件）
                insert.setString(6, "user"); // 默认角色为普通用户
                insert.executeUpdate();
                return 0;
            }
        } catch(Exception e1) {
            e1.printStackTrace(); 
            return 3;
        }
    }

    private User normalizeAvatar(User user) {
        if (user == null) return null;
        String av = user.getAvatar();
        if (av == null || av.trim().isEmpty()) user.setAvatar(DEFAULT_AVATAR);
        return user;
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
                return normalizeAvatar(user);
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
                return normalizeAvatar(user);
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
                p.setString(3, (avatar == null || avatar.trim().isEmpty()) ? DEFAULT_AVATAR : avatar);
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
    // 新增：更新用户名
    public boolean updateUsername(String userId, String oldPwd, String newUsername) {
        // 参数验证
        if (userId == null || userId.isEmpty() || 
            oldPwd == null || oldPwd.isEmpty() || 
            newUsername == null || newUsername.isEmpty()) {
            return false;
        }
        
        // 用户名基本格式检查
        if (newUsername.length() < 3 || newUsername.length() > 20) {
            return false;
        }
        
        try(Connection conn = DB.getConn()) {
            // 验证旧密码是否正确
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM user WHERE id=? AND password=?")) {
                check.setString(1, userId);
                check.setString(2, oldPwd);
                ResultSet rs = check.executeQuery();
                if(!rs.next()) return false; // 密码错误
            }
            
            // 检查新用户名是否与当前用户名相同
            try (PreparedStatement check = conn.prepareStatement("SELECT username FROM user WHERE id=?")) {
                check.setString(1, userId);
                ResultSet rs = check.executeQuery();
                if (rs.next() && newUsername.equals(rs.getString("username"))) {
                    // 新用户名与当前用户名相同，无需更新
                    return true;
                }
            }
            
            // 检查新用户名是否已被其他用户使用
            try (PreparedStatement check = conn.prepareStatement("SELECT id FROM user WHERE username=? AND id!=?")) {
                check.setString(1, newUsername);
                check.setString(2, userId);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    // 用户名已被其他用户使用
                    return false;
                }
            }
            
            // 更新用户名
            try (PreparedStatement up = conn.prepareStatement("UPDATE user SET username=? WHERE id=?")) {
                up.setString(1, newUsername);
                up.setString(2, userId);
                int rowsAffected = up.executeUpdate();
                return rowsAffected > 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 管理员功能：创建用户
    public int createUserByAdmin(String username, String password, String email, String role) {
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
            
            // 插入新用户，允许管理员指定角色
            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO user(id,username,password,email,avatar,role) VALUES (?,?,?,?,?,?)")) {
                insert.setString(1, userId);
                insert.setString(2, username);
                insert.setString(3, password);
                insert.setString(4, e);
                insert.setString(5, DEFAULT_AVATAR); // 设置默认头像
                insert.setString(6, role); // 管理员可以指定角色
                insert.executeUpdate();
                return 0;
            }
        } catch(Exception e1) {
            e1.printStackTrace(); 
            return 3;
        }
    }
    
    // 管理员功能：删除用户
    public boolean deleteUserByAdmin(String userId) {
        try (Connection conn = DB.getConn()) {
            // 开始事务
            conn.setAutoCommit(false);
            
            try {
                // 先删除与该用户相关的所有报名记录
                try (PreparedStatement deleteRegs = conn.prepareStatement("DELETE FROM registration WHERE user_id=?")) {
                    deleteRegs.setString(1, userId);
                    deleteRegs.executeUpdate();
                }
                
                // 再删除用户发布的活动
                try (PreparedStatement deleteActivities = conn.prepareStatement("DELETE FROM activity WHERE publisher_id=?")) {
                    deleteActivities.setString(1, userId);
                    deleteActivities.executeUpdate();
                }
                
                // 最后删除用户本身
                try (PreparedStatement deleteUser = conn.prepareStatement("DELETE FROM user WHERE id=?")) {
                    deleteUser.setString(1, userId);
                    boolean result = deleteUser.executeUpdate() > 0;
                    
                    // 提交事务
                    conn.commit();
                    return result;
                }
            } catch (Exception e) {
                // 回滚事务
                conn.rollback();
                throw e;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 管理员功能：更新用户信息（包括角色）
    public boolean updateUserByAdmin(String userId, String username, String email, String role) {
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
            
            // 更新用户信息，包括角色
            try (PreparedStatement p = conn.prepareStatement("UPDATE user SET username=?,email=?,role=? WHERE id=?")) {
                p.setString(1, username);
                p.setString(2, email);
                p.setString(3, role);
                p.setString(4, userId);
                return p.executeUpdate() > 0;
            }
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 管理员功能：获取所有用户列表（支持分页和搜索）
    public List<User> getAllUsers(int page, int pageSize, String searchKeyword) {
        List<User> users = new ArrayList<>();
        int offset = (page - 1) * pageSize;
        try(Connection conn = DB.getConn()) {
            String sql;
            PreparedStatement stmt;
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                // 模糊搜索查询
                sql = "SELECT * FROM user WHERE username LIKE ? ORDER BY id LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "%" + searchKeyword + "%");
                stmt.setInt(2, pageSize);
                stmt.setInt(3, offset);
            } else {
                // 普通分页查询
                sql = "SELECT * FROM user ORDER BY id LIMIT ? OFFSET ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, pageSize);
                stmt.setInt(2, offset);
            }
            
            ResultSet rs = stmt.executeQuery();
            System.out.println("开始查询用户列表，页码: " + page + ", 每页数量: " + pageSize + ", 搜索关键词: " + searchKeyword);
            while(rs.next()) {
                User user = new User(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("avatar"));
                user.setRole(rs.getString("role"));
                System.out.println("查询到用户: ID=" + user.getUserId() + ", 用户名=" + user.getUsername() + ", 角色=" + user.getRole());
                users.add(normalizeAvatar(user));
            }
            System.out.println("总共查询到 " + users.size() + " 个用户");
        } catch(Exception e) {
            System.err.println("查询用户列表时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    // 管理员功能：获取用户总数（支持搜索）
    public int getUserCount(String searchKeyword) {
        try(Connection conn = DB.getConn()) {
            String sql;
            PreparedStatement stmt;
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                // 模糊搜索计数
                sql = "SELECT COUNT(*) AS total FROM user WHERE username LIKE ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, "%" + searchKeyword + "%");
            } else {
                // 普通计数
                sql = "SELECT COUNT(*) AS total FROM user";
                stmt = conn.prepareStatement(sql);
            }
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch(Exception e) {
            System.err.println("查询用户总数时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}