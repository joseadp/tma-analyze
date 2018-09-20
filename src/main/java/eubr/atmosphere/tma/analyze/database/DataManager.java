package eubr.atmosphere.tma.analyze.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataManager {

    public void getData(String stringTime) {
        DatabaseManager databaseManager = new DatabaseManager();
        String sql = "select * from Data "
                + "where "
                + "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i\") = \"" + stringTime + "\""
                + "order by valueTime;";
        ResultSet rs = databaseManager.executeQuery(sql);

        try {
            System.out.println("stringTime: " + stringTime);
            System.out.println("sql: " + sql);
            while (rs.next()) {
                System.out.println(rs.getObject("descriptionId"));
                System.out.println(rs.getObject("resourceId"));
                System.out.println(rs.getObject("valueTime"));
                System.out.println(rs.getObject("value"));
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
