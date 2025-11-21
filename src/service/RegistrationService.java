package service;

import entity.Registration;
import db.DB;
import java.sql.*;
import java.util.*;

public class RegistrationService {
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
            // 使用用户ID作为报名ID
            p.setString(1,userId);
            p.setString(2,userId);
            p.setString(3,activityId);
            p.setString(4,"已申请");
            p.executeUpdate();
            System.out.println("用户 " + userId + " 成功报名活动 " + activityId);
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
}