package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<Double> getValuesPeriod(String initialDateTime, String finalDateTime,
            int descriptionId, int resourceId) {
        String sql = "select * from Data "
                + "where "
                + "valueTime between ? and ? and "
                + "descriptionId = ? and "
                + "resourceId = ? "
                + "order by valueTime;";

        List<Double> values = new ArrayList<Double>();

        try {
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, initialDateTime);
            ps.setString(2, finalDateTime);
            ps.setInt(3, descriptionId);
            ps.setInt(4, resourceId);

            ResultSet rs = DatabaseManager.executeQuery(ps);

            while (rs.next()) {
                Double value = ((Double) rs.getObject("value"));
                values.add(value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }
}
