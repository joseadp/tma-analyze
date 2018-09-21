package eubr.atmosphere.tma.analyze.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eubr.atmosphere.tma.analyze.Score;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class DataManager {

    public void getData(String stringTime) {
        DatabaseManager databaseManager = new DatabaseManager();
        String sql = "select * from Data "
                + "where "
                + "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i\") = ? "
                + "order by valueTime;";

        try {
            PreparedStatement ps =
                    DatabaseManager.getConnectionInstance().prepareStatement(sql);
            ps.setString(1, stringTime);
            ResultSet rs = databaseManager.executeQuery(ps);

            System.out.println("stringTime: " + stringTime);
            Score score = new Score();
            String valueTime = "";
            while (rs.next()) {
                int descriptionId = ((Integer) rs.getObject("descriptionId"));
                int resourceId = ((Integer) rs.getObject("resourceId"));

                Double value = ((Double) rs.getObject("value"));
                System.out.println();

                if (descriptionId == Constants.cpuDescriptionId) {
                    if (resourceId == Constants.podId) {
                        score.setCpuPod(value);
                    } else {
                        if (resourceId == Constants.nodeId) {
                            score.setCpuNode(value);
                        } else {
                            System.err.println("Something is not right!");
                        }
                    }
                } else {
                    // Memory
                    if (descriptionId == Constants.memoryDescriptionId) {
                        if (resourceId == Constants.podId) {
                            score.setMemoryPod(value);
                        } else {
                            if (resourceId == Constants.nodeId) {
                                score.setMemoryNode(value);
                            } else {
                                System.err.println("Something is not right!");
                            }
                        }
                    } else {
                        System.err.println("Something is not right!");
                    }
                }
                valueTime = rs.getObject("valueTime").toString();
            }

            System.out.println(valueTime + ": " + score.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
