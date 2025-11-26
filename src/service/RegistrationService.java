package service;

import entity.Registration;
import db.DB;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class RegistrationService {
    // 用于生成递增的报名ID
    private static final AtomicLong REGISTRATION_ID_COUNTER = new AtomicLong(100000000001L);
    
    // 初始化时从数据库中获取最大的ID值
    static {
        try(Connection conn = DB.getConn(); 
            PreparedStatement p = conn.prepareStatement("SELECT MAX(CAST(id AS UNSIGNED)) FROM registration");
            ResultSet rs = p.executeQuery()) {
            if (rs.next()) {
                long maxId = rs.getLong(1);
                if (!rs.wasNull() && maxId >= 100000000001L) {
                    REGISTRATION_ID_COUNTER.set(maxId + 1);
                }
            }
        } catch(Exception e) {
            System.err.println("初始化报名ID计数器时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public int getRegistrationCountForActivity(String activityId) {
        int n = 0;
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT COUNT(*) FROM registration WHERE activity_id=? AND status!='已拒绝'")){
            p.setString(1, activityId);
            ResultSet rs = p.executeQuery();
            if(rs.next()) n = rs.getInt(1);
        }catch(Exception e){
            System.err.println("获取活动报名人数时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return n;
    }
    
    // 按照提交顺序生成12位递增数字ID
    private String generateRegistrationId(Connection conn) throws SQLException {
        while (true) {
            long id = REGISTRATION_ID_COUNTER.getAndIncrement();
            // 检查数据库中是否已存在该ID
            try (PreparedStatement check = conn.prepareStatement("SELECT 1 FROM registration WHERE id = ?")) {
                check.setLong(1, id);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        // ID不存在，可以使用
                        return String.valueOf(id);
                    }
                    // ID已存在，继续下一个
                }
            }
        }
    }
    
    public boolean registerActivity(String userId, String activityId, int maxNum) {
        try(Connection conn = DB.getConn();
            PreparedStatement check = conn.prepareStatement("SELECT id FROM registration WHERE user_id=? AND activity_id=?");
            PreparedStatement p = conn.prepareStatement("INSERT INTO registration(id,user_id,activity_id,status) VALUES(?,?,?,?)")){
            check.setString(1, userId);
            check.setString(2, activityId);
            ResultSet rs = check.executeQuery();
            if(rs.next()) {
                System.err.println("用户 " + userId + " 已经报名活动 " + activityId);
                return false;
            }
            // 新增: 满员判断
            int current = getRegistrationCountForActivity(activityId);
            System.out.println("活动 " + activityId + " 当前报名人数: " + current + ", 最大人数: " + maxNum);
            if(current>=maxNum) {
                System.err.println("活动 " + activityId + " 已满员");
                return false;
            }
            // 使用12位递增数字生成唯一的注册ID
            String registrationId = generateRegistrationId(conn);
            p.setString(1, registrationId);
            p.setString(2, userId);
            p.setString(3, activityId);
            p.setString(4, "已申请");
            p.executeUpdate();
            System.out.println("用户 " + userId + " 成功报名活动 " + activityId + "，报名ID: " + registrationId);
            return true;
        }catch(Exception e){
            System.err.println("报名活动时出错: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }
    }
    public boolean reviewRegistration(String regId, boolean approve) {
        try(Connection conn = DB.getConn(); PreparedStatement up = conn.prepareStatement("UPDATE registration SET status=? WHERE id=?")){
            up.setString(1,approve?"已通过":"已拒绝");
            up.setString(2,regId);
            return up.executeUpdate()>0;
        }catch(Exception e){
            System.err.println("审核报名时出错: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }
    }
    public List<Registration> getRegistrationsForActivity(String activityId) {
        List<Registration> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM registration WHERE activity_id=? ORDER BY id DESC")){
            p.setString(1,activityId);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                list.add(new Registration(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("activity_id"),
                    rs.getString("status")));
            }
        }catch(Exception e){
            System.err.println("获取活动报名列表时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public List<Registration> getRegistrationsForPublisher(String publisherId) {
        List<Registration> list = new ArrayList<>();
        try(Connection conn = DB.getConn();
            PreparedStatement p = conn.prepareStatement(
                "SELECT r.* FROM registration r JOIN activity a ON r.activity_id=a.id WHERE a.publisher_id=? ORDER BY r.id DESC")){
            p.setString(1, publisherId);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                list.add(new Registration(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("activity_id"),
                    rs.getString("status")));
            }
        }catch(Exception e){
            System.err.println("获取发布者报名列表时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    // 新增：按用户ID查询我报名的活动
    public List<Registration> getRegistrationsByUser(String userId) {
        List<Registration> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM registration WHERE user_id=? ORDER BY id DESC")){
            p.setString(1, userId);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                list.add(new Registration(
                    rs.getString("id"),
                    rs.getString("user_id"),
                    rs.getString("activity_id"),
                    rs.getString("status")));
            }
        }catch(Exception e){
            System.err.println("获取用户报名列表时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public boolean deleteRegistration(String registrationId) {
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("DELETE FROM registration WHERE id=?")){
            p.setString(1, registrationId);
            return p.executeUpdate()>0;
        }catch(Exception e){
            System.err.println("删除报名信息时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // 获取待审核的报名数量
    public int getPendingRegistrationsCount() {
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT COUNT(*) FROM registration WHERE status='已申请'")){
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }catch(Exception e){e.printStackTrace();}
        return 0;
    }

    // 获取用户需要审核的报名数量（作为活动发布者）
    public int getPendingRegistrationsCountForPublisher(String publisherId) {
        try(Connection conn = DB.getConn(); 
            PreparedStatement p = conn.prepareStatement(
                "SELECT COUNT(*) FROM registration r " +
                "JOIN activity a ON r.activity_id = a.id " +
                "WHERE r.status = '已申请' AND a.publisher_id = ?")) {
            p.setString(1, publisherId);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}