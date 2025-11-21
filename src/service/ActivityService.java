package service;

import entity.Activity;
import entity.User;
import db.DB;
import java.sql.*;
import java.util.*;

public class ActivityService {
    // 发布活动（按顺序自增ID，记录开始/结束时间与发布时间）
    public boolean publishActivity(String name, String desc, String publisherId, int maxNum, String eventTime, String startTime, String endTime, String location, UserService userService) {
        String id = null;
        try(Connection conn = DB.getConn()){
            long publishedAt = System.currentTimeMillis();
            // 查询当前最大ID（数字）
            try(Statement s = conn.createStatement()){
                ResultSet rs = s.executeQuery("SELECT MAX(CAST(id AS UNSIGNED)) FROM activity");
                int next = 1;
                if(rs.next()){
                    int m = rs.getInt(1);
                    if(!rs.wasNull()) next = m + 1;
                }
                id = String.valueOf(next);
            }
            
            // 获取发布者信息，判断是否需要审核
            User publisher = userService.getUserInfo(publisherId);
            String status = "approved"; // 默认为已通过
            if (publisher != null && "user".equals(publisher.getRole())) {
                status = "pending"; // 普通用户发布的活动需要审核
            }
            
            try(PreparedStatement p = conn.prepareStatement("INSERT INTO activity(id,name,description,publisher_id,max_num,event_time,start_time,end_time,published_at,status,location) VALUES(?,?,?,?,?,?,?,?,?,?,?)")){
                p.setString(1,id);
                p.setString(2,name);
                p.setString(3,desc);
                p.setString(4,publisherId);
                p.setInt(5,maxNum);
                p.setString(6,eventTime);
                p.setString(7,startTime);
                p.setString(8,endTime);
                p.setLong(9,publishedAt);
                p.setString(10,status);
                p.setString(11,location);
                p.executeUpdate();
                return true;
            }
        }catch(Exception e){e.printStackTrace(); return false;}
    }
    // 查所有活动（按发布时间排序）- 只返回已通过审核的活动
    public List<Activity> queryActivities() {
        List<Activity> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM activity WHERE status='approved' ORDER BY published_at ASC, CAST(id AS UNSIGNED) ASC")){
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                Activity activity = new Activity(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("publisher_id"),
                    rs.getInt("max_num"),
                    rs.getString("event_time"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getLong("published_at"),
                    rs.getString("status"));
                activity.setLocation(rs.getString("location"));
                list.add(activity);
            }
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
    // 更新活动（仅限发布者本人）
    public boolean updateActivity(String activityId, String name, String desc, String publisherId, int maxNum, String eventTime, String startTime, String endTime, String location) {
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("UPDATE activity SET name=?,description=?,max_num=?,event_time=?,start_time=?,end_time=?,location=? WHERE id=? AND publisher_id=?")){
            p.setString(1,name);
            p.setString(2,desc);
            p.setInt(3,maxNum);
            p.setString(4,eventTime);
            p.setString(5,startTime);
            p.setString(6,endTime);
            p.setString(7,location);
            p.setString(8,activityId);
            p.setString(9,publisherId);
            return p.executeUpdate()>0;
        }catch(Exception e){e.printStackTrace(); return false;}
    }
    // 查询单个活动
    public Activity getActivity(String activityId) {
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM activity WHERE id=?")){
            p.setString(1, activityId);
            ResultSet rs = p.executeQuery();
            if(rs.next()) {
                Activity activity = new Activity(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("publisher_id"),
                    rs.getInt("max_num"),
                    rs.getString("event_time"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getLong("published_at"),
                    rs.getString("status"));
                activity.setLocation(rs.getString("location"));
                System.out.println("找到活动: " + activityId);
                return activity;
            } else {
                System.err.println("未找到活动: " + activityId);
            }
        }catch(Exception e){
            System.err.println("获取活动信息时出错: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    // 按发布者查活动
    public List<Activity> queryActivitiesByPublisher(String publisherId) {
        List<Activity> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM activity WHERE publisher_id=? ORDER BY published_at ASC, CAST(id AS UNSIGNED) ASC")){
            p.setString(1, publisherId);
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                Activity activity = new Activity(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("publisher_id"),
                    rs.getInt("max_num"),
                    rs.getString("event_time"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getLong("published_at"),
                    rs.getString("status"));
                activity.setLocation(rs.getString("location"));
                list.add(activity);
            }
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
    // 删除活动（管理员可以删除任意活动，普通用户只能删除自己发布的活动）
    public boolean deleteActivity(String activityId, String requesterId) {
        try(Connection conn = DB.getConn()) {
            // 开始事务
            conn.setAutoCommit(false);
            
            try {
                // 先查询活动信息和请求者信息
                User requester = null;
                String publisherId = null;
                
                try (PreparedStatement queryUser = conn.prepareStatement("SELECT role FROM user WHERE id=?")) {
                    queryUser.setString(1, requesterId);
                    ResultSet userRs = queryUser.executeQuery();
                    if (userRs.next()) {
                        requester = new User(requesterId, "", "", "");
                        requester.setRole(userRs.getString("role"));
                    }
                }
                
                try (PreparedStatement queryActivity = conn.prepareStatement("SELECT publisher_id FROM activity WHERE id=?")) {
                    queryActivity.setString(1, activityId);
                    ResultSet activityRs = queryActivity.executeQuery();
                    if (activityRs.next()) {
                        publisherId = activityRs.getString("publisher_id");
                    } else {
                        // 活动不存在
                        conn.commit();
                        return false;
                    }
                }
                
                // 检查权限：管理员可以删除任意活动，普通用户只能删除自己发布的活动
                boolean hasPermission = false;
                if (requester != null && "admin".equals(requester.getRole())) {
                    // 管理员有权限删除任意活动
                    hasPermission = true;
                } else if (requesterId.equals(publisherId)) {
                    // 普通用户可以删除自己发布的活动
                    hasPermission = true;
                }
                
                if (!hasPermission) {
                    conn.commit();
                    return false;
                }
                
                // 先删除与该活动相关的所有报名记录
                try (PreparedStatement deleteRegs = conn.prepareStatement("DELETE FROM registration WHERE activity_id=?")) {
                    deleteRegs.setString(1, activityId);
                    deleteRegs.executeUpdate();
                }
                
                // 再删除活动本身
                try (PreparedStatement deleteActivity = conn.prepareStatement("DELETE FROM activity WHERE id=?")) {
                    deleteActivity.setString(1, activityId);
                    boolean result = deleteActivity.executeUpdate() > 0;
                    
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
    
    // 审核活动
    public boolean reviewActivity(String activityId, String status) {
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("UPDATE activity SET status=? WHERE id=?")){
            p.setString(1, status); // "approved" 或 "rejected"
            p.setString(2, activityId);
            return p.executeUpdate()>0;
        }catch(Exception e){e.printStackTrace(); return false;}
    }
    
    // 查询待审核的活动
    public List<Activity> queryPendingActivities() {
        List<Activity> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM activity WHERE status='pending' ORDER BY published_at ASC, CAST(id AS UNSIGNED) ASC")){
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                Activity activity = new Activity(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("publisher_id"),
                    rs.getInt("max_num"),
                    rs.getString("event_time"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getLong("published_at"),
                    rs.getString("status"));
                activity.setLocation(rs.getString("location"));
                list.add(activity);
            }
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
    
    // 查询所有活动（供管理员使用）
    public List<Activity> queryActivitiesForAdmin() {
        List<Activity> list = new ArrayList<>();
        try(Connection conn = DB.getConn(); PreparedStatement p = conn.prepareStatement("SELECT * FROM activity ORDER BY published_at ASC, CAST(id AS UNSIGNED) ASC")){
            ResultSet rs = p.executeQuery();
            while(rs.next()){
                Activity activity = new Activity(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("publisher_id"),
                    rs.getInt("max_num"),
                    rs.getString("event_time"),
                    rs.getString("start_time"),
                    rs.getString("end_time"),
                    rs.getLong("published_at"),
                    rs.getString("status"));
                activity.setLocation(rs.getString("location"));
                list.add(activity);
            }
        }catch(Exception e){e.printStackTrace();}
        return list;
    }
}