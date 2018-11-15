package eubr.atmosphere.tma.analyze.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.utils.Score;
import eubr.atmosphere.tma.analyze.utils.Constants;

public class DataManager {

    private Connection connection = DatabaseManager.getConnectionInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManager.class);

    public DataManager() {
        this.connection = DatabaseManager.getConnectionInstance();
    }

    public Score getData(String stringTime) {
        String sql = "select * from Data "
                + "where "
                + "DATE_FORMAT(valueTime, \"%Y-%m-%d %H:%i\") = ? "
                + "order by valueTime desc;";
        Score score = null;

        try {
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, stringTime);
            ResultSet rs = DatabaseManager.executeQuery(ps);

            if (rs.next()) {
                score = new Score();
                int cpuCount = 0;
                int memoryCount = 0;
                do {
                    int descriptionId = ((Integer) rs.getObject("descriptionId"));
                    int resourceId = ((Integer) rs.getObject("resourceId"));

                    Double value = ((Double) rs.getObject("value"));

                    if (descriptionId == Constants.cpuDescriptionId) {
                        if (isMonitorizedPod(resourceId)) {
                            score.setCpuPod(score.getCpuPod() + value);
                            cpuCount++;
                        } else {
                            if (resourceId == Constants.nodeId) {
                                // score.setCpuNode(value);
                                // However, it was decided to change the score to use the maximum of the CPU capacity.
                                // This way, we will know how much CPU the pod is using
                                score.setCpuNode(Constants.maxCPU);
                            } else {
                                LOGGER.debug("Something is not right! " + stringTime);
                            }
                        }
                    } else {
                        // Memory
                        if (descriptionId == Constants.memoryDescriptionId) {
                            if (isMonitorizedPod(resourceId)) {
                                // It is needed to convert from bytes to Mi
                                score.setMemoryPod(score.getMemoryPod() + value / 1024);
                                memoryCount++;
                            } else {
                                if (resourceId == Constants.nodeId) {
                                    // It is necessary to divide per 1024 to convert from bytes to Mi.
                                    // score.setMemoryNode(value / 1024);
                                    // However, it was decided to change the score to use the maximum of the memory capacity.
                                    // This way, we will know how much Memory the pod is using
                                    score.setMemoryNode(Constants.maxMemory);
                                } else {
                                    LOGGER.debug("Something is not right! " + stringTime);
                                }
                            }
                        } else {
                            LOGGER.debug("Something is not right! " + stringTime);
                        }
                    }
                    String valueTime = rs.getObject("valueTime").toString();
                } while (rs.next());

                LOGGER.debug("cpuCount: {} / memoryCount: {}", cpuCount, memoryCount);

                // It calculate the average of the metrics of the monitorized pods
                if (cpuCount > 0) {
                    score.setCpuPod(score.getCpuPod() / cpuCount);
                }

                if (memoryCount > 0) {
                    score.setMemoryPod(score.getMemoryPod() / memoryCount);
                }

            } else {
                System.out.println("No data on: " + stringTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return score;
    }

    public boolean isMonitorizedPod(int podId) {
        return Constants.monitorizedPods.contains(podId);
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
