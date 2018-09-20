package eubr.atmosphere.tma.analyze.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DataManager {

    public void getData() {
        DatabaseManager databaseManager = new DatabaseManager();
        ResultSet rs = databaseManager.executeQuery("select * from Data order by valueTime;");
        
        try {
            while (rs.next()) {
                System.out.println(rs.getObject("probeId"));
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
