package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eubr.atmosphere.tma.analyze.utils.PropertiesManager;

public class DatabaseManager {
    private static Connection connection = null;
    private Statement statement = null;
    private ResultSet resultSet = null;

    public static Connection getConnectionInstance() {
        // This will load the MySQL driver, each DB has its own driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Setup the connection with the DB
        try {
            String connString = PropertiesManager.getInstance().getProperty("connectionString");
            String user = PropertiesManager.getInstance().getProperty("user");
            String password = PropertiesManager.getInstance().getProperty("password");

            connection = DriverManager.getConnection(connString, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    public static ResultSet executeQuery(String sql) {
        Connection conn = getConnectionInstance();
        Statement stmt;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static ResultSet executeQuery(PreparedStatement ps) {
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {

        }
    }

    public int execute(PreparedStatement ps) {
        int key = -1;
        try {
            ps.execute();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                key = generatedKeys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }
}
