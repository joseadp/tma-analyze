package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import eubr.atmosphere.tma.analyze.Score;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class DataManager {

	private Connection connection = DatabaseManager.getConnectionInstance();

	public DataManager() {
		this.connection = DatabaseManager.getConnectionInstance();
	}

    public Score getData(String stringTime) {
        String sql = "select * from Data "
                + "where "
                + "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i\") = ? "
                + "order by valueTime;";
        Score score = null;

        try {
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, stringTime);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            if (rs.next()) {
                score = new Score();
                String valueTime = "";
                do {
                    int descriptionId = ((Integer) rs.getObject("descriptionId"));
                    int resourceId = ((Integer) rs.getObject("resourceId"));

                    Double value = ((Double) rs.getObject("value"));

                    if (descriptionId == Constants.cpuDescriptionId) {
                        if (resourceId == Constants.podId) {
                            score.setCpuPod(value);
                        } else {
                            if (resourceId == Constants.nodeId) {
                                score.setCpuNode(value);
                            } else {
                                System.err.println("Something is not right! " + stringTime);
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
                                    System.err.println("Something is not right! " + stringTime);
                                }
                            }
                        } else {
                            System.err.println("Something is not right! " + stringTime);
                        }
                    }
                    valueTime = rs.getObject("valueTime").toString();
                } while (rs.next());
            } else {
                System.out.println("No data on: " + stringTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }
}
