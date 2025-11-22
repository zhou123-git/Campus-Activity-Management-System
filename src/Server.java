import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import service.UserService;
import service.ActivityService;
import service.RegistrationService;
import entity.User;
import entity.Activity;
import entity.Registration;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    static UserService userService = new UserService();
    static ActivityService activityService = new ActivityService();
    static RegistrationService registrationService = new RegistrationService();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        // 用户注册
        server.createContext("/api/register", Server::handleRegister);
        // 用户登录
        server.createContext("/api/login", Server::handleLogin);
        // 活动发布
        server.createContext("/api/activity/publish", Server::handleActivityPublish);
        // 活动列表
        server.createContext("/api/activity/list", Server::handleActivityList);
        // 活动修改
        server.createContext("/api/activity/update", Server::handleActivityUpdate);
        // 报名提交
        server.createContext("/api/registration/apply", Server::handleRegistrationApply);
        // 报名审核
        server.createContext("/api/registration/review", Server::handleRegistrationReview);
        // 活动下所有报名
        server.createContext("/api/registration/list", Server::handleRegistrationList);
        // 查询当前用户信息
        server.createContext("/api/user/info", Server::handleUserInfo);
        // 修改当前用户信息
        server.createContext("/api/user/update", Server::handleUserUpdate);
        // 修改密码
        server.createContext("/api/user/changePwd", Server::handleUserChangePwd);
        // 查询我发布的活动
        server.createContext("/api/activity/my", Server::handleActivityMy);
        // 删除活动
        server.createContext("/api/activity/delete", Server::handleActivityDelete);
        // 查询我报名的活动
        server.createContext("/api/registration/my", Server::handleRegistrationMy);
        // 用户头像上传
        server.createContext("/api/user/avatarUpload", Server::handleAvatarUpload);
        // 管理端增删报名
        server.createContext("/api/registration/add", Server::handleRegistrationAdd);
        server.createContext("/api/registration/delete", Server::handleRegistrationDelete);
        // 管理端审核活动
        server.createContext("/api/activity/review", Server::handleActivityReview);
        server.createContext("/api/activity/pending", Server::handlePendingActivities);
        // 管理端查询所有活动
        server.createContext("/api/activity/all", Server::handleAllActivities);
        // 管理员用户管理功能
        server.createContext("/api/admin/user/create", Server::handleAdminCreateUser);
        server.createContext("/api/admin/user/delete", Server::handleAdminDeleteUser);
        server.createContext("/api/admin/user/update", Server::handleAdminUpdateUser);
        server.createContext("/api/admin/user/list", Server::handleAdminListUsers);
        // 静态资源处理
        server.createContext("/", Server::handleStaticResource);
        server.createContext("/index.html", Server::handleStaticResource);
        server.createContext("/main.js", Server::handleStaticResource);
        server.createContext("/resources", Server::handleStaticResource);
        server.setExecutor(null);
        server.start();
        System.out.println("服务器已启动: http://localhost:8000");
    }
    static void allowCORS(HttpExchange t) {
        Headers h = t.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
    }
    static String readReqBody(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) bos.write(buf, 0, len);
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
    static Map<String, String> parseUrlEncoded(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                try {
                    String key = URLDecoder.decode(parts[0], "UTF-8");
                    String value = URLDecoder.decode(parts[1], "UTF-8");
                    map.put(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }
    static void resp(HttpExchange t, String resp) throws IOException {
        allowCORS(t);
        Headers h = t.getResponseHeaders();
        h.add("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(200, bytes.length);
        OutputStream os = t.getResponseBody();
        os.write(bytes);
        os.close();
    }
    static void handleRegister(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        int code = userService.register(map.get("username"), map.get("password"), map.get("email"));
        switch(code){
            case 0: resp(t, "{\"status\":0}"); break;
            case 1: resp(t, "{\"status\":1,\"msg\":\"用户名已存在\"}"); break;
            case 2: resp(t, "{\"status\":1,\"msg\":\"邮箱格式不正确\"}"); break;
            default: resp(t, "{\"status\":1,\"msg\":\"注册失败\"}"); break;
        }
    }
    static void handleLogin(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        User u = userService.login(map.get("username"), map.get("password"));
        if (u != null) {
            String r = String.format("{\"status\":0,\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"avatar\":\"%s\"}",
                u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), u.getAvatar());
            resp(t, r);
        } else resp(t, "{\"status\":1,\"msg\":\"用户名或密码错误\"}");
    }
    static void handleActivityPublish(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        String eventTime = (map.getOrDefault("startTime", "") + " ~ " + map.getOrDefault("endTime", "")).trim();
        boolean ok = activityService.publishActivity(
            map.get("name"),
            map.get("desc"),
            map.get("publisherId"),
            Integer.parseInt(map.getOrDefault("maxNum","30")),
            eventTime,
            map.getOrDefault("startTime",""),
            map.getOrDefault("endTime", ""),
            map.getOrDefault("location", ""),
            userService
        );
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"发布失败\"}");
    }
    static void handleActivityList(HttpExchange t) throws IOException {
        allowCORS(t);
        List<Activity> acts = activityService.queryActivities();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<acts.size();i++){
            Activity a = acts.get(i);
            int count = registrationService.getRegistrationCountForActivity(a.getActivityId());
            // 获取发布者用户名而不是ID
            User publisher = userService.getUserInfo(a.getPublisherId());
            String publisherName = publisher != null ? publisher.getUsername() : "未知用户";
            sb.append(String.format("{\"activityId\":\"%s\",\"activityName\":\"%s\",\"description\":\"%s\",\"publisherId\":\"%s\",\"publisherName\":\"%s\",\"maxNum\":%d,\"count\":%d,\"eventTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"status\":\"%s\",\"location\":\"%s\"}",
                a.getActivityId(),a.getActivityName(),a.getDescription(),a.getPublisherId(),publisherName,a.getMaxNum(),count,a.getEventTime(),a.getStartTime(),a.getEndTime(),a.getStatus(),a.getLocation()));
            if(i<acts.size()-1) sb.append(",\n");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
    static void handleActivityUpdate(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        String eventTime = (map.getOrDefault("startTime", "") + " ~ " + map.getOrDefault("endTime", "")).trim();
        // 确保只有活动发布者可以更新活动
        boolean ok = activityService.updateActivity(
            map.get("activityId"),
            map.get("name"),
            map.get("desc"),
            map.get("publisherId"),
            Integer.parseInt(map.getOrDefault("maxNum","30")),
            eventTime,
            map.getOrDefault("startTime",""),
            map.getOrDefault("endTime", ""),
            map.getOrDefault("location", "")
        );
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"更新失败或无权限\"}");
    }
    static void handleRegistrationApply(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        System.out.println("收到活动报名请求: " + body);
        
        String userId = map.get("userId");
        String activityId = map.get("activityId");
        
        if (userId == null || activityId == null) {
            System.err.println("缺少必要参数: userId=" + userId + ", activityId=" + activityId);
            resp(t, "{\"status\":1,\"msg\":\"缺少必要参数\"}");
            return;
        }
        
        Activity act = activityService.getActivity(activityId);
        if (act == null) {
            System.err.println("未找到活动: " + activityId);
            resp(t, "{\"status\":1,\"msg\":\"未找到活动\"}");
            return;
        }
        
        // 只允许审核通过的活动被报名
        if (!"approved".equals(act.getStatus())) {
            System.err.println("活动未审核通过: " + activityId);
            resp(t, "{\"status\":1,\"msg\":\"活动未审核通过\"}");
            return;
        }
        
        int maxNum = act.getMaxNum();
        System.out.println("活动 " + activityId + " 的最大人数: " + maxNum);
        
        boolean ok = registrationService.registerActivity(userId, activityId, maxNum);
        int current = registrationService.getRegistrationCountForActivity(activityId);
        
        System.out.println("报名结果: ok=" + ok + ", 当前人数: " + current + ", 最大人数: " + maxNum);
        
        if(current>maxNum) {
            resp(t, "{\"status\":2,\"msg\":\"人数已满\"}");
        } else {
            resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"已报名或失败\"}");
        }
    }
    static void handleRegistrationReview(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        boolean ok = registrationService.reviewRegistration(map.get("registrationId"), "1".equals(map.get("approve")));
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1}");
    }
    static void handleRegistrationList(HttpExchange t) throws IOException {
        allowCORS(t);
        String q = t.getRequestURI().getQuery();
        String activityId = "";
        if(q!=null && q.startsWith("activityId=")) activityId = q.substring(11);
        List<Registration> regs = registrationService.getRegistrationsForActivity(activityId);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i<regs.size();i++){
            Registration r = regs.get(i);
            // 获取用户名而不是用户ID
            User user = userService.getUserInfo(r.getUserId());
            String username = user != null ? user.getUsername() : r.getUserId();
            sb.append(String.format("{\"registrationId\":\"%s\",\"userId\":\"%s\",\"username\":\"%s\",\"activityId\":\"%s\",\"status\":\"%s\"}",
                r.getRegistrationId(), r.getUserId(), username, r.getActivityId(), r.getStatus()));
            if(i<regs.size()-1) sb.append(",");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
    // 管理端：增加报名者
    static void handleRegistrationAdd(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        Activity act = activityService.getActivity(map.get("activityId"));
        int maxNum = (act==null?30:act.getMaxNum());
        boolean ok = registrationService.registerActivity(map.get("userId"), map.get("activityId"), maxNum);
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1}");
    }
    // 管理端：删除报名者
    static void handleRegistrationDelete(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        boolean ok = registrationService.deleteRegistration(map.get("registrationId"));
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1}");
    }
    // 用户信息查询
    static void handleUserInfo(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        User u = userService.getUserInfo(map.get("userId"));
        if (u != null) {
            resp(t, String.format("{\"status\":0,\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"avatar\":\"%s\"}", u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), u.getAvatar()));
        } else resp(t, "{\"status\":1,\"msg\":\"用户不存在\"}");
    }
    // 用户信息修改加avatar参数
    static void handleUserUpdate(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        String avatar = map.get("avatar");
        boolean ok = userService.updateUser(map.get("userId"), map.get("username"), map.get("email"), avatar);
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"更新失败，用户名可能已存在\"}");
    }
    // 修改用户密码
    static void handleUserChangePwd(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        boolean ok = userService.changePassword(map.get("userId"), map.get("oldPwd"), map.get("newPwd"));
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"密码错误\"}");
    }
    // 查询我发布的活动
    static void handleActivityMy(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        List<Activity> acts = activityService.queryActivitiesByPublisher(map.get("publisherId"));
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i<acts.size();i++){
            Activity a = acts.get(i);
            // 获取发布者用户名而不是ID
            User publisher = userService.getUserInfo(a.getPublisherId());
            String publisherName = publisher != null ? publisher.getUsername() : "未知用户";
            sb.append(String.format("{\"activityId\":\"%s\",\"activityName\":\"%s\",\"description\":\"%s\",\"publisherId\":\"%s\",\"publisherName\":\"%s\",\"status\":\"%s\",\"location\":\"%s\"}", 
                a.getActivityId(),a.getActivityName(),a.getDescription(),a.getPublisherId(),publisherName,a.getStatus(),a.getLocation()));
            if(i<acts.size()-1) sb.append(",\n");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
    // 删除活动
    static void handleActivityDelete(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        // 获取当前用户信息并检查权限
        String requesterId = map.get("publisherId"); // 这里实际上是请求者的ID
        User requester = userService.getUserInfo(requesterId);
        if (requester == null) {
            resp(t, "{\"status\":1,\"msg\":\"用户不存在\"}");
            return;
        }
        
        boolean ok = activityService.deleteActivity(map.get("activityId"), requesterId);
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"删除失败，权限不足或活动不存在\"}");
    }
    // 查询我参与的活动报名
    static void handleRegistrationMy(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        List<Registration> regs = registrationService.getRegistrationsByUser(map.get("userId"));
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i<regs.size();i++){
            Registration r = regs.get(i);
            // 获取用户名而不是用户ID
            User user = userService.getUserInfo(r.getUserId());
            String username = user != null ? user.getUsername() : r.getUserId();
            
            // 获取活动信息以获取活动名称
            Activity activity = activityService.getActivity(r.getActivityId());
            String activityName = activity != null ? activity.getActivityName() : "未知活动";
            
            sb.append(String.format("{\"registrationId\":\"%s\",\"userId\":\"%s\",\"username\":\"%s\",\"activityId\":\"%s\",\"activityName\":\"%s\",\"status\":\"%s\"}",
                r.getRegistrationId(), r.getUserId(), username, r.getActivityId(), activityName, r.getStatus()));
            if(i<regs.size()-1) sb.append(",");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
    // 用户头像上传
    static void handleAvatarUpload(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String contentType = t.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null) {
            contentType = t.getRequestHeaders().getFirst("Content-type");
        }
        if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
            resp(t, "{\"status\":1,\"msg\":\"Content-Type必须为multipart/form-data\"}");
            return;
        }
        // (此处建议使用Multipart解析库处理文件数据，简化版处理略)
        // 直接将文件内容保存为 userId.jpg/png，返回URL
        // 这里只做接口声明，完整实现需补充文件处理逻辑
        // ... 留空...
        resp(t, "{\"status\":0,\"url\":\"/avatars/demo.jpg\"}"); // 示例返回
    }
    // 管理端审核活动
    static void handleActivityReview(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        // 获取当前用户信息并检查权限
        String reviewerId = map.get("reviewerId");
        User reviewer = userService.getUserInfo(reviewerId);
        if (reviewer == null || !"admin".equals(reviewer.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        String activityId = map.get("activityId");
        String status = map.get("status"); // "approved" 或 "rejected"
        
        boolean ok = activityService.reviewActivity(activityId, status);
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"审核失败\"}");
    }
    // 查询待审核的活动
    static void handlePendingActivities(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        
        // 获取当前用户信息并检查权限
        String q = t.getRequestURI().getQuery();
        String reviewerId = "";
        if(q!=null && q.startsWith("reviewerId=")) reviewerId = q.substring(11);
        
        User reviewer = userService.getUserInfo(reviewerId);
        if (reviewer == null || !"admin".equals(reviewer.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        List<Activity> acts = activityService.queryPendingActivities();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<acts.size();i++){
            Activity a = acts.get(i);
            int count = registrationService.getRegistrationCountForActivity(a.getActivityId());
            // 获取发布者用户名而不是ID
            User publisher = userService.getUserInfo(a.getPublisherId());
            String publisherName = publisher != null ? publisher.getUsername() : "未知用户";
            sb.append(String.format("{\"activityId\":\"%s\",\"activityName\":\"%s\",\"description\":\"%s\",\"publisherId\":\"%s\",\"publisherName\":\"%s\",\"maxNum\":%d,\"count\":%d,\"eventTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"status\":\"%s\",\"location\":\"%s\"}",
                a.getActivityId(),a.getActivityName(),a.getDescription(),a.getPublisherId(),publisherName,a.getMaxNum(),count,a.getEventTime(),a.getStartTime(),a.getEndTime(), a.getStatus(),a.getLocation()));
            if(i<acts.size()-1) sb.append(",\n");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
    
    // 查询所有活动（供管理员在活动管理页面使用）
    static void handleAllActivities(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        
        // 获取当前用户信息并检查权限
        String q = t.getRequestURI().getQuery();
        String reviewerId = "";
        if(q!=null && q.startsWith("reviewerId=")) reviewerId = q.substring(11);
        
        User reviewer = userService.getUserInfo(reviewerId);
        if (reviewer == null || !"admin".equals(reviewer.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        List<Activity> acts = activityService.queryActivitiesForAdmin();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<acts.size();i++){
            Activity a = acts.get(i);
            int count = registrationService.getRegistrationCountForActivity(a.getActivityId());
            // 获取发布者用户名而不是ID
            User publisher = userService.getUserInfo(a.getPublisherId());
            String publisherName = publisher != null ? publisher.getUsername() : "未知用户";
            sb.append(String.format("{\"activityId\":\"%s\",\"activityName\":\"%s\",\"description\":\"%s\",\"publisherId\":\"%s\",\"publisherName\":\"%s\",\"maxNum\":%d,\"count\":%d,\"eventTime\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"status\":\"%s\",\"location\":\"%s\"}",
                a.getActivityId(),a.getActivityName(),a.getDescription(),a.getPublisherId(),publisherName,a.getMaxNum(),count,a.getEventTime(),a.getStartTime(),a.getEndTime(), a.getStatus(),a.getLocation()));
            if(i<acts.size()-1) sb.append(",\n");
        }
        sb.append("]");
        resp(t, sb.toString());
    }
//    static void handleStaticResource(HttpExchange t) throws IOException {
//        allowCORS(t);
//        String uri = t.getRequestURI().getPath();
//        if (uri.equals("/")) uri = "/index.html";
//        java.io.File file = new java.io.File(System.getProperty("user.dir") + "/frontend" + uri);
//        if(uri.equals("/main.js")) t.getResponseHeaders().add("Content-Type","application/javascript");
//        else t.getResponseHeaders().add("Content-Type","text/html; charset=utf-8");
//        if (!file.exists()) {
//            String resp = "404 Not Found";
//            t.sendResponseHeaders(404, resp.length());
//            t.getResponseBody().write(resp.getBytes());
//            t.getResponseBody().close();
//            return;
//        }
//        byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
//        t.sendResponseHeaders(200, data.length);
//        t.getResponseBody().write(data);
//        t.getResponseBody().close();
//    }

    static void handleStaticResource(HttpExchange t) throws IOException {
    allowCORS(t);
    String uri = t.getRequestURI().getPath();
    String baseDir = System.getProperty("user.dir");

    // 处理资源文件
    if (uri.startsWith("/resources/")) {
        java.io.File file = new java.io.File(baseDir + uri);
        if (file.exists() && file.isFile()) {
            String contentType = getContentType(file.getName());
            t.getResponseHeaders().add("Content-Type", contentType);
            byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
            t.sendResponseHeaders(200, data.length);
            t.getResponseBody().write(data);
            t.getResponseBody().close();
            return;
        }
    } else {
        // 原有的处理逻辑
        if (uri.equals("/")) uri = "/index.html";
        java.io.File file = new java.io.File(baseDir + "/frontend" + uri);
        if(uri.equals("/main.js")) t.getResponseHeaders().add("Content-Type","application/javascript");
        else t.getResponseHeaders().add("Content-Type","text/html; charset=utf-8");
        if (!file.exists()) {
            String resp = "404 Not Found";
            t.sendResponseHeaders(404, resp.length());
            t.getResponseBody().write(resp.getBytes());
            t.getResponseBody().close();
            return;
        }
        byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
        t.sendResponseHeaders(200, data.length);
        t.getResponseBody().write(data);
        t.getResponseBody().close();
    }
}

// 添加获取内容类型的方法
private static String getContentType(String fileName) {
    if (fileName.endsWith(".png")) {
        return "image/png";
    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
        return "image/jpeg";
    } else if (fileName.endsWith(".gif")) {
        return "image/gif";
    } else {
        return "application/octet-stream";
    }
}


    // 管理员功能：创建用户
    static void handleAdminCreateUser(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        // 获取当前用户信息并检查权限
        String adminId = map.get("adminId");
        User admin = userService.getUserInfo(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        // 创建用户
        int result = userService.createUserByAdmin(
            map.get("username"), 
            map.get("password"), 
            map.get("email"), 
            map.getOrDefault("role", "user"));
            
        switch(result) {
            case 0: resp(t, "{\"status\":0}"); break;
            case 1: resp(t, "{\"status\":1,\"msg\":\"用户名已存在\"}"); break;
            case 2: resp(t, "{\"status\":1,\"msg\":\"邮箱格式不正确\"}"); break;
            default: resp(t, "{\"status\":1,\"msg\":\"创建失败\"}"); break;
        }
    }
    
    // 管理员功能：删除用户
    static void handleAdminDeleteUser(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        // 获取当前用户信息并检查权限
        String adminId = map.get("adminId");
        User admin = userService.getUserInfo(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        // 不能删除自己
        if (adminId.equals(map.get("userId"))) {
            resp(t, "{\"status\":1,\"msg\":\"不能删除自己\"}");
            return;
        }
        
        // 删除用户
        boolean ok = userService.deleteUserByAdmin(map.get("userId"));
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"删除失败\"}");
    }
    
    // 管理员功能：更新用户信息
    static void handleAdminUpdateUser(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equals("OPTIONS")) { allowCORS(t); t.sendResponseHeaders(200, -1); return; }
        
        String body = readReqBody(t);
        Map<String,String> map = parseUrlEncoded(body);
        
        // 获取当前用户信息并检查权限
        String adminId = map.get("adminId");
        User admin = userService.getUserInfo(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        // 检查是否尝试修改自己的角色
        String targetUserId = map.get("userId");
        if (adminId.equals(targetUserId)) {
            // 如果是修改自己的信息，不允许修改角色
            String newRole = map.getOrDefault("role", "user");
            if (!newRole.equals(admin.getRole())) {
                resp(t, "{\"status\":1,\"msg\":\"不能修改自己的角色\"}");
                return;
            }
        }
        
        // 更新用户信息
        boolean ok = userService.updateUserByAdmin(
            map.get("userId"), 
            map.get("username"), 
            map.get("email"), 
            map.getOrDefault("role", "user"));
            
        resp(t, ok ? "{\"status\":0}" : "{\"status\":1,\"msg\":\"更新失败，用户名可能已存在\"}");
    }
    
    // 管理员功能：获取用户列表
    static void handleAdminListUsers(HttpExchange t) throws IOException {
        allowCORS(t);
        
        // 获取查询参数
        String q = t.getRequestURI().getQuery();
        String adminId = "";
        int page = 1;
        int pageSize = 10;
        String searchKeyword = "";
        
        if (q != null) {
            String[] params = q.split("&");
            for (String param : params) {
                if (param.startsWith("adminId=")) {
                    adminId = param.substring(8);
                } else if (param.startsWith("page=")) {
                    try {
                        page = Integer.parseInt(param.substring(5));
                    } catch (NumberFormatException e) {
                        page = 1;
                    }
                } else if (param.startsWith("pageSize=")) {
                    try {
                        pageSize = Integer.parseInt(param.substring(9));
                    } catch (NumberFormatException e) {
                        pageSize = 10;
                    }
                } else if (param.startsWith("search=")) {
                    try {
                        searchKeyword = URLDecoder.decode(param.substring(7), "UTF-8");
                    } catch (Exception e) {
                        searchKeyword = "";
                    }
                }
            }
        }
        
        System.out.println("请求用户列表，adminId: " + adminId + ", page: " + page + ", pageSize: " + pageSize + ", search: " + searchKeyword);
        
        // 获取当前用户信息并检查权限
        User admin = userService.getUserInfo(adminId);
        System.out.println("获取到的管理员用户: " + (admin != null ? admin.getUsername() + "(" + admin.getRole() + ")" : "null"));
        
        if (admin == null || !"admin".equals(admin.getRole())) {
            System.out.println("权限不足，admin=" + admin + ", role=" + (admin != null ? admin.getRole() : "N/A"));
            resp(t, "{\"status\":1,\"msg\":\"权限不足\"}");
            return;
        }
        
        // 获取用户列表（分页+搜索）
        List<User> users = userService.getAllUsers(page, pageSize, searchKeyword);
        int total = userService.getUserCount(searchKeyword);
        int totalPages = (int) Math.ceil((double) total / pageSize);
        
        System.out.println("获取到的用户数量: " + users.size() + ", 总数: " + total + ", 总页数: " + totalPages);
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(int i=0;i<users.size();i++){
            User u = users.get(i);
            sb.append(String.format("{\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\",\"role\":\"%s\",\"avatar\":\"%s\"}",
                u.getUserId(), u.getUsername(), u.getEmail(), u.getRole(), u.getAvatar()));
            if(i<users.size()-1) sb.append(",");
        }
        sb.append("]");
        
        String jsonData = sb.toString();
        String escapedData = jsonData.replace("\\", "\\\\").replace("\"", "\\\"");
        String result = String.format("{\"status\":0,\"data\":\"%s\",\"total\":%d,\"page\":%d,\"pageSize\":%d,\"totalPages\":%d}", 
            escapedData, total, page, pageSize, totalPages);
        
        System.out.println("返回的用户数据: " + result);
        resp(t, result);
    }
}

