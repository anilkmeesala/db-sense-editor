package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static String url = "jdbc:sqlite:sample.db";
    private static String driver = "org.sqlite.JDBC";
    private static String user = "";
    private static String password = "";

    public static void configure(Properties p) {
        url = p.getProperty("jdbc.url", url);
        driver = p.getProperty("jdbc.driver", driver);
        user = p.getProperty("jdbc.user", user);
        password = p.getProperty("jdbc.password", password);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver class not found: " + driver + ". Make sure the JDBC driver is on the classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        if (user == null || user.isEmpty()) {
            return DriverManager.getConnection(url);
        } else {
            return DriverManager.getConnection(url, user, password);
        }
    }
}
