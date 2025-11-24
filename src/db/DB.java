package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/school_event?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci&createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PWD = "123456zhou";
    static {
        // Try to load JDBC driver class. If not available on classpath, try to load the jar from ./lib or other locations
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            // try to load the driver jar from common locations
            try {
                String jar = findJdbcJar();
                if (jar != null) {
                    loadJdbcDriverFromLib(jar);
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } else {
                    throw new IllegalStateException("MySQL connector jar not found in common locations.");
                }
            } catch (Exception ex) {
                // final fallback — print helpful message (driver not found)
                System.err.println("Failed to load MySQL JDBC driver automatically. Make sure 'mysql-connector-j-*.jar' is on the classpath or in the project's lib folder.");
                ex.printStackTrace();
            }
        }
        initTable();
    }

    public static Connection getConn() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PWD);
        } catch (SQLException e) {
            // If no suitable driver, try to (re)load the driver jar and retry once.
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("no suitable driver")) {
                try {
                    String jar = findJdbcJar();
                    if (jar != null) {
                        loadJdbcDriverFromLib(jar);
                    }
                } catch (Exception ex) {
                    System.err.println("Retry to load JDBC driver failed: " + ex.getMessage());
                }
                // retry connection once
                return DriverManager.getConnection(URL, USER, PWD);
            }
            throw e;
        }
    }

    public static void initTable() {
        try (Connection conn = getConn(); Statement s = conn.createStatement()) {
            // 用户user表 (修改id字段长度以适应8位数字ID)
            s.executeUpdate("CREATE TABLE IF NOT EXISTS user (id VARCHAR(8) PRIMARY KEY, username VARCHAR(64) NOT NULL UNIQUE, password VARCHAR(64), email VARCHAR(128), avatar VARCHAR(255), role VARCHAR(32) DEFAULT 'user') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            try { s.executeUpdate("ALTER TABLE user ADD COLUMN role VARCHAR(32) DEFAULT 'user'"); } catch (Exception ignore) {}
            // 活动activity表（新增列）
            s.executeUpdate("CREATE TABLE IF NOT EXISTS activity (id VARCHAR(36) PRIMARY KEY, name VARCHAR(128), description VARCHAR(512), publisher_id VARCHAR(8), max_num INT, event_time VARCHAR(256), status VARCHAR(32) DEFAULT 'approved') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            try { s.executeUpdate("ALTER TABLE activity MODIFY event_time VARCHAR(256)"); } catch (Exception ignore) {}
            try { s.executeUpdate("ALTER TABLE activity ADD COLUMN start_time VARCHAR(32)"); } catch (Exception ignore) {}
            try { s.executeUpdate("ALTER TABLE activity ADD COLUMN end_time VARCHAR(32)"); } catch (Exception ignore) {}
            try { s.executeUpdate("ALTER TABLE activity ADD COLUMN published_at BIGINT"); } catch (Exception ignore) {}
            try { s.executeUpdate("ALTER TABLE activity ADD COLUMN status VARCHAR(32) DEFAULT 'approved'"); } catch (Exception ignore) {}
            // 报名registration表 (修改id和user_id字段长度以适应8位数字ID)
            s.executeUpdate("CREATE TABLE IF NOT EXISTS registration (id VARCHAR(8) PRIMARY KEY, user_id VARCHAR(8), activity_id VARCHAR(36), status VARCHAR(32)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");

            // 如果表为空，尝试从资源 SQL 文件导入初始数据
            boolean needLoad = isTableEmpty(conn, "user") && isTableEmpty(conn, "activity") && isTableEmpty(conn, "registration");
            if (needLoad) {
                // Prefer classpath resource first (when packaged), else fallback to several common file locations
                InputStream sqlStream = DB.class.getResourceAsStream("/db/school_event.sql");
                if (sqlStream == null) {
                    // try several common filesystem locations (useful when running from IDE/project root)
                    String[] candidates = new String[] {
                        "resources/db/school_event.sql",
                        "src/resources/db/school_event.sql",
                        "src/main/resources/db/school_event.sql",
                        "resources/school_event.sql",
                        "db/school_event.sql"
                    };
                    for (String p : candidates) {
                        File f = new File(p);
                        if (f.exists()) {
                            try { sqlStream = new java.io.FileInputStream(f); break; } catch (Exception ex) { sqlStream = null; }
                        }
                    }
                }
                if (sqlStream != null) {
                    try { runSqlScript(conn, sqlStream); System.out.println("Loaded initial data from school_event.sql"); } catch (Exception ex) { ex.printStackTrace(); }
                } else {
                    System.out.println("No school_event.sql found on classpath or in common resource locations. Skipping initial data import.");
                }
            }

            // 一次性清空旧活动数据（若需要）
            //try{ s.executeUpdate("DELETE FROM activity"); }catch(Exception ignore){}
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static boolean isTableEmpty(Connection conn, String table) {
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (Exception e) {
            // If table doesn't exist or query fails, return true (so init will try to populate)
            return true;
        }
        return true;
    }

    private static void runSqlScript(Connection conn, InputStream in) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                // skip SQL comments
                String trim = line.trim();
                if (trim.startsWith("--") || trim.startsWith("//") || trim.startsWith("/*")) continue;
                sb.append(line).append('\n');
            }
            // simple split on semicolon — sufficient for straightforward SQL seed files
            String[] statements = sb.toString().split(";\n|;\r\n|;");
            try (Statement st = conn.createStatement()) {
                for (String raw : statements) {
                    String sql = raw.trim();
                    if (sql.isEmpty()) continue;
                    try {
                        st.execute(sql);
                    } catch (SQLException e) {
                        // print and continue — don't fail entire import on one bad statement
                        System.err.println("Failed to execute statement: " + sql);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String findJdbcJar() {
        // search common locations for a mysql connector jar
        String[] locations = new String[] {"lib", "./lib", "../lib", "libs", "./libs"};
        for (String loc : locations) {
            File dir = new File(loc);
            if (dir.exists() && dir.isDirectory()) {
                File[] matches = dir.listFiles((d, name) -> name.toLowerCase().contains("mysql-connector-j") && name.toLowerCase().endsWith(".jar"));
                if (matches != null && matches.length > 0) {
                    return matches[0].getPath();
                }
                // also accept connector jars named mysql-connector-java
                matches = dir.listFiles((d, name) -> name.toLowerCase().contains("mysql-connector") && name.toLowerCase().endsWith(".jar"));
                if (matches != null && matches.length > 0) return matches[0].getPath();
            }
        }
        // also try project root paths
        String[] rootCandidates = new String[] {"lib/mysql-connector-j-9.1.0.jar", "lib\\mysql-connector-j-9.1.0.jar", "lib/mysql-connector-java.jar"};
        for (String p : rootCandidates) {
            File f = new File(p);
            if (f.exists()) return f.getPath();
        }
        return null;
    }

    private static void loadJdbcDriverFromLib(String relativeJarPath) throws Exception {
        File jar = new File(relativeJarPath);
        if (!jar.exists()) {
            // try with backslash for windows, or another common name
            jar = new File("lib\\mysql-connector-j-9.1.0.jar");
        }
        if (!jar.exists()) {
            throw new IllegalStateException("JDBC driver jar not found at '" + relativeJarPath + "' or 'lib\\mysql-connector-j-9.1.0.jar'. Please add the MySQL connector jar to the lib folder or to your classpath.");
        }
        URL jarUrl = jar.toURI().toURL();
        // Create a URLClassLoader and add it to the context classloader so Class.forName can find the driver
        URLClassLoader child = new URLClassLoader(new URL[] { jarUrl }, DB.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(child);
        // Load the driver class using the new classloader and register an instance with DriverManager to avoid classloader issues
        Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver", true, child);
        Object driverObj = driverClass.getDeclaredConstructor().newInstance();
        if (driverObj instanceof Driver) {
            DriverManager.registerDriver(new DriverShim((Driver) driverObj));
        } else {
            // fallback: try to call Class.forName normally (may register driver)
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
    }

    // A small wrapper that delegates java.sql.Driver calls to the actual driver instance loaded from the connector jar.
    // This helps avoid classloader mismatches when registering drivers dynamically.
    private static class DriverShim implements Driver {
        private final Driver delegate;
        DriverShim(Driver d) { this.delegate = d; }
        public boolean acceptsURL(String u) throws SQLException { return delegate.acceptsURL(u); }
        public Connection connect(String u, Properties p) throws SQLException { return delegate.connect(u, p); }
        public int getMajorVersion() { return delegate.getMajorVersion(); }
        public int getMinorVersion() { return delegate.getMinorVersion(); }
        public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException { return delegate.getPropertyInfo(u, p); }
        public boolean jdbcCompliant() { return delegate.jdbcCompliant(); }
        public Logger getParentLogger() { try { return delegate.getParentLogger(); } catch (Exception e) { return Logger.getGlobal(); } }
    }
}