import entity.User;
import entity.Activity;
import entity.Registration;
import service.UserService;
import service.ActivityService;
import service.RegistrationService;

import java.util.List;
import java.util.Scanner;

public class App {
    static UserService userService = new UserService();
    static ActivityService activityService = new ActivityService();
    static RegistrationService registrationService = new RegistrationService();
    static User currentUser = null;

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                if (currentUser == null) {
                    System.out.println("1. 注册\n2. 登录\n0. 退出");
                    String op = sc.nextLine();
                    if (op.equals("1")) {
                        System.out.print("用户名:"); String u = sc.nextLine();
                        System.out.print("密码:"); String p = sc.nextLine();
                        System.out.print("邮箱:"); String e = sc.nextLine();
                        if (userService.register(u, p, e)==1) {
                            System.out.println("注册成功");
                        } else {
                            System.out.println("用户名已存在");
                        }
                    } else if (op.equals("2")) {
                        System.out.print("用户名:"); String u = sc.nextLine();
                        System.out.print("密码:"); String p = sc.nextLine();
                        currentUser = userService.login(u, p);
                        if (currentUser == null) System.out.println("登录失败");
                        else System.out.println("欢迎: " + currentUser.getUsername());
                    } else return;
                } else {
                    System.out.println("1. 发布活动\n2. 查询活动\n3. 修改活动\n4. 活动报名\n5. 报名审核\n6. 当前信息\n9. 注销\n0. 退出");
                    String op = sc.nextLine();
                    if(op.equals("1")){
                        System.out.print("活动名称:"); String n = sc.nextLine();
                        System.out.print("简介:"); String d = sc.nextLine();
                        System.out.print("最大人数:"); String maxStr = sc.nextLine();
                        int maxNum = 0;
                        try { maxNum = Integer.parseInt(maxStr); } catch(NumberFormatException ex) { maxNum = 0; }
                        System.out.print("活动时间(任意字符串):"); String eventTime = sc.nextLine();
                        System.out.print("开始时间(任意字符串):"); String startTime = sc.nextLine();
                        System.out.print("结束时间(任意字符串):"); String endTime = sc.nextLine();
                        if (activityService.publishActivity(n, d, currentUser.getUserId(), maxNum, eventTime, startTime, endTime, userService))
                            System.out.println("发布成功");
                    } else if(op.equals("2")){
                        List<Activity> list = activityService.queryActivities();
                        System.out.println("活动列表:");
                        for (Activity a : list) {
                            System.out.println(a.getActivityId() + ": " + a.getActivityName() + " (" + a.getDescription() + ") by " + a.getPublisherId());
                        }
                    } else if(op.equals("3")){
                        System.out.print("活动ID:"); String id = sc.nextLine();
                        System.out.print("新名称:"); String n = sc.nextLine();
                        System.out.print("新简介:"); String d = sc.nextLine();
                        System.out.print("最大人数:"); String maxStr = sc.nextLine();
                        int maxNum = 0;
                        try { maxNum = Integer.parseInt(maxStr); } catch(NumberFormatException ex) { maxNum = 0; }
                        System.out.print("活动时间(任意字符串):"); String eventTime = sc.nextLine();
                        System.out.print("开始时间(任意字符串):"); String startTime = sc.nextLine();
                        System.out.print("结束时间(任意字符串):"); String endTime = sc.nextLine();
                        if(activityService.updateActivity(id, n, d, currentUser.getUserId(), maxNum, eventTime, startTime, endTime)) System.out.println("修改成功");
                        else System.out.println("未找到活动或无权限");
                    } else if(op.equals("4")){
                        System.out.print("报名活动ID:"); String aid = sc.nextLine();
                        Activity act = activityService.getActivity(aid);
                        if(act==null) {
                            System.out.println("活动未找到");
                        } else {
                            if(registrationService.registerActivity(currentUser.getUserId(), aid, act.getMaxNum()))
                                System.out.println("报名已提交");
                            else
                                System.out.println("报名失败（可能已报名或已满）");
                        }
                    } else if(op.equals("5")){
                        System.out.print("活动ID:"); String aid = sc.nextLine();
                        List<Registration> regs = registrationService.getRegistrationsForActivity(aid);
                        for(Registration r : regs) {
                            System.out.println(r.getRegistrationId() + "|用户:" + r.getUserId() + ", 状态:" + r.getStatus());
                        }
                        System.out.print("审核报名ID:"); String rid = sc.nextLine();
                        System.out.print("通过/拒绝 (1/0):"); String res = sc.nextLine();
                        if(registrationService.reviewRegistration(rid, res.equals("1")))
                            System.out.println("审核成功");
                    } else if (op.equals("6")) {
                        System.out.println("用户名:"+currentUser.getUsername()+", 邮箱:"+currentUser.getEmail());
                    } else if (op.equals("9")) {
                        currentUser = null;
                        System.out.println("已注销");
                    } else if (op.equals("0")) return;
                }
            }
        }
    }
}