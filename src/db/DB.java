package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/school_event?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci";
    private static final String USER = "root";
    private static final String PWD = "123456zhou";
    static {
        try {Class.forName("com.mysql.cj.jdbc.Driver");} catch(Exception e){e.printStackTrace();}
        initTable();
    }
    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(URL,USER,PWD);
    }
    public static void initTable() {
        try(Connection conn = getConn(); Statement s = conn.createStatement()){
            // 用户user表 (修改id字段长度以适应8位数字ID)
            s.executeUpdate("CREATE TABLE IF NOT EXISTS user (id VARCHAR(8) PRIMARY KEY, username VARCHAR(64) NOT NULL UNIQUE, password VARCHAR(64), email VARCHAR(128), avatar VARCHAR(255), role VARCHAR(32) DEFAULT 'user') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            try{ s.executeUpdate("ALTER TABLE user ADD COLUMN role VARCHAR(32) DEFAULT 'user'"); }catch(Exception ignore){}
            // 活动activity表（新增列）
            s.executeUpdate("CREATE TABLE IF NOT EXISTS activity (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128), description VARCHAR(512), publisher_id VARCHAR(8), max_num INT, event_time VARCHAR(256), status VARCHAR(32) DEFAULT 'approved') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            try{ s.executeUpdate("ALTER TABLE activity MODIFY event_time VARCHAR(256)"); }catch(Exception ignore){}
            try{ s.executeUpdate("ALTER TABLE activity ADD COLUMN start_time VARCHAR(32)"); }catch(Exception ignore){}
            try{ s.executeUpdate("ALTER TABLE activity ADD COLUMN end_time VARCHAR(32)"); }catch(Exception ignore){}
            try{ s.executeUpdate("ALTER TABLE activity ADD COLUMN published_at BIGINT"); }catch(Exception ignore){}
            try{ s.executeUpdate("ALTER TABLE activity ADD COLUMN status VARCHAR(32) DEFAULT 'approved'"); }catch(Exception ignore){}
            // 报名registration表 (修改id和user_id字段长度以适应8位数字ID)
            s.executeUpdate("CREATE TABLE IF NOT EXISTS registration (id VARCHAR(8) PRIMARY KEY, user_id VARCHAR(8), activity_id VARCHAR(36), status VARCHAR(32)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            // 一次性清空旧活动数据（若需要）
            //try{ s.executeUpdate("DELETE FROM activity"); }catch(Exception ignore){}
        }catch(Exception e){e.printStackTrace();}
    }
}